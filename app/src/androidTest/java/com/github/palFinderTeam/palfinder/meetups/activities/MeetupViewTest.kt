package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.UIMockMeetUpRepositoryModule
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class MeetupViewTest {

    private lateinit var meetup: MeetUp
    private val eventName = "dummy1"
    private val eventDescription = "dummy2"
    private lateinit var date1: Calendar
    private lateinit var date2: Calendar

    private val format = SimpleDateFormat("EEEE, MMMM d, yyyy \'at\' h:mm a")
    private lateinit var expectDate2: String
    private lateinit var expectDate1: String

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var meetUpRepository: MeetUpRepository

    @Before
    fun setup() {
        hiltRule.inject()

        date1 = Calendar.getInstance()
        date1.set(2022, 2, 1, 0, 0, 0)
        date2 = Calendar.getInstance()
        date2.set(2022, 2, 1, 1, 0, 0)

        val user = ProfileUser("dummy1", "dummy2", "dummy", date1, ImageInstance("icons/demo_pfp.jpeg"))

        expectDate1 = format.format(date1)
        expectDate2 = format.format(date2)

        meetup = MeetUp(
            "dummy",
            user,
            "",
            eventName,
            eventDescription,
            date1,
            date2,
            Location(0.0, 0.0),
            emptySet(),
            true,
            2,
            mutableListOf(user)
        )
    }

    @After
    fun cleanUp() {
        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).clearDB()
    }

    @Test
    fun editExistingMeetupDisplayRightFields() = runTest {

        val id = meetUpRepository.createMeetUp(meetup)
        assertThat(id, notNullValue())

        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
            .apply {
                putExtra(MEETUP_EDIT, id)
            }
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            onView(withId(R.id.et_EventName)).check(matches(withText(eventName)))
            onView(withId(R.id.et_Description)).check(matches(withText(eventDescription)))
            onView(withId(R.id.tv_StartDate)).check(matches(withText(expectDate1)))
            onView(withId(R.id.tv_EndDate)).check(matches(withText(expectDate2)))
            onView(withId(R.id.et_Capacity)).check(matches(isEnabled()))
            onView(withId(R.id.hasCapacityButton)).check(matches(isChecked()))
        }
    }

    @Test
    fun editExistingMeetupEditTheRightOneInDB() = runTest {

        val id = meetUpRepository.createMeetUp(meetup)
        assertThat(id, notNullValue())

        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
            .apply {
                putExtra(MEETUP_EDIT, id)
            }
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {

            Intents.init()

            onView(withId(R.id.et_EventName)).perform(typeText("Manger des patates"))
            closeSoftKeyboard()
            onView(withId(R.id.bt_Done)).perform(scrollTo(), click())

            Intents.intended(IntentMatchers.hasComponent(MeetUpView::class.java.name))
            Intents.release()

            onView(withId(R.id.tv_ViewEventName)).check(matches(withText("dummy1Manger des patates")))
        }
    }

    @Test
    fun createMeetUpDisplayBlankInfo() = runTest {
        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            onView(withId(R.id.et_EventName)).check(matches(withText("")))
            onView(withId(R.id.et_Description)).check(matches(withText("")))
            onView(withId(R.id.et_Capacity)).check(matches(isNotEnabled()))
            onView(withId(R.id.hasCapacityButton)).check(matches(isNotChecked()))
        }
    }

    @Test
    fun capacityFieldMatchesCapacityCheckBox() = runTest {

        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            onView(withId(R.id.hasCapacityButton)).perform(scrollTo())

            onView(withId(R.id.et_Capacity)).check(matches(isNotEnabled()))
            onView(withId(R.id.hasCapacityButton)).check(matches(isNotChecked()))

            onView(withId(R.id.hasCapacityButton)).perform(click())

            onView(withId(R.id.et_Capacity)).check(matches(isEnabled()))
            onView(withId(R.id.hasCapacityButton)).check(matches(isChecked()))

            onView(withId(R.id.hasCapacityButton)).perform(click())

            onView(withId(R.id.et_Capacity)).check(matches(isNotEnabled()))
            onView(withId(R.id.hasCapacityButton)).check(matches(isNotChecked()))
        }
    }

    @Test
    fun createMeetUpThenDisplayRightInfo() = runTest {
        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            Intents.init()

            onView(withId(R.id.et_EventName)).perform(typeText("Meetup name"), click())
            onView(withId(R.id.et_Description)).perform(typeText("Meetup description"), click())
            closeSoftKeyboard()
            onView(withId(R.id.bt_Done)).perform(scrollTo(), click())

            Intents.intended(IntentMatchers.hasComponent(MeetUpView::class.java.name))
            Intents.release()

            onView(withId(R.id.tv_ViewEventName)).check(matches(withText("Meetup name")))
            onView(withId(R.id.tv_ViewEventDescritpion)).check(matches(withText("Meetup description")))
        }
    }
}