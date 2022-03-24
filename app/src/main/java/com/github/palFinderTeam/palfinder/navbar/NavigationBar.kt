package com.github.palFinderTeam.palfinder.navbar

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.MapsActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationBar {
    fun setup(fragment: Fragment, id: Int, selected: Int){
        val nav = fragment.requireView().findViewById<BottomNavigationView>(id)
        nav.selectedItemId = selected
        nav.setOnItemSelectedListener {
            onSelect(it, fragment.requireContext())
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
        startActivity(context, intent, null)
    }

    fun onFindMeetup(context: Context){
        val intent = Intent(context, MapsActivity::class.java)
        startActivity(context, intent, null)
    }

    fun onGroups(context: Context){
        val intent = Intent(context, MeetupListActivity::class.java)
        startActivity(context, intent, null)
    }
}