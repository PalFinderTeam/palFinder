package com.github.palFinderTeam.palfinder

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import java.util.*

/**
 * When creating the profile page, the username (which is unique) will
 * be sent to from the previous page as an intent. A database query will be made
 * and the user info will be sent back
 */
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Fetch user
        injectUserInfo(intent.getSerializableExtra(DUMMY_USER) as ProfileUser)
    }

    fun injectUserInfo(user: ProfileUser) {
        findViewById<TextView>(R.id.userProfileName).apply { text = user.getFullName() }
        findViewById<TextView>(R.id.userProfileUsername).apply { text = user.getAtUsername() }
        findViewById<TextView>(R.id.userProfileJoinDate).apply { text = user.getPrettyJoinTime() }
    }

}