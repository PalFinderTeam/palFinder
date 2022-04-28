package com.github.palFinderTeam.palfinder

import android.app.Application
import android.content.Context
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PalFinderApplication : Application() {

    var iconPack: IconPack? = null

    companion object {
        lateinit var instance: PalFinderApplication private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        loadIconPack()
    }


    fun loadIconPack() {
        // Create an icon pack loader with application context.
        val loader = IconPackLoader(this)

        // Create an icon pack and load all drawables.
        val iconPack = createDefaultIconPack(loader)
        iconPack.loadDrawables(loader.drawableLoader)

        this.iconPack = iconPack
    }

}