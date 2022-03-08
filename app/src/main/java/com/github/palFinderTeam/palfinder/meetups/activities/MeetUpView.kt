package com.github.palFinderTeam.palfinder.meetups.activities

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp


const val MEETUP_SHOWN = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_SHOWN"

class MeetUpView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_view)

        val meetup = intent.getSerializableExtra(MEETUP_SHOWN) as MeetUp
        fillFields(meetup)
    }
    private fun fillFields(meetup: MeetUp){
        findViewById<TextView>(R.id.tv_ViewEventName).apply { text = meetup.name }
        findViewById<TextView>(R.id.tv_ViewEventDescritpion).apply { text = meetup.description }
        findViewById<TextView>(R.id.tv_ViewEndDate).apply { text = meetup.description }

        val format = SimpleDateFormat(getString(R.string.date_long_format))
        val startDate = format.format(meetup.startDate.time)
        val endDate = format.format(meetup.startDate.time)

        findViewById<TextView>(R.id.tv_ViewStartDate).apply { text = startDate }
        findViewById<TextView>(R.id.tv_ViewEndDate).apply { text = endDate }
    }
}