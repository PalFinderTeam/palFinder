package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.TempUser
import com.github.palFinderTeam.palfinder.utils.askTime

@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
class MeetUpCreation : AppCompatActivity() {
    private val model: MeetUpCreationViewModel by viewModels()

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
            }
        }
    }
    fun onEndTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            findViewById<TextView>(R.id.tv_EndDate).apply {
                val format = SimpleDateFormat(getString(R.string.date_long_format))
                text = format.format(it.time)
                model.startDate = it
            }
        }
    }
    fun onDone(v: View){
        val m = MeetUp(
            TempUser(null, "dummy"),
            null,
            findViewById<TextView>(R.id.et_EventName).text.toString(),
            findViewById<TextView>(R.id.et_Description).text.toString(),
            model.startDate,
            model.endDate,
            Location("0.0,0.0"),
            emptyList(),
            0,
            emptyList()
        )
    }
}