package com.github.palFinderTeam.palfinder.notification

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.chat.CachedChatService
import com.github.palFinderTeam.palfinder.chat.ChatService
import com.github.palFinderTeam.palfinder.di.FirestoreModule
import com.github.palFinderTeam.palfinder.meetups.CachedMeetUpService
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.utils.EndlessService
import com.github.palFinderTeam.palfinder.utils.context.AppContextService
import com.github.palFinderTeam.palfinder.utils.isBefore
import com.github.palFinderTeam.palfinder.utils.time.RealTimeService
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class NotificationService @Inject constructor(
    var timeService: TimeService,
    var meetupService: MeetUpRepository,
    var chatService: ChatService
    ): JobService() {

    constructor() : this(
        RealTimeService(),
        CachedMeetUpService(FirestoreModule.provideFirestore(), RealTimeService(), AppContextService()),
        CachedChatService(FirestoreModule.provideFirestore(), AppContextService())
    )


    private val notifications = DictionaryCache("notification", CachedNotification::class.java, false, this)
    private val meetups = DictionaryCache("meetup_meta", MeetupMetaData::class.java, false, this)

    @RequiresApi(Build.VERSION_CODES.O)
    fun action() {
        for (notif in notifications.getAll()){
            if (notif.time.isBefore(timeService.now())){
                NotificationHandler(this).post(notif.title, notif.content, notif.icon)
                notifications.delete(notif.uuid)
            }
        }
        var context = this
        runBlocking {
            for (m in (meetupService as CachedMeetUpService).getAllJoinedMeetupID()) {
                var meetup = meetupService.getMeetUpData(m)
                val meta = if (meetups.contains(m)) {
                    meetups.get(m)
                }else{
                    val ret = MeetupMetaData(m, false, "")
                    meetups.store(m, ret)
                    ret
                }
                if (meetup != null){
                    if (!meta.sendStartNotification && meetup.startDate.isBefore(timeService.now())) {
                        NotificationHandler(context).post(meetup.name, meetup.description, R.drawable.icon_beer)
                        meta.sendStartNotification = true
                        meetups.store(m, meta)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartJob(params: JobParameters?): Boolean {
        action()
        EndlessService.scheduleJob(applicationContext) // reschedule the job
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    data class MeetupMetaData(var uuid: String, var sendStartNotification: Boolean, var lastMessageNotification: String)
}