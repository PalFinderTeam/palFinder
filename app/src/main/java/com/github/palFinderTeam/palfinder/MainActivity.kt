package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.map.MapsFragment
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.github.palFinderTeam.palfinder.notification.NotificationHandler
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

const val EXTRA_MESSAGE = "com.github.palFinderTeam.palFinder.MESSAGE"
const val USER_ID = "com.github.palFinderTeam.palFinder.USER_ID"

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
            R.id.miSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
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
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra(USER_ID, "VqcgAHtrm7hmPmSqfC5eGayy1jY2")
        }
        startActivity(intent)
    }

    fun goToProfileCat(view: View?) {
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra(USER_ID, "Ze3Wyf0qgVaR1xb9BmOqPmDJsYd2")
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun seeList(view: View?) {
        val intent = Intent(this, MeetupListActivity::class.java)
        startActivity(intent)
    }

    fun accessMap(view: View?) {
        val intent = Intent(this, MapsFragment::class.java).apply {  }
        startActivity(intent)
    }

    fun editCatAccount(view: View?) {
        val intent = Intent(this, UserSettingsActivity::class.java).apply {
            putExtra(USER_ID, "Ze3Wyf0qgVaR1xb9BmOqPmDJsYd2")
        }
        startActivity(intent)
    }

    fun accessCreateProfile(view: View?) {
        val intent = Intent(this, UserSettingsActivity::class.java).apply {  }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun notif(view: View){
        NotificationHandler(this).post("test", "content", R.drawable.icon_beer)
    }
    
}
