package com.github.palFinderTeam.palfinder.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.cache.FileCache
import com.github.palFinderTeam.palfinder.profile.USER_ID

private const val CHANNEL_ID = "PalFinder"
private const val NOTIFICATION = "notification"

class NotificationHandler (
    private val context: Context
): BroadcastReceiver(){
    private var hasCreateChannel = false
    private var data = MetaData(0)

    private var cache = FileCache("NotificationHandlerMetadata", MetaData::class.java, true, context)
    private val notifications = DictionaryCache("notification", CachedNotification::class.java, false, context)

    private fun initChannel(){
        if (!hasCreateChannel){
            hasCreateChannel = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.app_name)
                val descriptionText = context.getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }

                val notificationManager: NotificationManager =
                    getSystemService(context, NotificationManager::class.java)!!
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun getNotificationUUID():Int{
        if (cache.exist()){
            data = cache.get()
        }
        val ret = data.notificationId++
        cache.store(data)
        return ret
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(title: String, content: String, icon: Int, ): Notification{
        return Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(title: String, content: String, icon: Int, intent: Intent): Notification{
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, getNotificationUUID(), intent, PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }


    private fun post(notification: Notification){
        initChannel()
        with(NotificationManagerCompat.from(context)) {
            notify(getNotificationUUID(), notification)
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
            context.getString(title),
            context.getString(content),
            icon
        )
    }

    /**
     * Post a Notification
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun post(title: String, content: String, icon: Int, intent: Intent){
        post(createNotification(title,content, icon, intent))
    }

    /**
     * Post a Notification
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun post(title: Int, content: Int, icon: Int, intent: Intent){
        post(
            context.getString(title),
            context.getString(content),
            icon,
            intent
        )
    }

    private fun schedule(notification: CachedNotification){
        notifications.store(notification.uuid, notification)
    }

    /**
     * Schedule a Notification for the [date]
     * @param date: Date to post the notification
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    fun schedule(date: Calendar, title: String, content: String, icon: Int){
        schedule(CachedNotification(getNotificationUUID().toString(), date, title,content, icon))
    }

    /**
     * Schedule a Notification for the [date]
     * @param date: Date to post the notification
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    fun schedule(date: Calendar, title: Int, content: Int, icon: Int){
        schedule(
            date,
            context.getString(title),
            context.getString(content),
            icon
        )
    }

    /**
     * Schedule a Notification for the [date]
     * @param date: Date to post the notification
     * @param uuid: UUID of the notification (Create new notification if new uuid otherwise override previous)
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    fun schedule(date: Calendar, uuid: String,title: String, content: String, icon: Int){
        schedule(CachedNotification(uuid, date, title,content, icon))
    }

    /**
     * Schedule a Notification for the [date]
     * @param date: Date to post the notification
     * @param uuid: UUID of the notification (Create new notification if new uuid otherwise override previous)
     * @param title: Title of the notification
     * @param content: Content of the notification
     * @param icon: Icon of the notification
     */
    fun schedule(date: Calendar, uuid: String,title: Int, content: Int, icon: Int){
        schedule(CachedNotification(uuid, date, context.getString(title), context.getString(content), icon))
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val notification: Notification = intent!!.getParcelableExtra(NOTIFICATION)!!
        post(notification)
    }

    data class MetaData(var notificationId:Int)
}