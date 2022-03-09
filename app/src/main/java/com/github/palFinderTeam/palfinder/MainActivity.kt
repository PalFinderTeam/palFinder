package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.tag.example.TagShowcaseActivity
import com.github.palFinderTeam.palfinder.meetups.MeetUpDumb
import java.io.Serializable
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import java.util.*
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

const val EXTRA_MESSAGE = "com.github.palFinderTeam.palFinder.MESSAGE"
const val DUMMY_USER = "com.github.palFinderTeam.palFinder.DUMMY_PROFILE_USER"

class MainActivity : AppCompatActivity() {

    private companion object{
        private const val TAG = "MainActivity"
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.mainGoToTagButton).setOnClickListener {
            val intent = Intent(this, TagShowcaseActivity::class.java)
            startActivity(intent)
        }
        auth = Firebase.auth
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.miLogout){
            Log.i(TAG, "Logout")
            //Logout the user
            auth.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("341371843047-6i3a92lfmcb6555vsj9sb02tnhmkh4c8.apps.googleusercontent.com") //somehow cannot access value through google-service values.xml
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(this, gso)
            client.signOut()
            val logoutIntent = Intent(this, LoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        }

        return super.onOptionsItemSelected(item)

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

    fun goToProfile(view: View?) {
        // Create a fake user for demo
        val joinDate = Date(122, 2, 6, 14, 1, 0)
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra(DUMMY_USER, ProfileUser("gerussi", "Louca", "Gerussi", joinDate) as Serializable)
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun seeList(view: View?) {
        var c1 = Calendar.getInstance()
        c1.set(2022, 2, 6)
        var c2 = Calendar.getInstance()
        c2.set(2022, 1, 8)
        var c3 = Calendar.getInstance()
        c3.set(2022, 2, 1)
        var c4 = Calendar.getInstance()
        c4.set(2022, 0, 1)

        val meetups_list = listOf<MeetUpDumb>(
            MeetUpDumb(icon = null, name = "cuire des carottes",
                description = "nous aimerions bien nous atteler à la cuisson de carottes au beurre", startDate = c1,
                endDate = c2, location = null, tags = null, capacity = 45),
            MeetUpDumb(icon = null, name = "cuire des patates",
                description = "nous aimerions bien nous atteler à la cuisson de patates au beurre", startDate = c2,
                endDate = c1, location = null, tags = null, capacity = 48),
            MeetUpDumb(icon = null, name = "Street workout",
                description = "workout pepouse au pont chauderon", startDate = c3,
                endDate = c1, location = null, tags = null, capacity = 4),
            MeetUpDumb(icon = null, name = "Van Gogh Beaulieux",
                description = "Expo sans tableau c'est bo", startDate = c4,
                endDate = c1, location = null, tags = null, capacity = 15),
            MeetUpDumb(icon = null, name = "Palexpo",
                description = "popopo", startDate = c4,
                endDate = c2, location = null, tags = null, capacity = 18),
        )
        val intent = Intent(this, MeetupListActivity::class.java)
            .apply{
                putExtra("MEETUPS", meetups_list as Serializable)
            }
        startActivity(intent)
    }
}