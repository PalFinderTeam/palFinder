package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.map.MapsActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.github.palFinderTeam.palfinder.navbar.NavigationBar
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
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
        private const val PROFILE_DESC = "Hello this is the Cat, but you can call me Kitty. I like to lay around and do nothing. Also you can see that my profile picture looks cure but in real life I am a real biach. Enjoy!"
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        NavigationBar.setup(this, R.id.nav_bar, R.id.nav_bar_find)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miLogout -> {
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
                return true
            }
            R.id.nav_bar_create -> {
                val intent = Intent(this, MeetUpCreation::class.java)
                startActivity(intent)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun openMeetupCreationPage(view: View?) {
        val intent = Intent(this, MeetUpCreation::class.java).apply {
        }
        startActivity(intent)
    }

    fun goToProfileLouca(view: View?) {
        // Create a fake user for demo
        val joinDate = Calendar.getInstance()
        joinDate.set(2022, 2, 6, 14, 1, 0)
        val pfp = ImageInstance("icons/demo_pfp.jpeg")
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra(DUMMY_USER, ProfileUser("gerussi", "Louca", "Gerussi", joinDate, pfp) as Serializable)
        }
        startActivity(intent)
    }

    fun goToProfileCat(view: View?) {
        val joinDate = Calendar.getInstance()
        joinDate.set(2022, 2, 1, 14, 1, 0)
        val pfp = ImageInstance("icons/cat.png")
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra(DUMMY_USER, ProfileUser("theCat", "Epic", "Cat", joinDate, pfp, PROFILE_DESC) as Serializable)
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun seeList(view: View?) {
        val intent = Intent(this, MeetupListActivity::class.java)
        startActivity(intent)
    }

    fun accessMap(view: View?) {
        val intent = Intent(this, MapsActivity::class.java).apply {  }
        startActivity(intent)
    }
}
