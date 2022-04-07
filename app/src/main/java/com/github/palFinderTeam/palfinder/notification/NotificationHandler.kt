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
import com.github.palFinderTeam.palfinder.cache.FileCache
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import java.util.*
import javax.inject.Inject

private const val CHANNEL_ID = "PalFinder"
private const val NOTIFICATION = "notification"

class NotificationHandler @Inject constructor(
    private val contextProvider: ContextService
): BroadcastReceiver(){
    private var hasCreateChannel = false
    private var data = MetaData(0)

    private var cache = FileCache("NotificationHandlerMetadata", MetaData::class.java, true, contextProvider.get())

    private fun initChannel(){
        if (!hasCreateChannel){
            hasCreateChannel = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = contextProvider.get().getString(R.string.app_name)
                val descriptionText = contextProvider.get().getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }

                val notificationManager: NotificationManager =
                    getSystemService(contextProvider.get(), NotificationManager::class.java)!!
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(title: String, content: String, icon: Int): Notification{
        return Notification.Builder(contextProvider.get(), CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .build()
    }


    private fun post(notification: Notification){
        initChannel()
        with(NotificationManagerCompat.from(contextProvider.get())) {
            if (cache.exist()){
                data = cache.get()
            }
            notify(data.notificationId++, notification)
            cache.store(data)
        }
    }

    /**
     * Post a Notification
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun post(title: String, content: String, icon: Int){
        post(createNotification(title,content, icon))
    }

    /**
     * Post a Notification
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun post(title: Int, content: Int, icon: Int){
        post(
            contextProvider.get().getString(title),
            contextProvider.get().getString(content),
            icon
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun schedule(date: Calendar, notification: Notification){
        val notificationIntent = Intent(contextProvider.get(), this::class.java).apply {
            putExtra(NOTIFICATION, notification)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            contextProvider.get(),
            0,
            notificationIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val alarmManager = getSystemService(contextProvider.get(), AlarmManager::class.java)
        alarmManager!![AlarmManager.ELAPSED_REALTIME_WAKEUP, date.timeInMillis] = pendingIntent
    }

    /**
     * Schedule a Notification for the [date]
     * @param date: Date to post the notification
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun schedule(date: Calendar, title: String, content: String, icon: Int){
        schedule(date, createNotification(title,content, icon))
    }

    /**
     * Schedule a Notification for the [date]
     * @param date: Date to post the notification
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun schedule(date: Calendar, title: Int, content: Int, icon: Int){
        schedule(
            date,
            contextProvider.get().getString(title),
            contextProvider.get().getString(content),
            icon
        )
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val notification: Notification = intent!!.getParcelableExtra(NOTIFICATION)!!
        post(notification)
    }

    data class MetaData(var notificationId:Int)
}