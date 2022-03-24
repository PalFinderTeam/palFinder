package com.github.palFinderTeam.palfinder.navbar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.MapsActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
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
        setup(R.id.nav_bar, -1)
    }

    fun setup(id: Int, selected: Int){
        val nav = requireView().findViewById<BottomNavigationView>(id)
        nav.selectedItemId = selected
        nav.setOnItemSelectedListener {
            onSelect(it, requireContext())
        }
    }

    private fun onSelect(item: MenuItem, context: Context): Boolean{
        return when(item.itemId){
            R.id.nav_bar_create -> {onCreateMeetup(context); true}
            R.id.nav_bar_find -> {onFindMeetup(context); true}
            R.id.nav_bar_groups -> {onGroups(context); true}
            else -> false
        }
    }

    fun onCreateMeetup(context: Context){
        val intent = Intent(context, MeetUpCreation::class.java)
        ContextCompat.startActivity(context, intent, null)
    }

    fun onFindMeetup(context: Context){
        val intent = Intent(context, MapsActivity::class.java)
        ContextCompat.startActivity(context, intent, null)
    }

    fun onGroups(context: Context){
        val intent = Intent(context, MeetupListActivity::class.java)
        ContextCompat.startActivity(context, intent, null)
    }
}