package com.github.palFinderTeam.palfinder.navigation

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity.Companion.SHOW_NAVBAR_ARG
import com.google.android.material.tabs.TabLayout

class FindFragment : Fragment(R.layout.fragment_find) {

    private val findViewModel: FindViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // We can't inflate the NavHostFragment from XML because it will crash the 2nd time the dialog is opened
        val navHost = NavHostFragment()
        childFragmentManager.beginTransaction().replace(R.id.find_content, navHost).commitNow()
        navHost.navController.setGraph(R.navigation.find_nav_graph)
        val tabMenu = view.findViewById<TabLayout>(R.id.find_tabs)
        val navController = navHost.navController

        // Hide navBar when needed
        navController.addOnDestinationChangedListener { _, _, arguments ->
            (requireActivity() as MainNavActivity).hideShowNavBar(
                arguments?.getBoolean(
                    SHOW_NAVBAR_ARG,
                    false
                ) == true
            )
        }

        tabMenu.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        when (tab.position) {
                            0 -> {
                                findViewModel.tabState = 0
                                navController.popBackStack()
                                navController.navigate(R.id.maps_fragment)
                            }
                            1 -> {
                                findViewModel.tabState = 1
                                navController.popBackStack()
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

        tabMenu.getTabAt(findViewModel.tabState)?.select()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do custom work here
                    if(!navController.navigateUp()) {
                        // If no up navigation available, fall back on the main activity.
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })

    }
}