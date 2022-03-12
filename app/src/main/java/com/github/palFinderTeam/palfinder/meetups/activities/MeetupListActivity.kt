package com.github.palFinderTeam.palfinder.meetups.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetupListAdapter
import com.github.palFinderTeam.palfinder.utils.searchedFilter
import java.util.*


class MeetupListActivity : AppCompatActivity() {
    private lateinit  var meetupList : RecyclerView
    private lateinit var listOfMeetup : List<MeetUp>
    private lateinit var adapter: MeetupListAdapter<MeetUp>


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        listOfMeetup = intent.getSerializableExtra("MEETUPS") as ArrayList<MeetUp>
        Log.d("meetup", listOfMeetup.toString())
        meetupList = findViewById(R.id.meetup_list_recycler)
        meetupList.layoutManager = LinearLayoutManager(this)
        adapter = MeetupListAdapter<MeetUp>(listOfMeetup)
        meetupList.adapter = adapter

        val searchField = findViewById<SearchView>(R.id.search_list)
        searchField.imeOptions = EditorInfo.IME_ACTION_DONE
        searchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }

        })


    }

    fun sortByCap(): List<MeetUp> {
        adapter.currentDataSet.clear()
        adapter.currentDataSet.addAll(listOfMeetup.sortedBy { it.capacity })
        adapter.notifyDataSetChanged()
        return adapter.currentDataSet
    }

    fun sortByName(): List<MeetUp> {
        adapter.currentDataSet.clear()
        adapter.currentDataSet.addAll(listOfMeetup.sortedBy { it.name.lowercase()})
        adapter.notifyDataSetChanged()
        return adapter.currentDataSet
    }

    fun getAdapter(): MeetupListAdapter<MeetUp> {
        return adapter
    }
}