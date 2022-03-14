package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.Intent
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
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import java.io.Serializable
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
        adapter = MeetupListAdapter(listOfMeetup) { position -> onListItemClick(position) }
        meetupList.adapter = adapter

        val searchField = findViewById<SearchView>(R.id.search_list)
        searchField.imeOptions = EditorInfo.IME_ACTION_DONE
        SearchedFilter.setupSearchField(searchField, adapter.filter )


    }

    fun sortByCap(view: View?) {
        adapter.currentDataSet.clear()
        listOfMeetup = listOfMeetup.sortedBy { it.capacity }
        adapter.currentDataSet.addAll(listOfMeetup)
        adapter.notifyDataSetChanged()
    }

    fun sortByName(view: View?) {
        adapter.currentDataSet.clear()
        listOfMeetup = listOfMeetup.sortedBy { it.name.lowercase()}
        adapter.currentDataSet.addAll(listOfMeetup)
        adapter.notifyDataSetChanged()
    }

    private fun onListItemClick(position: Int) {
        val intent = Intent(this, MeetUpView::class.java)
            .apply { putExtra(MEETUP_SHOWN, listOfMeetup[position]) }
        startActivity(intent)
    }
}