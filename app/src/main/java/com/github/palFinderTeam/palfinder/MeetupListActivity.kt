package com.github.palFinderTeam.palfinder

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import com.github.palFinderTeam.palfinder.meetups.MeetUpDumb


class MeetupListActivity : AppCompatActivity() {
    private lateinit var listOfMeetup : List<MeetUpDumb>
    private lateinit  var meetupList : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        meetupList = findViewById(R.id.meetup_layout)
        listOfMeetup = intent.getSerializableExtra("MEETUPS") as List<MeetUpDumb>

        for (meetup : MeetUpDumb in listOfMeetup) {
            addMeetup(meetup)
        }
    }

    private fun addMeetup(meetup : MeetUpDumb) {
        val v: View = layoutInflater.inflate(R.layout.meetup_listview, null)

        val text: TextView = v.findViewById(R.id.meetup_title)
        text.text = meetup.name

        meetupList.addView(v)
    }
}