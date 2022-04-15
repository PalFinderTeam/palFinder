package com.github.palFinderTeam.palfinder.map

import android.icu.util.Calendar
import android.os.Bundle
import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.UIMockMeetUpRepositoryModule
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.launchFragmentInHiltContainer
import com.github.palFinderTeam.palfinder.utils.onHiltFragment
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class MapsFragmentTest {


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


    @Before
    fun init_() {
        hiltRule.inject()
    }


    @ExperimentalCoroutinesApi
    @Test
    fun testMarkerClick() = runTest {
        val lat = 15.0
        val long = -15.0

        val date1 = Calendar.getInstance()
        date1!!.set(2022, 2, 1, 0, 0, 0)
        val date2 = Calendar.getInstance()
        date2!!.set(2022, 2, 1, 1, 0, 0)

        val meetup = MeetUp(
            "",
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

        val id = meetUpRepository.createMeetUp(meetup)
        assertThat(id, `is`(notNullValue()))

        val scenario = launchFragmentInHiltContainer<MapsFragment>(Bundle().apply {
            putSerializable(
                "Context",
                MapsFragment.Context.MARKER
            )
        })
        scenario!!.use {
            scenario.onHiltFragment<MapsFragment> {
                it.viewModel.useUserLocation.value = false
                it.viewModel.showOnlyJoined = false
                it.viewModel.searchLocation.value = meetup.location
                it.viewModel.fetchMeetUps()
                it.setMapLocation(meetup.location)
            }
            Intents.init()

            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.wait(Until.hasObject(By.desc("MAP READY")), 1000)
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
    fun testSelectLocation() {
        val scenario = launchFragmentInHiltContainer<MapsFragment>(Bundle().apply {
            putSerializable(
                "Context",
                MapsFragment.Context.SELECT_LOCATION
            )
        })

        scenario.use {

            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.wait(Until.hasObject(By.desc("MAP READY")), 1000)

            onView(withId(R.id.map_tab)).perform(click())

            val marker = device.findObject(
                UiSelector().descriptionContains("Google Map")
                    .childSelector(UiSelector().descriptionContains("Here"))
            )



            Assert.assertNotNull(marker)
        }


        //onView(withId(R.id.bt_locationSelection)).perform(click())
        // Assert.assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
    }

    @Test
    fun canSearchOnMap() {
        launchFragmentInHiltContainer<MapsFragment>(Bundle().apply {
            putSerializable(
                "Context",
                MapsFragment.Context.SELECT_LOCATION
            )
        })
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.desc("MAP READY")), 1000)

        onView(withId(R.id.search_on_map)).perform(click(), typeText("Delhi"))
            .perform(pressKey(KeyEvent.KEYCODE_ENTER))
/*
                Assert.assertEquals(29, it.viewModel.getCameraPosition().latitude.roundToInt());
                Assert.assertEquals(77, it.viewModel.getCameraPosition().longitude.roundToInt());
*/
        onView(withId(R.id.search_on_map)).perform(click())
            .perform(pressKey(KeyEvent.KEYCODE_ENTER))
        onView(withId(R.id.search_on_map)).perform(click(), typeText("invalid_location"))
            .perform(pressKey(KeyEvent.KEYCODE_ENTER))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun canChangeMapType() {
        //val intent = Intent(ApplicationProvider.getApplicationContext(), MapsFragment::class.java)
        val scenario = launchFragmentInHiltContainer<MapsFragment>(Bundle().apply {
            putSerializable(
                "Context",
                MapsFragment.Context.MARKER
            )
        })

        scenario?.use {
            scenario.onActivity {
                val mapFrag = it.supportFragmentManager.findFragmentById(android.R.id.content) as MapsFragment
                assertThat(mapFrag.getMapType(), `is`(GoogleMap.MAP_TYPE_NORMAL))
            }
            onView(withId(R.id.bt_changeMapType)).perform(click())
            scenario.onActivity {
                val mapFrag = it.supportFragmentManager.findFragmentById(android.R.id.content) as MapsFragment
                assertThat(mapFrag.getMapType(), `is`(GoogleMap.MAP_TYPE_HYBRID))
            }
            onView(withId(R.id.bt_changeMapType)).perform(click())
            scenario.onActivity {
                val mapFrag = it.supportFragmentManager.findFragmentById(android.R.id.content) as MapsFragment
                assertThat(mapFrag.getMapType(), `is`(GoogleMap.MAP_TYPE_NORMAL))
            }
        }
    }
}
