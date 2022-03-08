package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.TempUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.askTime
import com.github.palFinderTeam.palfinder.utils.isBefore

const val MEETUP_EDIT = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_EDIT"

@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
class MeetUpCreation : AppCompatActivity() {
    private val model: MeetUpCreationViewModel by viewModels()
    private val defaultTimeDelta = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_creation)
        if (intent.hasExtra(MEETUP_EDIT)) {
            val meetup = intent.getSerializableExtra(MEETUP_EDIT) as MeetUp
            fillFields(meetup)
        } else {
            defaultFields()
        }
    }

    private fun defaultFields(){
        // Fills Date
        findViewById<TextView>(R.id.tv_StartDate).apply {
            val format = SimpleDateFormat(getString(R.string.date_long_format))
            text = format.format(model.startDate)
        }
        findViewById<TextView>(R.id.tv_EndDate).apply {
            val format = SimpleDateFormat(getString(R.string.date_long_format))
            text = format.format(model.endDate)
        }
    }

    private fun setStartDate(date: Calendar){
        findViewById<TextView>(R.id.tv_StartDate).apply {
            val format = SimpleDateFormat(getString(R.string.date_long_format))
            text = format.format(date.time)
            model.startDate = date
            checkDateIntegrity()
        }
    }
    private fun setEndDate(date: Calendar){
        findViewById<TextView>(R.id.tv_EndDate).apply {
            val format = SimpleDateFormat(getString(R.string.date_long_format))
            text = format.format(date.time)
            model.endDate = date
            checkDateIntegrity()
        }
    }

    private fun fillFields(meetUp: MeetUp){
        findViewById<TextView>(R.id.et_EventName).apply { this.text = meetUp.name }
        findViewById<TextView>(R.id.et_Description).apply { this.text = meetUp.description }
        setStartDate(meetUp.startDate)
        setEndDate(meetUp.endDate)
    }

    fun onStartTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            setStartDate(it)
        }
    }
    fun onEndTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            setEndDate(it)
        }
    }
    private fun checkDateIntegrity(){
        if (model.endDate.isBefore(model.startDate)){
            model.startDate = model.startDate
            model.startDate.add(Calendar.MINUTE, defaultTimeDelta)
        }
    }

    fun onDone(v: View){
        val m = MeetUp(
            "dummy",
            TempUser("", "dummy"),
            "",
            findViewById<TextView>(R.id.et_EventName).text.toString(),
            findViewById<TextView>(R.id.et_Description).text.toString(),
            model.startDate,
            model.endDate,
            Location(0.0,0.0),
            emptyList(),
            0,
            emptyList()
        )

        val intent = Intent(this, MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, m)
        }
        startActivity(intent)
    }
}