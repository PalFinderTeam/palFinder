package com.github.palFinderTeam.palfinder.navbar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.MapsActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.github.palFinderTeam.palfinder.meetups.activities.SHOW_JOINED_ONLY
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Class to handle the Navigation Bar Fragments
 */
class NavigationBarFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nav_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup(R.id.navbar, -1)
    }

    fun setup(id: Int, selected: Int){
        val nav = requireView().findViewById<BottomNavigationView>(id)
        nav.selectedItemId = selected
        nav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_bar_create -> {startActivity(MeetUpCreation::class.java); true}
                R.id.nav_bar_find -> {startActivity(MapsActivity::class.java); true}
                R.id.nav_bar_list -> {startActivity(MeetupListActivity::class.java); true}
                R.id.nav_bar_groups -> {startActivityGroup(); true}
                else -> false
            }
        }
    }

    private fun startActivity(cls: Class<out AppCompatActivity>){
        ContextCompat.startActivity(requireContext(), Intent(context, cls), null)
    }
    private fun startActivityGroup(){
        val intent = Intent(context, MeetupListActivity::class.java).apply {
            putExtra(SHOW_JOINED_ONLY,true)
        }
        ContextCompat.startActivity(requireContext(), intent, null)
    }
}