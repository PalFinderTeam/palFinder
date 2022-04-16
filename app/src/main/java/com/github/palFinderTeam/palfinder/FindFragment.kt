package com.github.palFinderTeam.palfinder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.tabs.TabLayout

class FindFragment : Fragment(R.layout.fragment_find) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // We can't inflate the NavHostFragment from XML because it will crash the 2nd time the dialog is opened
        val navHost = NavHostFragment()
        childFragmentManager.beginTransaction().replace(R.id.find_content, navHost).commitNow()
        navHost.navController.setGraph(R.navigation.find_nav_graph)
        val tabMenu = view.findViewById<TabLayout>(R.id.find_tabs)
        val navController = navHost.navController


        tabMenu.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        when (tab.position) {
                            0 -> {
                                navController.navigate(R.id.maps_fragment)
                            }
                            1 -> {
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
}