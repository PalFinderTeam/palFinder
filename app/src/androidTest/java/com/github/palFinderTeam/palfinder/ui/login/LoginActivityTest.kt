package com.github.palFinderTeam.palfinder.ui.login

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity.Companion.HIDE_ONE_TAP
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class LoginActivityTest {

    @Inject
    lateinit var profileService: ProfileService

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var auth: FirebaseAuth

    @Before
    fun setup() {
        hiltRule.inject()
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
        auth = FirebaseAuth.getInstance()
        auth.signOut()
    }

    @Test
    fun createNewAccountPath() = runTest {
        val intent = Intent(getApplicationContext(), LoginActivity::class.java).apply {
            putExtra(HIDE_ONE_TAP, true)
        }
        val scenario = ActivityScenario.launch<LoginActivity>(intent)
        scenario.use {
            onView(withId(R.id.email)).perform(typeText("michel@michel.com"))
            closeSoftKeyboard()
            onView(withId(R.id.password)).perform(typeText("123456"))
            closeSoftKeyboard()
            Intents.init()
            onView(withId(R.id.login)).perform(click())
        }
    }


}