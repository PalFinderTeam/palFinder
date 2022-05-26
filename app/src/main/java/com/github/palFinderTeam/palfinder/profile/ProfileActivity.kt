package com.github.palFinderTeam.palfinder.profile

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_EDIT
import dagger.hilt.android.AndroidEntryPoint

/**
 * This activity acts as a container for [ProfileFragment], the idea
 * being that certain activity are expecting to launch another activity
to show a profile, and to avoid changing everything this act as a temporary fix.
 */
@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("theme", Context.MODE_PRIVATE) ?: return
        val theme = sharedPref.getInt("theme", R.style.palFinder_default_theme)
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_compat)
        val sharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "theme") {
                    recreate()
                }
            }
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.profile_compat_content) as NavHostFragment).navController
        navController.setGraph(
            R.navigation.profile_compat_nav, bundleOf(
                Pair("UserId", intent.getStringExtra(USER_ID))
            )
        )
    }
}