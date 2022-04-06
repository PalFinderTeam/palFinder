package com.github.palFinderTeam.palfinder.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.github.palFinderTeam.palfinder.R
import java.util.*


object NotificationHandler: BroadcastReceiver(){
    private var hasCreateChannel = false
    var NOTIFICATION = "notification"
    var notificationId = 0

    val CHANNEL_ID = "PalFinder"

    private fun initChannel(context: Context){
        if (!hasCreateChannel){
            hasCreateChannel = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.app_name)
                val descriptionText = context.getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    getSystemService(context, NotificationManager::class.java)!!
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun post(context: Context, notification: Notification){
        initChannel(context)
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId++, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun post(context: Context, title: String, content: String){
        var builder = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)

        post(context, builder.build())
    }


    fun schedule(context: Context, date: Calendar, notification: Notification){
        val notificationIntent = Intent(context, this::class.java).apply {
            putExtra(NOTIFICATION, notification)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(context, AlarmManager::class.java)
        alarmManager!![AlarmManager.ELAPSED_REALTIME_WAKEUP, date.timeInMillis] = pendingIntent
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun schedule(context: Context, date: Calendar, title: String, content: String){
        var builder = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)

        schedule(context, date, builder.build())
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val notification: Notification = intent!!.getParcelableExtra(NOTIFICATION)!!
        post(context!!, notification)
    }
}