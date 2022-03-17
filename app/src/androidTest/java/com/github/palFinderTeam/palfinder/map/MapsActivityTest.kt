package com.github.palFinderTeam.palfinder.map

import android.icu.util.Calendar
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.maps.model.LatLng
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class MapsActivityTest {

    @get:Rule
    val testRule = ActivityScenarioRule(MapsActivity::class.java)
    @get:Rule
    var fineLocationPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)
    @get:Rule
    var coarseLocationPermissionRule : GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    private val utils = MapsActivity.utils


    @Test
    fun testMarkerClick(){
        val latch = CountDownLatch(1)


        val id = "id"
        val lat = 15.0
        val long = -15.0

        val date1 = Calendar.getInstance()
        date1!!.set(2022, 2,1,0,0,0)
        val date2 = Calendar.getInstance()
        date2!!.set(2022, 2,1,1,0,0)

        val meetup = MeetUp(
            id,
            ProfileUser("", "tempUser4", "user4", date2),
            "",
            "meetUp4Name",
            "meetUp4Description",
            date1,
            date2,
            Location(long, lat),
            emptySet(),
            false,
            42,
            mutableListOf(ProfileUser("", "tempUser2", "tempUser2", date2))
        )



        utils.addMeetupMarker(meetup)

        Intents.init()
        utils.setCameraPosition(LatLng(lat, long))
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val marker = device.findObject(UiSelector().descriptionContains("Google Map").childSelector(UiSelector().descriptionContains(id)))
        marker.waitForExists(1000)
        marker.click()
        intended(IntentMatchers.hasExtra(MEETUP_SHOWN, meetup))
        Intents.release()
    }


}
