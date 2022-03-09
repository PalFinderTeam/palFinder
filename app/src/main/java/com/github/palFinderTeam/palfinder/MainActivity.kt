package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.map.MapsActivity

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

    fun accessMap(view: View?) {
        val intent = Intent(this, MapsActivity::class.java).apply {  }
        startActivity(intent)
    }
}