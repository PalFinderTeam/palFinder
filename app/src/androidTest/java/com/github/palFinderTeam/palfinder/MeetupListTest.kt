package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.icu.util.Calendar
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.palFinderTeam.palfinder.meetups.MeetUpDumb
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class MeetUpListTest {
    private lateinit var meetups_list: List<MeetUpDumb>;//TODO - correct to use right meetup

    @Before
    fun create_meetups_lists() {
        var c1 = Calendar.getInstance()
        c1.set(2022, 2, 6)
        var c2 = Calendar.getInstance()
        c2.set(2022, 1, 8)
        var c3 = Calendar.getInstance()
        c3.set(2022, 2, 1)
        var c4 = Calendar.getInstance()
        c4.set(2022, 0, 1)

        meetups_list = listOf(
            MeetUpDumb(
                icon = null,
                name = "cuire des carottes",
                description = "nous aimerions bien nous atteler à la cuisson de carottes au beurre",
                startDate = c1,
                endDate = c2,
                location = null,
                tags = null,
                capacity = 45
            )
        )
    }

    @Test
    fun testAddingMeetup() {
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)
            .apply {
                putExtra("MEETUPS", meetups_list as Serializable)
            }
        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        try { //TODO - extend tests to test all fields
            onView(withId(R.id.meetup_title)).check(matches(withText(meetups_list.get(0).name)))
            onView(withId(R.id.meetup_description)).check(
                matches(
                    withText(meetups_list.get(0).description)
                )
            )
        } finally {
            scenario.close()
        }
    }
}
