package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import com.github.palFinderTeam.palfinder.map.MapsFragment
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainNavActivity : AppCompatActivity() {
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
                        navController.navigate(R.id.list_fragment, args)
                        return@setOnItemSelectedListener true
                    }
                    R.id.nav_bar_find -> {
                        navController.navigate(R.id.maps_fragment)
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
                            0 -> navController.navigate(R.id.maps_fragment)
                            1 -> navController.navigate(R.id.list_fragment)
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
}