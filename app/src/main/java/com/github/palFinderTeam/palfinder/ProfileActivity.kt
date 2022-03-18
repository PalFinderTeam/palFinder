package com.github.palFinderTeam.palfinder

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import kotlinx.coroutines.launch

/**
 * When creating the profile page, the username (which is unique) will
 * be sent to from the previous page as an intent. A database query will be made
 * and the user info will be sent back
 */
class ProfileActivity : AppCompatActivity() {

    companion object{
        const val EMPTY_FIELD = ""
        const val NO_BIO_WARN = "No bio provided..."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        // Fetch user
        injectUserInfo(intent.getSerializableExtra(DUMMY_USER) as ProfileUser)
    }

    private fun injectUserInfo(user: ProfileUser) {
        findViewById<TextView>(R.id.userProfileName).text = user.fullName()
        findViewById<TextView>(R.id.userProfileUsername).text = user.atUsername()
        findViewById<TextView>(R.id.userProfileJoinDate).apply { text = user.prettyJoinTime() }
        injectBio(user.description)
        lifecycleScope.launch {
            user.pfp.loadImageInto(findViewById(R.id.userProfileImage))
        }
    }


    private fun injectBio(bio: String) {
        if (bio == EMPTY_FIELD) {
            findViewById<TextView>(R.id.userProfileAboutTitle).text = NO_BIO_WARN
            findViewById<TextView>(R.id.userProfileDescription).text = EMPTY_FIELD
        } else {
            findViewById<TextView>(R.id.userProfileDescription).text = bio
        }
    }


}