package com.github.palFinderTeam.palfinder.utils

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.content.ContextCompat.startForegroundService
import kotlinx.coroutines.*

const val START_SERVICE = "com.github.palFinderTeam.palFinder.service.start"
const val STOP_SERVICE = "com.github.palFinderTeam.palFinder.service.stop"
private var isServiceStarted = false

fun <T> start(context: Context, clazz: Class<T>){
    Intent(context, EndlessService::class.java).also {
        startForegroundService(context, it)
    }
}

abstract class EndlessService(val delay: Int) : Service() {
    private var wakeLock: PowerManager.WakeLock? = null

    abstract fun action()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                START_SERVICE -> startService()
                STOP_SERVICE -> stopService()
            }
        }
        return START_STICKY
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startService() {
        if (isServiceStarted) return
        isServiceStarted = true

        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire(10*60*1000L /*10 minutes*/)
                }
            }

        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    action()
                }
                delay(delay * 1000L)
            }
        }
    }

    private fun stopService() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            
        }
        isServiceStarted = false
    }
}