package com.github.palFinderTeam.palfinder.meetups.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.fragment.picker.DatePickerFragment
import com.github.palFinderTeam.palfinder.fragment.picker.TimePickerFragment
import com.github.palFinderTeam.palfinder.utils.SimpleDate

class MeetUpCreation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_creation)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onTimeSelectButton(v: View){
        val dateFrag = DatePickerFragment()
        val timeFrag = TimePickerFragment()
        var date: SimpleDate? = null

        dateFrag.show(supportFragmentManager, "datePicker")
        dateFrag.value.thenAccept {
            timeFrag.show(supportFragmentManager, "timePicker")
            date = it
        }
        timeFrag.value.thenAccept{
            findViewById<TextView>(R.id.StartTime).apply {
                text = date!!.withTime(it).toString()
            }
        }
    }
}