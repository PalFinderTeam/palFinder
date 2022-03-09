package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp


const val MEETUP_SHOWN = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_SHOWN"

@SuppressLint("SimpleDateFormat")
class MeetUpView : AppCompatActivity() {
    private val model: MeetUpViewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_view)

        val meetup = intent.getSerializableExtra(MEETUP_SHOWN) as MeetUp
        fillFields(meetup)
        model.meetUp = meetup
    }

    private fun fillFields(meetup: MeetUp){
        // Set Name Field
        findViewById<TextView>(R.id.tv_ViewEventName).apply { text = meetup.name }

        // Set Description Field
        findViewById<TextView>(R.id.tv_ViewEventDescritpion).apply { text = meetup.description }

        // Set Creator Field
        findViewById<TextView>(R.id.tv_ViewEventCreator).apply {
            text = getString(R.string.meetup_view_creator, meetup.creator.name)
        }

        val format = SimpleDateFormat(getString(R.string.date_long_format))
        val startDate = format.format(meetup.startDate.time)
        val endDate = format.format(meetup.startDate.time)

        // Set Start Date Field
        findViewById<TextView>(R.id.tv_ViewStartDate).apply { text = startDate }

        // Set End Date Field
        findViewById<TextView>(R.id.tv_ViewEndDate).apply { text = endDate }
    }

    fun onEdit(v: View){
        val intent = Intent(this, MeetUpCreation::class.java).apply {
            putExtra(MEETUP_EDIT, model.meetUp)
        }
        startActivity(intent)
    }
}