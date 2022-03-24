package com.github.palFinderTeam.palfinder.navbar

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.init
import androidx.test.espresso.intent.Intents.release
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.github.palFinderTeam.palfinder.MainActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.MapsActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavBarTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun testCreateButton(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        Espresso.onView(
            ViewMatchers.withId(R.id.nav_bar_create)
        ).perform(ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(MeetUpCreation::class.java.name))
        release()
    }

    @Test
    fun testMapButton(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        Espresso.onView(
            ViewMatchers.withId(R.id.nav_bar_find)
        ).perform(ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(MapsActivity::class.java.name))
        release()
    }

    @Test
    fun testGroupButton(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        Espresso.onView(
            ViewMatchers.withId(R.id.nav_bar_groups)
        ).perform(ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(MeetupListActivity::class.java.name))
        release()
    }
}