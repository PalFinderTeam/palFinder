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
        model.meetUp = meetup

        fillFields(meetup)
    }

    private fun setTextView(id: Int, value: String){
        findViewById<TextView>(id).apply { this.text = value }
    }

    private fun fillFields(meetup: MeetUp){
        val format = SimpleDateFormat(getString(R.string.date_long_format))
        val startDate = format.format(meetup.startDate.time)
        val endDate = format.format(meetup.endDate.time)

        setTextView(R.id.tv_ViewEventName,meetup.name)
        setTextView(R.id.tv_ViewEventDescritpion,meetup.description)
        setTextView(R.id.tv_ViewEventCreator,
            getString(R.string.meetup_view_creator, meetup.creator.name))

        setTextView(R.id.tv_ViewStartDate, startDate)
        setTextView(R.id.tv_ViewEndDate,endDate)
    }

    fun onEdit(v: View){
        val intent = Intent(this, MeetUpCreation::class.java).apply {
            putExtra(MEETUP_EDIT, model.meetUp)
        }
        startActivity(intent)
    }
}