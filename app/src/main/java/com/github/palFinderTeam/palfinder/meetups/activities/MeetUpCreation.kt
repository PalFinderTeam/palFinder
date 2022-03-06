package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.TempUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.askTime

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
class MeetUpCreation : AppCompatActivity() {
    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_creation)

        // Filles Date
        findViewById<TextView>(R.id.tv_StartDate).apply {
            val format = SimpleDateFormat(getString(R.string.date_long_format))
            text = format.format(Calendar.getInstance())
        }
        findViewById<TextView>(R.id.tv_EndDate).apply {
            val format = SimpleDateFormat(getString(R.string.date_long_format))
            text = format.format(Calendar.getInstance())
        }
    }

    fun onStartTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            findViewById<TextView>(R.id.tv_StartDate).apply {
                val format = SimpleDateFormat(getString(R.string.date_long_format))
                text = format.format(it.time)
                startDate = it
            }
        }
    }
    fun onEndTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            findViewById<TextView>(R.id.tv_EndDate).apply {
                val format = SimpleDateFormat(getString(R.string.date_long_format))
                text = format.format(it.time)
                endDate = it
            }
        }
    }
    fun onDone(v: View){
        val m = MeetUp(
            TempUser(null, "dummy"),
            null,
            findViewById<TextView>(R.id.et_EventName).text.toString(),
            findViewById<TextView>(R.id.et_Description).text.toString(),
            startDate,
            endDate,
            Location(0.0,0.0),
            emptyList(),
            0,
            emptyList()
        )
    }
}