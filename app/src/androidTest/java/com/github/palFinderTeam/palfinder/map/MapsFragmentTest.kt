package com.github.palFinderTeam.palfinder.map

import android.icu.util.Calendar
import android.os.Bundle
import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.ShowParam
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.UIMockTimeServiceModule
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.launchFragmentInHiltContainer
import com.github.palFinderTeam.palfinder.utils.onHiltFragment
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.closeTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.math.roundToInt


@ExperimentalCoroutinesApi
@HiltAndroidTest
class MapsFragmentTest {


    @Inject
    lateinit var meetUpRepository: MeetUpRepository

    @Inject
    lateinit var profileService: ProfileService

    @Inject
    lateinit var timeService: TimeService


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
        val dateNow = Calendar.getInstance()
        dateNow.set(2022, 2, 0)
        (timeService as UIMockTimeServiceModule.UIMockTimeService).setDate(dateNow)
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID("0000")
    }


    @ExperimentalCoroutinesApi
    @Test
    fun testMarkerClick() = runTest {
        val lat = 15.0
        val long = -15.0

        val date1 = Calendar.getInstance()
        date1!!.set(2024, 2, 1, 0, 0, 0)
        val date2 = Calendar.getInstance()
        date2!!.set(2025, 2, 1, 1, 0, 0)

        val meetup = MeetUp(
            "",
            "user4",
            ImageInstance(""),
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

        val id = meetUpRepository.create(meetup)
        assertThat(id, `is`(notNullValue()))

        val scenario = launchFragmentInHiltContainer<MapsFragment>(Bundle().apply {
            putSerializable(
                "Context",
                MapsFragment.Context.MARKER
            )
        })
        scenario!!.use {
            scenario.onHiltFragment<MapsFragment> {
                it.viewModel.setSearchParameters(location = meetup.location, showParam = ShowParam.ALL)
                it.viewModel.fetchMeetUps()
                it.setMapLocation(meetup.location, instantaneous = true)
                it.viewModel.firstInit()
            }
            init()

            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.wait(Until.hasObject(By.desc("MAP READY")), 1000)
            val marker = device.findObject(UiSelector().descriptionContains(id))
            marker.waitForExists(1000)
            assertThat(marker, `is`(notNullValue()))
            marker.click()
            intended(IntentMatchers.hasExtra(MEETUP_SHOWN, id))
            release()
        }
    }

    @Test
    fun onMarkerClickDoesNothingWhenSelecting() {

        val scenario = launchFragmentInHiltContainer<MapsFragment>(Bundle().apply {
            putSerializable(
                "Context",
                MapsFragment.Context.SELECT_LOCATION
            )
        })

        scenario.use {

            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.wait(Until.hasObject(By.desc("MAP READY")), 1000)

            onView(withId(R.id.map_content)).perform(click())

            val marker = device.findObject(
                UiSelector().descriptionContains("Here")
            )

            marker.waitForExists(1000)
            assertThat(marker, `is`(notNullValue()))

            init()
            marker.click()


            assertThat(getIntents().size, `is`(0))
            release()

        }
    }

    @Test
    fun selectionWithStartingValueShouldFocusOnIt() {
        val scenario = launchFragmentInHiltContainer<MapsFragment>(Bundle().apply {
            putSerializable(
                "Context",
                MapsFragment.Context.SELECT_LOCATION
            )
            putSerializable(
                "StartSelection",
                Location(69.0, 69.0)
            )
        })

        scenario.use {

            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.wait(Until.hasObject(By.desc("MAP READY")), 1000)

            val marker = device.findObject(
                UiSelector().descriptionContains("Here")
            )

            marker.waitForExists(1000)
            assertThat(marker, `is`(notNullValue()))
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

            onView(withId(R.id.map_content)).perform(click())

            val marker = device.findObject(
                UiSelector().descriptionContains("Google Map")
                    .childSelector(UiSelector().descriptionContains("Here"))
            )

            marker.waitForExists(1000)
            assertThat(marker, `is`(notNullValue()))
        }
    }

    @Test
    fun canSearchOnMap() {
        val scenario = launchFragmentInHiltContainer<MapsFragment>(Bundle().apply {
            putSerializable(
                "Context",
                MapsFragment.Context.SELECT_LOCATION
            )
        })
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.desc("MAP READY")), 1000)

        scenario!!.use {
            onView(withId(R.id.search_on_map)).perform(click(), typeText("Delhi"))
                .perform(pressKey(KeyEvent.KEYCODE_ENTER))


            scenario.onHiltFragment<MapsFragment> {
                assertThat(it.viewModel.searchLocation.value?.latitude?.roundToInt(), `is`(29))
                assertThat(it.viewModel.searchLocation.value?.longitude?.roundToInt(), `is`(77))
            }
            onView(withId(R.id.search_on_map)).perform(click())
                .perform(pressKey(KeyEvent.KEYCODE_ENTER))
            onView(withId(R.id.search_on_map)).perform(click(), typeText("invalid_location"))
                .perform(pressKey(KeyEvent.KEYCODE_ENTER))
        }
    }

    @Test
    fun setAndRetrieveLocationFromParentViewIsValid() {
        val scenario = launchFragmentInHiltContainer<MapsFragment>(Bundle().apply {
            putSerializable(
                "Context",
                MapsFragment.Context.SELECT_LOCATION
            )
        })
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.desc("MAP READY")), 1000)

        scenario!!.use {
            scenario.onHiltFragment<MapsFragment> {
                it.setMapLocation(Location(1.0, 2.0), instantaneous = true)
                val location = it.getMapLocation()
                assertThat(location.longitude, `is`(closeTo(1.0, 1e-1)))
                assertThat(location.latitude, `is`(closeTo(2.0, 1e-1)))
            }
        }
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
                val mapFrag =
                    it.supportFragmentManager.findFragmentById(android.R.id.content) as MapsFragment
                assertThat(mapFrag.getMapType(), `is`(GoogleMap.MAP_TYPE_NORMAL))
            }
            onView(withId(R.id.bt_changeMapType)).perform(click())
            scenario.onActivity {
                val mapFrag =
                    it.supportFragmentManager.findFragmentById(android.R.id.content) as MapsFragment
                assertThat(mapFrag.getMapType(), `is`(GoogleMap.MAP_TYPE_HYBRID))
            }
            onView(withId(R.id.bt_changeMapType)).perform(click())
            scenario.onActivity {
                val mapFrag =
                    it.supportFragmentManager.findFragmentById(android.R.id.content) as MapsFragment
                assertThat(mapFrag.getMapType(), `is`(GoogleMap.MAP_TYPE_NORMAL))
            }
        }
    }
}
