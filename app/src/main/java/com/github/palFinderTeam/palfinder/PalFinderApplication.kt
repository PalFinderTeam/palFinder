package com.github.palFinderTeam.palfinder

import android.app.Application
import com.github.palFinderTeam.palfinder.utils.EndlessService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PalFinderApplication : Application() {
    companion object {
        lateinit var instance: PalFinderApplication private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        EndlessService.scheduleJob(this)
    }

}