package com.github.palFinderTeam.palfinder.profile

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.github.palFinderTeam.palfinder.PalFinderBaseActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.profile.ProfileFragment.Companion.PROFILE_ID_ARG
import dagger.hilt.android.AndroidEntryPoint

/**
 * This activity acts as a container for [ProfileFragment], the idea
 * being that certain activity are expecting to launch another activity
 * to show a profile, and to avoid changing everything this act as a temporary fix.
 */
@AndroidEntryPoint
class ProfileActivity : PalFinderBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_compat)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.profile_compat_content) as NavHostFragment).navController
        navController.setGraph(
            R.navigation.profile_compat_nav, bundleOf(
                Pair(PROFILE_ID_ARG, intent.getStringExtra(USER_ID))
            )
        )
    }
}