package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.meetups.MeetUpDumb
import java.io.Serializable

const val EXTRA_MESSAGE = "com.github.palFinderTeam.palFinder.MESSAGE"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /** Called when the user taps the Send button  */
    fun sendMessage(view: View?) {
        val editText = findViewById<EditText>(R.id.mainName)
        val message = editText.text.toString()
        val intent = Intent(this, GreetingActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun seeList(view: View?) {
        var c1 = Calendar.getInstance()
        c1.set(2009, 8, 23)
        var c2 = Calendar.getInstance()
        c2.set(2009, 8, 25)

        val meetups_list = listOf<MeetUpDumb>(
            MeetUpDumb(icon = null, name = "cuire des carottes",
            description = "nous aimerions bien nous atteler à la cuisson de carottes au beurre", startDate = c1,
            endDate = c2, location = null, tags = null, capacity = 45)
        )
        val intent = Intent(this, MeetupListActivity::class.java)
            .apply{
                putExtra("MEETUPS", meetups_list as Serializable)
            }
        startActivity(intent)
    }
}