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
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NoAccountWarningTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun init_() {
        hiltRule.inject()
    }

    @Test
    fun testLoginPopup(){

        val intent = Intent(getApplicationContext(), LoginActivity::class.java)

        val scenario = ActivityScenario.launch<LoginActivity>(intent)

        init()
        scenario.use {
            onView(withId(R.id.noAccountButton)).perform(click())
            onView(withId(R.id.continue_warning_button)).perform(click())
        }

        intended(hasComponent(MainNavActivity::class.java.name))
        release()
    }

    @Test
    fun testLoginPopup(){

        val intent = Intent(getApplicationContext(), LoginActivity::class.java)

        val scenario = ActivityScenario.launch<LoginActivity>(intent)

        init()
        scenario.use {
            onView(withId(R.id.noAccountButton)).perform(click())
            onView(withId(R.id.continue_warning_button)).perform(click())
        }

        intended(hasComponent(MainNavActivity::class.java.name))
        release()
    }

    @Test
    fun testProfilePopup(){

        val intent = Intent(getApplicationContext(), MainNavActivity::class.java)

        val scenario = ActivityScenario.launch<LoginActivity>(intent)

        init()
        scenario.use {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext);
            onView(withId(R.id.miUserSettings)).perform(click())
            onView(withId(R.id.continue_warning_button)).perform(click())
        }

        intended(hasComponent(LoginActivity::class.java.name))
        release()
    }

    @Test
    fun testGroupPopup(){

        val intent = Intent(getApplicationContext(), MainNavActivity::class.java)

        val scenario = ActivityScenario.launch<LoginActivity>(intent)

        init()
        scenario.use {
            onView(withId(R.id.nav_bar_groups)).perform(click())
            onView(withId(R.id.continue_warning_button)).perform(click())
        }

        intended(hasComponent(LoginActivity::class.java.name))
        release()
    }

    @Test
    fun testCreatePopup(){

        val intent = Intent(getApplicationContext(), MainNavActivity::class.java)

        val scenario = ActivityScenario.launch<LoginActivity>(intent)

        init()
        scenario.use {
            onView(withId(R.id.nav_bar_create)).perform(click())
            onView(withId(R.id.continue_warning_button)).perform(click())
        }

        intended(hasComponent(LoginActivity::class.java.name))
        release()
    }
    

}