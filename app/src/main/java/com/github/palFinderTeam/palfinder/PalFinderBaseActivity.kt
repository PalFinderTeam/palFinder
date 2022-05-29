package com.github.palFinderTeam.palfinder

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity.Companion.THEME_KEY

/**
 * This is the activity that all of our activities extend from, in order to share the logic for the
 * app theme.
 */
abstract class PalFinderBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences(THEME_KEY, Context.MODE_PRIVATE) ?: return
        val theme = sharedPref.getInt(THEME_KEY, R.style.palFinder_default_theme)
        setTheme(theme)

        sharedPref.registerOnSharedPreferenceChangeListener(preferenceListener)

        super.onCreate(savedInstanceState)
    }

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == THEME_KEY) {
            this.recreate()
        }
    }
}