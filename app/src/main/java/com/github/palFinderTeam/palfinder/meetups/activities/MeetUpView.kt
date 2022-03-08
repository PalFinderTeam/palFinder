package com.github.palFinderTeam.palfinder.meetups.activities

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
        fillField(meetup)
    }
    fun fillField(meetup: MeetUp){
        findViewById<TextView>(R.id.tv_ViewEventName).apply { text = meetup.name }
        findViewById<TextView>(R.id.tv_ViewEventDescritpion).apply { text = meetup.description }
    }
}