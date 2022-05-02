package com.github.palFinderTeam.palfinder.utils

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.github.palFinderTeam.palfinder.notification.NotificationService


const val START_SERVICE = "com.github.palFinderTeam.palFinder.service.start"
const val STOP_SERVICE = "com.github.palFinderTeam.palFinder.service.stop"
private var isServiceStarted = false

class StartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        EndlessService.scheduleJob(context)
    }
}

abstract class EndlessService{
    companion object{
        fun scheduleJob(context: Context) {
            val serviceComponent = ComponentName(context, NotificationService::class.java)
            val builder = JobInfo.Builder(0, serviceComponent)
            builder.setMinimumLatency((1 * 1000).toLong())
            builder.setOverrideDeadline((3 * 1000).toLong())
            builder.setRequiresCharging(false)
            val jobScheduler = context.getSystemService(JobScheduler::class.java)
            jobScheduler.schedule(builder.build())
        }
    }
}