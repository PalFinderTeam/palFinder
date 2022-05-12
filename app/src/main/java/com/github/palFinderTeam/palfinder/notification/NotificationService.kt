package com.github.palFinderTeam.palfinder.notification

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.chat.CachedChatService
import com.github.palFinderTeam.palfinder.chat.ChatActivity
import com.github.palFinderTeam.palfinder.chat.ChatService
import com.github.palFinderTeam.palfinder.di.FirestoreModule
import com.github.palFinderTeam.palfinder.meetups.CachedMeetUpService
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.CachedProfileService
import com.github.palFinderTeam.palfinder.profile.FirebaseProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.utils.EndlessService
import com.github.palFinderTeam.palfinder.utils.context.AppContextService
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import com.github.palFinderTeam.palfinder.utils.time.isBefore
import com.github.palFinderTeam.palfinder.utils.time.RealTimeService
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class NotificationService @Inject constructor(
    val contextService: ContextService,
    val timeService: TimeService,
    val meetupService: MeetUpRepository,
    val profileService: ProfileService,
    val chatService: ChatService
): JobService() {

    constructor():this(
        AppContextService(),
        RealTimeService(),
        CachedMeetUpService(FirebaseMeetUpService(FirestoreModule.provideFirestore(),FirebaseProfileService(FirestoreModule.provideFirestore())), RealTimeService(), AppContextService()),
        CachedProfileService(FirebaseProfileService(FirestoreModule.provideFirestore()), RealTimeService(), AppContextService()),
        CachedChatService(FirestoreModule.provideFirestore(), AppContextService()),
    )

    private val notifications = DictionaryCache("notification", CachedNotification::class.java, false, contextService.get())
    private val meetupsMetaData = DictionaryCache("meetup_meta", MeetupMetaData::class.java, false, contextService.get())
    private val profileMetaData = DictionaryCache("profile_meta", ProfileMetaData::class.java, false, contextService.get())

    @RequiresApi(Build.VERSION_CODES.O)
    fun action(){
        for (notif in notifications.getAll()){
            if (notif.time.isBefore(timeService.now())){
                NotificationHandler(contextService.get()).post(notif.title, notif.content, notif.icon)
                notifications.delete(notif.uuid)
            }
        }
        val context = contextService.get()
        runBlocking {
            val id = profileService.getLoggedInUserID()
            if (id != null){
                val logged = profileService.fetch(id!!)
                if (logged != null){
                    for(p in logged!!.following){
                        val user = profileService.fetch(p)
                        if (!profileMetaData.contains(p) && user != null){
                            profileMetaData.store(p, ProfileMetaData(p, true))
                            NotificationHandler(context).post(context.getString(R.string.following_title), context.getString(R.string.following_content).format(user.username), R.drawable.icon_beer)
                        }
                    }
                }
            }
            for (m in meetupService.getAllJoinedMeetupID()) {
                var meetup = meetupService.fetch(m)
                val meta = if (meetupsMetaData.contains(m)) {
                    meetupsMetaData.get(m)
                } else {
                    val ret = MeetupMetaData(m, false, "")
                    meetupsMetaData.store(m, ret)
                    ret
                }
                if (meetup != null) {
                    // Check if meetup started and notification not sent
                    if (!meta.sendStartNotification && meetup.startDate.isBefore(timeService.now())) {
                        NotificationHandler(context).post(meetup.name, meetup.description, R.drawable.icon_beer)
                        meta.sendStartNotification = true
                        meetupsMetaData.store(m, meta)
                    }

                    // Look for message of that meetup
                    val messages = chatService.fetchMessages(m)
                    if (messages!= null && messages.isNotEmpty()) {
                        val meta = meetupsMetaData.get(m)
                        val last = messages.takeLast(1)[0]
                        val hash = last.hashCode().toString()

                        if (hash != meta.lastMessageNotification && last.sentBy != profileService.getLoggedInUserID()) {
                            val name = profileService.fetch(last.sentBy)?.username?:""
                            if (ChatActivity.currentlyViewChat != m) {
                                NotificationHandler(context).post(name, last.content, R.drawable.icon_beer)
                            }
                            meta.lastMessageNotification = hash
                            meetupsMetaData.store(m, meta)
                        }
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
    data class ProfileMetaData(var uuid: String, var sendStartNotification: Boolean)
}