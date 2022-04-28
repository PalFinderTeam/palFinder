package com.github.palFinderTeam.palfinder.utils

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class PopupWindowTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var profileService: ProfileService

    @Before
    fun init_() {
        hiltRule.inject()
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(null)
    }

    @Test
    fun testGroupPopup(){

        val intent = Intent(getApplicationContext(), MainNavActivity::class.java)

        val scenario = ActivityScenario.launch<MainNavActivity>(intent)

        init()
        onView(withId(R.id.nav_bar_groups)).perform(click())
        onView(withId(R.id.continue_warning_button))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(click())


        intended(hasComponent(LoginActivity::class.java.name))
        release()
    }

    @Test
    fun testCreatePopup(){

        val intent = Intent(getApplicationContext(), MainNavActivity::class.java)

        val scenario = ActivityScenario.launch<MainNavActivity>(intent)

        init()
        onView(withId(R.id.nav_bar_create)).perform(click())
        onView(withId(R.id.continue_warning_button))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(click())

        intended(hasComponent(LoginActivity::class.java.name))
        release()
    }


}