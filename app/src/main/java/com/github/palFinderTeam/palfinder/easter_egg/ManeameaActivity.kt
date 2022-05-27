package com.github.palFinderTeam.palfinder.easter_egg

import android.R
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity


class ManeameaActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("theme", MODE_PRIVATE) ?: return
        val theme = sharedPref.getInt("theme", com.github.palFinderTeam.palfinder.R.style.palFinder_default_theme)
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(com.github.palFinderTeam.palfinder.R.layout.activity_video)
        val sharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "theme") {
                    recreate()
                }
            }
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        val videoview = findViewById<VideoView>(R.id.easter_egg_video)
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.)
        videoview.setVideoURI(uri)
        videoview.setOnPreparedListener { videoview.start() }
    }
}