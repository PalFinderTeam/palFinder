package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
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

//@RunWith(AndroidJUnit4::class)
class MeetupViewTest {
/*
    private var meetup: MeetUp? = null
    private val eventName = "dummy1"
    private val eventDescription = "dummy2"
    private var date1: Calendar? = null
    private var date2: Calendar? = null

    val format = SimpleDateFormat("EEEE, MMMM d, yyyy \'at\' h:mm a")
    var expectDate2: String? = null
    var expectDate1: String? = null

    @Before
    fun init(){
        date1 = Calendar.getInstance()
        date1!!.set(2022, 2,1,0,0,0)
        date2 = Calendar.getInstance()
        date2!!.set(2022, 2,1,1,0,0)

        expectDate1 = format.format(date1)!!
        expectDate2 = format.format(date2)!!

        meetup = MeetUp(
            "dummy",
            TempUser("", "Bob"),
            "",
            eventName,
            eventDescription,
            date1!!,
            date2!!,
            Location(0.0,0.0),
            emptyList(),
            true,
            2,
            mutableListOf(TempUser("", "Alice"))
        )
    }

    @Test
    fun testCorrectFields(){
        val intent = Intent(getApplicationContext(), MeetUpView::class.java)
            .apply{
                putExtra(MEETUP_SHOWN, meetup)
            }

        val scenario = ActivityScenario.launch<MeetUpView>(intent)
        scenario.use {
            onView(withId(R.id.tv_ViewEventName)).check(matches(withText(eventName)))
            onView(withId(R.id.tv_ViewEventDescritpion)).check(matches(withText(eventDescription)))
            onView(withId(R.id.tv_ViewStartDate)).check(matches(withText(expectDate1)))
            onView(withId(R.id.tv_ViewEndDate)).check(matches(withText(expectDate2)))
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
            onView(withId(R.id.tv_StartDate)).check(matches(withText(expectDate1)))
            onView(withId(R.id.tv_EndDate)).check(matches(withText(expectDate2)))
        }
    }
*/
}