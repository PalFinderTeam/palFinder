package com.github.palFinderTeam.palfinder

import android.icu.util.Calendar
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiSelector
import androidx.test.espresso.intent.Intents
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.github.palFinderTeam.palfinder.map.MapsActivity
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.TempUser
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.google.android.gms.maps.model.Marker
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapsActivityTest {

    @get:Rule
    val testRule = ActivityScenarioRule(MapsActivity::class.java)


    @Test
    fun testClickOnMarker(){
        val uuid = "someuuid"
        val date1 = Calendar.getInstance()
        date1!!.set(2022, 2,1,0,0,0)
        val date2 = Calendar.getInstance()
        date2!!.set(2022, 2,1,1,0,0)
        val meetup = MeetUp(
            uuid,
            TempUser("", "Bob"),
            "",
            "title",
            "eventDescription",
            date1,
            date2,
            com.github.palFinderTeam.palfinder.utils.Location(0.0, 0.0),
            emptyList(),
            true,
            2,
            mutableListOf(TempUser("", "Alice"))
        )

        val marker:Marker = MapsActivity.addMeetUpMarker(meetup)
        Intents.init()
        val UIDevice = UiDevice.getInstance(getInstrumentation())
        val UIMarker = UIDevice.findObject(UiSelector().descriptionContains(uuid))
        UIMarker.click()
        intended(IntentMatchers.hasExtra(MEETUP_SHOWN, meetup))
    }
}