package com.github.palFinderTeam.palfinder.map

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class MapsActivityTest {


    @Inject
    lateinit var meetUpRepository: MeetUpRepository
    @Inject
    lateinit var profileService: ProfileService


    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var fineLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    var coarseLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)


    lateinit var utils: MapListViewModel

    @Before
    fun init_() {
        hiltRule.inject()
        utils = MapListViewModel(meetUpRepository)
    }


/*
    @Test
    fun testMarkerClick() = runTest() {

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

        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).db[meetup.uuid] = meetup
        utils.refresh()

        scenario.use {

            Intents.init()

            utils.setCameraPosition(LatLng(lat, long))
            utils.updateFetcherLocation(LatLng(lat, long))


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
    }*/




    @Test
    fun testSelectLocation(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        val basePosition = LatLng(42.0, 42.0)
        intent.apply {
            putExtra(LOCATION_SELECT, basePosition)
        }
        val scenario = ActivityScenario.launch<MapsActivity>(intent)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.desc("MAP READY")), 1000)


        scenario.use{
            utils.setCameraPosition(basePosition)
            val marker = device.findObject(
                UiSelector().descriptionContains("Google Map")
                    .childSelector(UiSelector().descriptionContains("Here"))
            )

            Assert.assertNotNull(marker)

            onView(withId(R.id.bt_locationSelection)).perform(click())

            Assert.assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
        }
    }
}
