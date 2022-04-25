package com.github.palFinderTeam.palfinder.meetups.activities

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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_edit_compat)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.creation_compat_content) as NavHostFragment).navController
        navController.setGraph(
            R.navigation.creation_compat_nav, bundleOf(
                Pair("MeetUpId", intent.getStringExtra(MEETUP_EDIT))
            )
        )
    }
}

const val MEETUP_EDIT = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_EDIT"