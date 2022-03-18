package com.github.palFinderTeam.palfinder

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * When creating the profile page, the username (which is unique) will
 * be sent to from the previous page as an intent. A database query will be made
 * and the user info will be sent back
 */
@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()
    companion object{
        const val EMPTY_FIELD = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (intent.hasExtra(USER_ID)) {
            val userId = intent.getStringExtra(USER_ID)!!
            viewModel.fetchProfile(userId)
        }
        viewModel.profile.observe(this) {
            injectUserInfo(it)
        }
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
            findViewById<TextView>(R.id.userProfileAboutTitle).text = this.resources.getString(R.string.no_desc)
            findViewById<TextView>(R.id.userProfileDescription).text = EMPTY_FIELD
        } else {
            findViewById<TextView>(R.id.userProfileDescription).text = bio
        }
    }


}