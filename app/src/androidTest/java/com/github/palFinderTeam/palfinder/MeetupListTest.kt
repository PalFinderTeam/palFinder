package com.github.palFinderTeam.palfinder

import android.content.Intent
import android.icu.util.Calendar
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.TempUser
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.RecyclerViewMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class MeetUpListTest {
    private lateinit var meetups_list: List<MeetUp>;//TODO - correct to use right meetup

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
            MeetUp(icon = "", name = "cuire des carottes",
                description = "nous aimerions bien nous atteler à la cuisson de carottes au beurre", startDate = c1,
                endDate = c2, location = Location(0.0, 0.0), tags = listOf(Category.DRINKING.toString()), capacity = 45,
                creator = TempUser("", "as"), hasMaxCapacity = true, participants = listOf<TempUser>(
                    TempUser("", "cae")
                ).toMutableList(),
                uuid = "ce"
            ),
            MeetUp(icon = "", name = "cuire des patates",
                description = "nous aimerions bien nous atteler à la cuisson de patates au beurre", startDate = c2,
                endDate = c1, location = Location(0.0, 0.0), tags = listOf(Category.DRINKING.toString()), capacity = 48,
                creator = TempUser("", "as"), hasMaxCapacity = true, participants = listOf<TempUser>(
                    TempUser("", "cae")
                ).toMutableList(),
                uuid = "ce"),
            MeetUp(icon = "", name = "Street workout",
                description = "workout pepouse au pont chauderon", startDate = c3,
                endDate = c1, location = Location(0.0, 0.0), tags = listOf(Category.DRINKING.toString()), capacity = 9,
                creator = TempUser("", "as"), hasMaxCapacity = true, participants = listOf<TempUser>(
                    TempUser("", "cae")
                ).toMutableList(),
                uuid = "ce"
            ),
            MeetUp(icon = "", name = "Van Gogh Beaulieux",
                description = "Expo sans tableau c'est bo", startDate = c4,
                endDate = c1, location = Location(0.0, 0.0), tags = listOf(Category.DRINKING.toString()), capacity = 15,
                creator = TempUser("", "as"), hasMaxCapacity = true, participants = listOf<TempUser>(
                    TempUser("", "cae")
                ).toMutableList(),
                uuid = "ce"
            ),
            MeetUp(icon = "", name = "Palexpo",
                description = "popopo", startDate = c4,
                endDate = c2, location = Location(0.0, 0.0), tags = listOf(Category.DRINKING.toString()), capacity = 13,
                creator = TempUser("", "as"), hasMaxCapacity = true, participants = listOf<TempUser>(
                    TempUser("", "cae")
                ).toMutableList(),
                uuid = "ce"
            ),
        )
    }

    @Test
    fun testDisplayActivity() {
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

    @Test
    fun sortByCapacityWorks() {
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)
            .apply {
                putExtra("MEETUPS", meetups_list as Serializable)
            }
        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use{
            onView(RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(0, R.id.meetup_title))
                .check(matches(withText(meetups_list[0].name)))
        }
    }
}
