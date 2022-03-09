package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.Intent
import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.TempUser
import com.github.palFinderTeam.palfinder.utils.Location
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeetupViewTest {
    private var meetup: MeetUp? = null
    private val eventName = "dummy1"
    private val eventDescription = "dummy2"
    @Before
    fun init(){
        val date1 = Calendar.getInstance()
        val date2 = Calendar.getInstance()

        meetup = MeetUp(
            "dummy",
            TempUser("", "Bob"),
            "",
            eventName,
            eventDescription,
            date1,
            date2,
            Location(0.0,0.0),
            emptyList(),
            true,
            2,
            mutableListOf(TempUser("", "Alice"))
        )
    }

    @Test
    fun testCorrectName(){
        val intent = Intent(getApplicationContext(), MeetUpView::class.java)
            .apply{
                putExtra(MEETUP_SHOWN, meetup)
            }
        val scenario = ActivityScenario.launch<MeetUpView>(intent)
        scenario.use {
            onView(withId(R.id.tv_ViewEventName)).check(matches(withText(eventName)))
            onView(withId(R.id.tv_ViewEventDescritpion)).check(matches(withText(eventDescription)))
        }
    }

    @Test
    fun testEdit(){
        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
            .apply{
                putExtra(MEETUP_EDIT, meetup)
            }
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            onView(withId(R.id.et_EventName)).check(matches(withText(eventName)))
            onView(withId(R.id.et_Description)).check(matches(withText(eventDescription)))
        }
    }
}