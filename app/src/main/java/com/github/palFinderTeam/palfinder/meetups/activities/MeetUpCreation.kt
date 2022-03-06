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
import com.github.palFinderTeam.palfinder.utils.askTime
import java.util.*

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
class MeetUpCreation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_creation)

        // Filles Date
        findViewById<TextView>(R.id.StartTime).apply {
            val format = SimpleDateFormat(getString(R.string.date_long_format))
            text = format.format(Calendar.getInstance())
        }
        findViewById<TextView>(R.id.EndTime).apply {
            val format = SimpleDateFormat(getString(R.string.date_long_format))
            text = format.format(Calendar.getInstance())
        }
    }

    fun onStartTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            findViewById<TextView>(R.id.StartTime).apply {
                val format = SimpleDateFormat(getString(R.string.date_long_format))
                text = format.format(it.time)
            }
        }
    }
    fun onEndTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            findViewById<TextView>(R.id.EndTime).apply {
                val format = SimpleDateFormat(getString(R.string.date_long_format))
                text = format.format(it.time)
            }
        }
    }
}