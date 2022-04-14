package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainNavActivity : AppCompatActivity() {

    private var findState = FindState.MAP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_nav)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment).navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val tabMenu = findViewById<TabLayout>(R.id.find_tabs)

        navController.addOnDestinationChangedListener { _, _, arguments ->
            // Hide navbar when needed
            if (bottomNavigationView != null) {
                bottomNavigationView.isVisible = arguments?.getBoolean("ShowNavBar", false) == true
            }
            // Hide tab bar when needed
            tabMenu.isVisible = arguments?.getBoolean("ShowFindTabs", false) == true
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val selected = bottomNavigationView.selectedItemId
            if (selected != item.itemId) {
                val direction = navItemToPosition(item.itemId) - navItemToPosition(selected)
                val animationIn = if (direction < 0) R.anim.slide_in_left else R.anim.slide_in_right
                val animationOut = if (direction < 0) R.anim.slide_out_right else R.anim.slide_out_left
                val navOptions = NavOptions.Builder()
                navOptions.setEnterAnim(animationIn).setExitAnim(animationOut)

                when (item.itemId) {
                    R.id.nav_bar_create -> {
                        val intent = Intent(this, MeetUpCreation::class.java)
                        startActivity(intent)
                        return@setOnItemSelectedListener true
                    }
                    R.id.nav_bar_groups -> {
                        val args = Bundle().apply {
                            putBoolean("ShowOnlyJoined", true)
                            putBoolean("ShowFindTabs", false)
                        }
                        navController.navigate(
                            R.id.list_fragment,
                            args = args,
                            navOptions = navOptions.build()
                        )
                        return@setOnItemSelectedListener true
                    }
                    R.id.nav_bar_find -> {
                        when (findState) {
                            FindState.MAP -> navController.navigate(
                                R.id.maps_fragment,
                                args = null,
                                navOptions = navOptions.build()
                            )
                            FindState.LIST -> navController.navigate(
                                R.id.list_fragment,
                                args = null,
                                navOptions = navOptions.build()
                            )
                        }
                        return@setOnItemSelectedListener true
                    }
                }
            }
            false
        }

        tabMenu.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        when (tab.position) {
                            0 -> {
                                findState = FindState.MAP
                                navController.navigate(R.id.maps_fragment)
                            }
                            1 -> {
                                findState = FindState.LIST
                                navController.navigate(R.id.list_fragment)
                            }
                            else -> {}
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // Ignore
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // Ignore
                }

            }
        )
    }

    private fun navItemToPosition(itemId: Int): Int {
        return when (itemId) {
            R.id.nav_bar_create -> 0
            R.id.nav_bar_find -> 1
            R.id.nav_bar_groups -> 2
            else -> -1
        }
    }

    // Make sure we keep track of what mode of find was used.
    private enum class FindState {
        MAP,
        LIST
    }
}