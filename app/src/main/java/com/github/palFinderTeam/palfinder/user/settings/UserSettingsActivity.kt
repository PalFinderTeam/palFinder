package com.github.palFinderTeam.palfinder.user.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)
    }

}