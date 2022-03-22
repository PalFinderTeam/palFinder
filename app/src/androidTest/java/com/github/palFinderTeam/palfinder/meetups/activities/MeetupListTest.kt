package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.Intent
import android.content.res.Resources
import android.icu.util.Calendar
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.UIMockMeetUpRepositoryModule
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.Assert.assertEquals
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class MeetUpListTest {

    private lateinit var date1: Calendar
    private lateinit var date2: Calendar
    private lateinit var date3: Calendar
    private lateinit var date4: Calendar
    private lateinit var meetUpList: List<MeetUp>
    private lateinit var user1: ProfileUser
    private lateinit var user2: ProfileUser

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var meetUpRepository: MeetUpRepository

    @Before
    fun setup() {
        hiltRule.inject()

        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).clearDB()

        date1 = Calendar.getInstance()
        date1.set(2022, 2, 6)
        date2 = Calendar.getInstance()
        date2.set(2022, 1, 8)
        date3 = Calendar.getInstance()
        date3.set(2022, 2, 1)
        date4 = Calendar.getInstance()
        date4.set(2022, 0, 1)

        user1 = ProfileUser("User1", "Us", "er1", date1, ImageInstance("icons/demo_pfp.jpeg"))
        user2 = ProfileUser("User2", "Us", "er2", date2, ImageInstance("icons/demo_pfp.jpeg"))


        meetUpList = listOf(
            MeetUp(
                icon = "",
                name = "cuire des carottes",
                description = "nous aimerions bien nous atteler à la cuisson de carottes au beurre",
                startDate = date1,
                endDate = date2,
                location = Location(0.0, 0.0),
                tags = setOf(Category.DRINKING),
                capacity = 45,
                creator = user1,
                hasMaxCapacity = true,
                participants = listOf(
                    user2
                ).toMutableList(),
                uuid = "ce"
            ),
            MeetUp(
                icon = "",
                name = "cuire des patates",
                description = "nous aimerions bien nous atteler à la cuisson de patates au beurre",
                startDate = date2,
                endDate = date1,
                location = Location(0.0, 0.0),
                tags = setOf(Category.WORKING_OUT, Category.DUMMY_TAG1),
                capacity = 48,
                creator = user1,
                hasMaxCapacity = true,
                participants = listOf(
                    user2
                ).toMutableList(),
                uuid = "ce"
            ),
            MeetUp(
                icon = "",
                name = "Street workout",
                description = "workout pepouse au pont chauderon",
                startDate = date3,
                endDate = date1,
                location = Location(0.0, 0.0),
                tags = setOf(Category.DRINKING),
                capacity = 9,
                creator = user2,
                hasMaxCapacity = true,
                participants = listOf(
                    user1
                ).toMutableList(),
                uuid = "ce"
            ),
            MeetUp(
                icon = "",
                name = "Van Gogh Beaulieux",
                description = "Expo sans tableau c'est bo",
                startDate = date4,
                endDate = date1,
                location = Location(0.0, 0.0),
                tags = setOf(Category.DRINKING),
                capacity = 15,
                creator = user1,
                hasMaxCapacity = true,
                participants = listOf(
                    user1
                ).toMutableList(),
                uuid = "ce"
            ),
            MeetUp(
                icon = "",
                name = "Palexpo",
                description = "popopo",
                startDate = date4,
                endDate = date2,
                location = Location(0.0, 0.0),
                tags = setOf(Category.DRINKING),
                capacity = 13,
                creator = user1,
                hasMaxCapacity = true,
                participants = listOf(
                    user2
                ).toMutableList(),
                uuid = "ce2"
            ),
        )
    }

    @After
    fun cleanUp() {
        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).clearDB()
    }

    @Test
    fun testDisplayActivities() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)

        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use {
            onView(
                RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(0,
                    R.id.meetup_title
                ))
                .check(matches(withText(meetUpList[0].name)))
            onView(
                RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(0,
                    R.id.meetup_description
                ))
                .check(matches(withText(meetUpList[0].description)))
        }
    }

    @Test
    fun sortWorks() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)

        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use{
            scenario.onActivity { it.sortByCap() }
            onView(RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(0, R.id.meetup_title))
                .check(matches(withText(meetUpList.sortedBy { it.capacity }[0].name)))
            scenario.onActivity { it.sortByName() }
            onView(RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(0, R.id.meetup_title))
                .check(matches(withText(meetUpList.sortedBy { it.name.lowercase() }[0].name)))
        }
    }

    @Test
    fun filterWorks() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)

        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use {
            scenario.onActivity { it.filterByTag(setOf(Category.CINEMA)) }
            scenario.onActivity { assert(it.adapter.currentDataSet.isEmpty()) }
            scenario.onActivity { it.filterByTag(setOf(Category.WORKING_OUT, Category.DUMMY_TAG1)) }
            scenario.onActivity { assertEquals(1, it.adapter.currentDataSet.size) }
            scenario.onActivity { it.filterByTag(setOf()) }
            scenario.onActivity { assertEquals(5, it.adapter.currentDataSet.size) }
        }
    }

    @Test
    fun filterWorksAddTag() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)

        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use {
            scenario.onActivity { it.viewModel.tagRepository.addTag(Category.CINEMA)}
            scenario.onActivity { assert(it.adapter.currentDataSet.isEmpty()) }
            scenario.onActivity { it.viewModel.tagRepository.removeTag(Category.CINEMA)}
            scenario.onActivity { assertEquals(5, it.adapter.currentDataSet.size) }
            scenario.onActivity { it.viewModel.tagRepository.removeTag(Category.CINEMA)}
            scenario.onActivity { assertEquals(5, it.adapter.currentDataSet.size) }
            scenario.onActivity { it.viewModel.tagRepository.addTag(Category.WORKING_OUT)}
            scenario.onActivity { assertEquals(1, it.adapter.currentDataSet.size) }
            scenario.onActivity { it.viewModel.tagRepository.addTag(Category.WORKING_OUT)}
            scenario.onActivity { assertEquals(1, it.adapter.currentDataSet.size) }
        }
    }

    @Test
    fun sortButtonWorks() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)

        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use {
            onView(withId(R.id.sort_list)).perform(click())
            onView(withText(R.string.list_sort_by_capacity))
                .perform(click());
            onView(RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(0, R.id.meetup_title))
                .check(matches(withText(meetUpList.sortedBy { it.capacity }[0].name)))
            onView(withId(R.id.sort_list)).perform(click())
            onView(withText(R.string.list_sort_by_alphabetical_order)).perform(click())
            onView(RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(0, R.id.meetup_title))
                .check(matches(withText(meetUpList.sortedBy { it.name.lowercase() }[0].name)))
            onView(withId(R.id.sort_list)).perform(click())
            onView(withText(R.string.list_sort_by_location)).perform(click())
            onView(RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(0, R.id.meetup_title))
                .check(matches(withText(meetUpList.sortedBy { it.location.distanceInKm(Location(0.0, 0.0))}[0].name)))
        }
    }

    @Test
    fun clickItem() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }
        val intent = Intent(getApplicationContext(), MeetupListActivity::class.java)
        init()
        ActivityScenario.launch<MeetupListActivity>(intent)
        onView(
            RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(
                0,
                R.id.meetup_title
            )
        ).perform(click())
        intended(hasComponent(MeetUpView::class.java.name))
        release()
    }

}



class RecyclerViewMatcher(private val recyclerViewId: Int) {
    fun atPosition(position: Int): Matcher<View> {
        return atPositionOnView(position, -1)
    }

    fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var childView: View? = null
            override fun describeTo(description: Description) {
                var idDescription = recyclerViewId.toString()
                if (resources != null) {
                    idDescription = try {
                        resources!!.getResourceName(recyclerViewId)
                    } catch (var4: Resources.NotFoundException) {
                        String.format(
                            "%s (resource name not found)",
                            Integer.valueOf(recyclerViewId)
                        )
                    }
                }
                description.appendText("with id: $idDescription")
            }

            public override fun matchesSafely(view: View): Boolean {
                resources = view.resources
                if (childView == null) {
                    val recyclerView = view.rootView.findViewById<View>(
                        recyclerViewId
                    ) as RecyclerView
                    childView = if (recyclerView.id == recyclerViewId) {
                        recyclerView.findViewHolderForAdapterPosition(position)!!.itemView
                    } else {
                        return false
                    }
                }
                return if (targetViewId == -1) {
                    view === childView
                } else {
                    val targetView = childView!!.findViewById<View>(targetViewId)
                    view === targetView
                }
            }
        }
    }
}