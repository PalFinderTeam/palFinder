package com.github.palFinderTeam.palfinder

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.meetups.MeetUpDumb
import java.util.*


class MeetupListActivity : AppCompatActivity() {
    private lateinit var listOfMeetup : List<MeetUpDumb>
    private lateinit  var meetupList : LinearLayout

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        meetupList = findViewById(R.id.meetup_layout)
        listOfMeetup = intent.getSerializableExtra("MEETUPS") as List<MeetUpDumb>

        for (meetup : MeetUpDumb in listOfMeetup) {
            addMeetup(meetup)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addMeetup(meetup : MeetUpDumb) { //TODO - take into account real values
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
}