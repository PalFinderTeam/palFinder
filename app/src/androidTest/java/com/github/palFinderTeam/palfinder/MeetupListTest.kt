package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.palFinderTeam.palfinder.meetups.MeetUpDumb
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class MeetUpListTest {

    @Test
    fun testAddingMeetup(){
        var c1 = Calendar.getInstance()
        c1.set(2009, 8, 23)
        var c2 = Calendar.getInstance()
        c2.set(2009, 8, 25)

        val meetups_list = listOf<MeetUpDumb>(MeetUpDumb(icon = null, name = "cuire des carottes",
            description = "nous aimerions bien nous atteler à la cuisson de carottes au beurre", startDate = c1,
            endDate = c2, location = null, tags = null, capacity = 45))
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)
            .apply{
                putExtra("MEETUPS", meetups_list as Serializable)
            }
        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        try {
            onView(withId(R.id.meetup_title)).check(matches(withText(meetups_list.get(0).name)))
        } finally {
            scenario.close()
        }

    }
}
