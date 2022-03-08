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

@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
class MeetUpCreation : AppCompatActivity() {
    private val model: MeetUpCreationViewModel by viewModels()
    private val defaultTimeDelta = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_creation)

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

    fun onStartTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            findViewById<TextView>(R.id.tv_StartDate).apply {
                val format = SimpleDateFormat(getString(R.string.date_long_format))
                text = format.format(it.time)
                model.startDate = it
                checkDateIntegrity()
            }
        }
    }
    fun onEndTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            findViewById<TextView>(R.id.tv_EndDate).apply {
                val format = SimpleDateFormat(getString(R.string.date_long_format))
                text = format.format(it.time)
                model.startDate = it
                checkDateIntegrity()
            }
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