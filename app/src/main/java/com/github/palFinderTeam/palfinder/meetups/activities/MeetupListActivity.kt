package com.github.palFinderTeam.palfinder.meetups.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.utils.searchedFilter
import java.util.*


class MeetupListActivity : AppCompatActivity() {
    private lateinit  var meetupList : RecyclerView
    private lateinit var listOfMeetup : List<MeetUp>


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        listOfMeetup = intent.getSerializableExtra("MEETUPS") as ArrayList<MeetUp>
        meetupList = findViewById(R.id.meetup_list_recycler)


        for (meetup : MeetUp in listOfMeetup.sortedBy { it.capacity}) {
            addMeetup(meetup)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addMeetup(meetup : MeetUp) { //TODO - take into account real values
        val v: View = layoutInflater.inflate(R.layout.meetup_listview, null)
        var text: TextView = v.findViewById(R.id.meetup_title)
        text.text = meetup.name

        text = v.findViewById(R.id.date)
        text.text = timeDiff(meetup.startDate, Calendar.getInstance())

        text = v.findViewById(R.id.meetup_description)
        text.text = meetup.description

        text = v.findViewById(R.id.number_participants)
        text.text = meetup.capacity.toString() + " / 50"

        meetupList.addView(v)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun timeDiff(start: android.icu.util.Calendar, end: Calendar) : String {
        val diff: Long = end.timeInMillis - start.timeInMillis
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        if (hours < 24) {
            return "commence dans " + hours + " heures"
        } else {
            return "commence dans " + (hours/24 + 1) + " jours"
        }
    }

    fun sortByCap(view: View?) {
        meetupList.removeAllViews()
        for (meetup : MeetUp in listOfMeetup.sortedBy { it.capacity}) {
            addMeetup(meetup)
        }
    }

    fun sortByName(view: View?) {
        meetupList.removeAllViews()
        for (meetup : MeetUp in listOfMeetup.sortedBy { it.name.lowercase()}) {
            addMeetup(meetup)
        }
    }
}