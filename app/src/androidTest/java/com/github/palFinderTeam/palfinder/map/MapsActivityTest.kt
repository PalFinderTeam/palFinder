package com.github.palFinderTeam.palfinder.map

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MapsActivityTest {


    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var fineLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    var coarseLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    private val utils = MapsActivity.utils


    @Before
    fun init() {
        hiltRule.inject()
    }


    @Test
    fun testMarkerClick() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        val scenario = ActivityScenario.launch<MapsActivity>(intent)

        val id = "id"
        val lat = 15.0
        val long = -15.0

        val date1 = Calendar.getInstance()
        date1!!.set(2022, 2, 1, 0, 0, 0)
        val date2 = Calendar.getInstance()
        date2!!.set(2022, 2, 1, 1, 0, 0)

        val meetup = MeetUp(
            id,
            "user4",
            "",
            "meetUp4Name",
            "meetUp4Description",
            date1,
            date2,
            Location(long, lat),
            emptySet(),
            false,
            42,
            listOf("user2")
        )


        scenario.use {
            utils.addMeetupMarker(meetup)

            Intents.init()
            utils.setCameraPosition(LatLng(lat, long))


            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val marker = device.findObject(
                UiSelector().descriptionContains("Google Map")
                    .childSelector(UiSelector().descriptionContains(id))
            )
            marker.waitForExists(1000)
            marker.click()
            intended(IntentMatchers.hasExtra(MEETUP_SHOWN, id))
            Intents.release()
        }

    }



    @Test
    fun testSelectLocation(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        val basePosition = LatLng(42.0, 42.0)
        val selectedPosition = LatLng(40.0, 20.0)
        intent.apply {
            putExtra(LOCATION_SELECT, basePosition)
        }
        val scenario = ActivityScenario.launch<MapsActivity>(intent)

        scenario.use{
            utils.setCameraPosition(basePosition)
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val marker = device.findObject(
                UiSelector().descriptionContains("Google Map")
                    .childSelector(UiSelector().descriptionContains("Here"))
            )

            Assert.assertNotNull(marker)

            val expectedIntent = Intent().apply {
                putExtra(LOCATION_SELECTED, basePosition)
            }


            onView(withId(R.id.bt_locationSelection)).perform(click())

            val resultIntent = scenario.result.resultData

            Assert.assertEquals(basePosition, resultIntent.getParcelableExtra<LatLng>(
                LOCATION_SELECTED)!!)

        }



    }


}
