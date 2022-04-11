package com.github.palFinderTeam.palfinder

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.github.palFinderTeam.palfinder.navbar.NavigationBarFragment
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

        bottomNavigationView!!.setupWithNavController(navController)

        tabMenu.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        when (tab.position) {
                            0 -> navController.navigate(R.id.maps_fragment)
                            1 -> {
                                Log.i("Main", "List")
                                navController.navigate(R.id.list_fragment)
                            }
                            else -> {
                                Log.i("Main", tab.id.toString())
                                Log.i("Main", R.id.list_fragment.toString())
                            }
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