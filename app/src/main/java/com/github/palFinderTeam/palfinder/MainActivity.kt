package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.tag.example.TagShowcaseActivity

const val EXTRA_MESSAGE = "com.github.palFinderTeam.palFinder.MESSAGE"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.mainGoToTagButton).setOnClickListener {
            val intent = Intent(this, TagShowcaseActivity::class.java)
            startActivity(intent)
        }
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
}