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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

import com.github.palFinderTeam.palfinder.map.MapActivity
import com.github.palFinderTeam.palfinder.tag.example.TagShowcaseActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.map.MapsActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.Serializable

const val EXTRA_MESSAGE = "com.github.palFinderTeam.palFinder.MESSAGE"
const val DUMMY_USER = "com.github.palFinderTeam.palFinder.DUMMY_PROFILE_USER"

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miLogout) {
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

    fun openMeetupCreationPage(view: View?) {
        val intent = Intent(this, MeetUpCreation::class.java).apply {
        }
        startActivity(intent)
    }

    fun goToProfile(view: View?) {
        // Create a fake user for demo
        val joinDate = Calendar.getInstance()
        joinDate.set(2022, 2, 6, 14, 1, 0)
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra(
                DUMMY_USER,
                ProfileUser("gerussi", "Louca", "Gerussi", joinDate) as Serializable
            )
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun seeList(view: View?) {
        val intent = Intent(this, MeetupListActivity::class.java)
        startActivity(intent)
    }

    fun accessMap(view: View?) {
        val intent = Intent(this, MapActivity::class.java).apply {  }
        startActivity(intent)
    }
}
