package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import com.github.palFinderTeam.palfinder.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * This class is just here to help doing the transition from activities to navigation.
 * It will be removed later.
 */
@AndroidEntryPoint
class MeetUpEditCompat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("theme", Context.MODE_PRIVATE) ?: return
        val theme = sharedPref.getInt("theme", R.style.palFinder_default_theme)
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_edit_compat)
        var sharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "theme") {
                    recreate()
                }
            }
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        val navController =
            (supportFragmentManager.findFragmentById(R.id.creation_compat_content) as NavHostFragment).navController
        navController.setGraph(
            R.navigation.creation_compat_nav, bundleOf(
                Pair("MeetUpId", intent.getStringExtra(MEETUP_EDIT))
            )
        )
    }
}