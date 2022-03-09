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
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class MeetUpListTest {

    @Test
    fun testAddingMeetup(){
        var c1 = Calendar.getInstance()
        c1.set(2022, 2, 6)
        var c2 = Calendar.getInstance()
        c2.set(2022, 1, 8)
        var c3 = Calendar.getInstance()
        c3.set(2022, 2, 1)
        var c4 = Calendar.getInstance()
        c4.set(2022, 0, 1)

        val meetups_list = listOf<MeetUpDumb>(
            MeetUpDumb(icon = null, name = "cuire des carottes",
                description = "nous aimerions bien nous atteler à la cuisson de carottes au beurre", startDate = c1,
                endDate = c2, location = null, tags = null, capacity = 45),
            MeetUpDumb(icon = null, name = "cuire des patates",
                description = "nous aimerions bien nous atteler à la cuisson de patates au beurre", startDate = c2,
                endDate = c1, location = null, tags = null, capacity = 48),
            MeetUpDumb(icon = null, name = "Street workout",
                description = "workout pepouse au pont chauderon", startDate = c3,
                endDate = c1, location = null, tags = null, capacity = 4),
            MeetUpDumb(icon = null, name = "Van Gogh Beaulieux",
                description = "Expo sans tableau c'est bo", startDate = c4,
                endDate = c1, location = null, tags = null, capacity = 15),
            MeetUpDumb(icon = null, name = "Palexpo",
                description = "popopo", startDate = c4,
                endDate = c2, location = null, tags = null, capacity = 18),
        )
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)
            .apply{
                putExtra("MEETUPS", meetups_list as Serializable)
            }
        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        try { //TODO - extend tests to test all fields
                onView(allOf(withId(R.id.meetup_title), withText("carottes")).).check(matches(withText(meetups_list.get(0).name)))

        } finally {
            scenario.close()
        }

    }
}
