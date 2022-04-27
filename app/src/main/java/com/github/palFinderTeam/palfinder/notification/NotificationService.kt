package com.github.palFinderTeam.palfinder.notification

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.utils.EndlessService
import com.github.palFinderTeam.palfinder.utils.isBefore
import com.github.palFinderTeam.palfinder.utils.time.RealTimeService
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import javax.inject.Inject


class NotificationService @Inject constructor(
    var chatService: TimeService
    ): JobService() {

    constructor() : this(RealTimeService())

    private val notifications = DictionaryCache("notification", CachedNotification::class.java, false, this)

    @RequiresApi(Build.VERSION_CODES.O)
    fun action() {
        for (notif in notifications.getAll()){
            if (notif.time.isBefore(chatService.now())){
                NotificationHandler(this).post(notif.title, notif.content, notif.icon)
                notifications.delete(notif.uuid)
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
}