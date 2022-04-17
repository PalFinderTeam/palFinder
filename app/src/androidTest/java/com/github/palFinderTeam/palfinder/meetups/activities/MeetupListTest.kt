package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.res.Resources
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.UIMockMeetUpRepositoryModule
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.launchFragmentInHiltContainer
import com.github.palFinderTeam.palfinder.utils.onHiltFragment
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltAndroidTest
class MeetUpListTest {

    private lateinit var date1: Calendar
    private lateinit var date2: Calendar
    private lateinit var date3: Calendar
    private lateinit var date4: Calendar
    private lateinit var meetUpList: List<MeetUp>
    private lateinit var user1: String
    private lateinit var user2: String
    private lateinit var navController: TestNavHostController

    private val searchLocation = Location(-122.0, 37.0)
    private val loggedUserId = "Marcel"

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var meetUpRepository: MeetUpRepository

    @Inject
    lateinit var profileService: ProfileService

    @Before
    fun setup() {
        hiltRule.inject()

        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).clearDB()
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(
            loggedUserId
        )

        date1 = Calendar.getInstance()
        date1.set(2022, 2, 6)
        date2 = Calendar.getInstance()
        date2.set(2022, 1, 8)
        date3 = Calendar.getInstance()
        date3.set(2022, 2, 1)
        date4 = Calendar.getInstance()
        date4.set(2022, 0, 1)

        //user1 = ProfileUser("User1", "Us", "er1", date1, ImageInstance("icons/demo_pfp.jpeg"))
        //user2 = ProfileUser("User2", "Us", "er2", date2, ImageInstance("icons/demo_pfp.jpeg"))

        user1 = "user1Id"
        user2 = "user2Id"


        meetUpList = listOf(
            MeetUp(
                iconId = "",
                name = "cuire des carottes",
                description = "nous aimerions bien nous atteler à la cuisson de carottes au beurre",
                startDate = date1,
                endDate = date2,
                location = Location(-122.0, 38.0),
                tags = setOf(Category.DRINKING),
                capacity = 45,
                creatorId = user1,
                hasMaxCapacity = true,
                participantsId = listOf(
                    user2,
                    loggedUserId
                ),
                uuid = "ce"
            ),
            MeetUp(
                iconId = "",
                name = "cuire des patates",
                description = "nous aimerions bien nous atteler à la cuisson de patates au beurre",
                startDate = date2,
                endDate = date1,
                location = Location(-122.0, 38.0),
                tags = setOf(Category.WORKING_OUT, Category.DUMMY_TAG1),
                capacity = 48,
                creatorId = user1,
                hasMaxCapacity = true,
                participantsId = listOf(
                    user2
                ),
                uuid = "ce"
            ),
            MeetUp(
                iconId = "",
                name = "Street workout",
                description = "workout pepouse au pont chauderon",
                startDate = date3,
                endDate = date1,
                location = Location(-122.0, 38.0),
                tags = setOf(Category.DRINKING),
                capacity = 9,
                creatorId = user2,
                hasMaxCapacity = true,
                participantsId = listOf(
                    user1
                ),
                uuid = "ce"
            ),
            MeetUp(
                iconId = "",
                name = "Van Gogh Beaulieux",
                description = "Expo sans tableau c'est bo",
                startDate = date4,
                endDate = date1,
                location = Location(-122.0, 37.0),
                tags = setOf(Category.DRINKING),
                capacity = 15,
                creatorId = user1,
                hasMaxCapacity = true,
                participantsId = listOf(
                    user1
                ),
                uuid = "ce"
            ),
            MeetUp(
                iconId = "",
                name = "Palexpo",
                description = "popopo",
                startDate = date4,
                endDate = date2,
                location = Location(-122.0, 38.0),
                tags = setOf(Category.DRINKING),
                capacity = 13,
                creatorId = user1,
                hasMaxCapacity = true,
                participantsId = listOf(
                    user2
                ),
                uuid = "ce2"
            ),
        )

        runOnUiThread {
            navController = TestNavHostController(ApplicationProvider.getApplicationContext())
            navController.setViewModelStore(ViewModelStore())
            navController.setGraph(R.navigation.main_nav_graph)
            navController.setCurrentDestination(R.id.list_fragment)
        }
    }

    @After
    fun cleanUp() {
        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).clearDB()
    }

    @Test
    fun testDisplayActivities() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putBoolean("ShowOnlyJoined", false)
        }, navHostController = navController)

        scenario!!.use {
            scenario.onHiltFragment<MeetupListFragment> {
                it.viewModel.setSearchParameters(location = searchLocation)
                it.viewModel.fetchMeetUps()
            }

            onView(
                RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(
                    0,
                    R.id.meetup_title
                )
            )
                .check(matches(withText(meetUpList[0].name)))
            onView(
                RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(
                    0,
                    R.id.meetup_description
                )
            )
                .check(matches(withText(meetUpList[0].description)))
        }
    }

    @Test
    fun sortWorks() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putBoolean("ShowOnlyJoined", false)
        }, navHostController = navController)
        scenario!!.use {
            scenario.onHiltFragment<MeetupListFragment> {
                it.viewModel.setSearchParameters(location = searchLocation)
                it.viewModel.fetchMeetUps()
            }
            onView(withId(R.id.sort_list)).perform(click())
            onView(withText(R.string.list_sort_by_capacity)).perform(click())

            onView(
                RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(
                    0,
                    R.id.meetup_title
                )
            )
                .check(matches(withText(meetUpList.sortedBy { it.capacity }[0].name)))
            onView(withId(R.id.sort_list)).perform(click())
            onView(withText(R.string.list_sort_by_alphabetical_order)).perform(click())
            onView(
                RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(
                    0,
                    R.id.meetup_title
                )
            )
                .check(matches(withText(meetUpList.sortedBy { it.name.lowercase() }[0].name)))

            onView(withId(R.id.sort_list)).perform(click())
            onView(withText(R.string.list_sort_by_location)).perform(click())
            onView(
                RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(
                    0,
                    R.id.meetup_title
                )
            )
                .check(matches(withText(meetUpList.sortedBy {
                    it.location.distanceInKm(
                        searchLocation
                    )
                }[0].name)))
        }
    }


    @Test
    fun filterWorks() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putBoolean("ShowOnlyJoined", false)
        }, navHostController = navController)

        scenario!!.use {
            scenario.onHiltFragment<MeetupListFragment> { listFrag ->
                listFrag.viewModel.setSearchParameters(location = searchLocation)
                listFrag.viewModel.fetchMeetUps()
            }
            scenario.onHiltFragment<MeetupListFragment> { listFrag ->
                listFrag.filter(setOf(Category.CINEMA))
                assert(listFrag.adapter.currentDataSet.isEmpty())
                listFrag.filter(setOf(Category.WORKING_OUT, Category.DUMMY_TAG1))
                assertThat(listFrag.adapter.currentDataSet.size, `is`(1))
                listFrag.filter(setOf())
                assertThat(listFrag.adapter.currentDataSet.size, `is`(5))
            }
        }
    }

    @Test
    fun filterWorksAddTag() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putBoolean("ShowOnlyJoined", false)
        }, navHostController = navController)

        scenario!!.use {
            scenario.onHiltFragment<MeetupListFragment> { listFrag ->
                listFrag.viewModel.setSearchParameters(location = searchLocation)
                listFrag.viewModel.fetchMeetUps()
            }
            scenario.onHiltFragment<MeetupListFragment> { listFrag ->
                listFrag.viewModel.tagRepository.addTag(Category.CINEMA)
                assert(listFrag.adapter.currentDataSet.isEmpty())
                listFrag.viewModel.tagRepository.removeTag(Category.CINEMA)
                assertThat(listFrag.adapter.currentDataSet.size, `is`(5))
                listFrag.viewModel.tagRepository.removeTag(Category.CINEMA)
                assertThat(listFrag.adapter.currentDataSet.size, `is`(5))
                listFrag.viewModel.tagRepository.addTag(Category.WORKING_OUT)
                assertThat(listFrag.adapter.currentDataSet.size, `is`(1))
                listFrag.viewModel.tagRepository.addTag(Category.WORKING_OUT)
                assertThat(listFrag.adapter.currentDataSet.size, `is`(1))
            }
        }
    }

    @Test
    fun clickItem() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putBoolean("ShowOnlyJoined", false)
        }, navHostController = navController)

        scenario!!.use {
            init()
            scenario.onHiltFragment<MeetupListFragment> {
                it.viewModel.setSearchParameters(location = searchLocation)
                it.viewModel.fetchMeetUps()
            }
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

    @Test
    fun openMap() {
        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putBoolean("ShowOnlyJoined", false)
        }, navHostController = navController)

        scenario!!.use {
            scenario.onHiltFragment<MeetupListFragment> {
                it.viewModel.setSearchParameters(location = searchLocation)
                it.viewModel.fetchMeetUps()
            }

            onView(withId(R.id.search_place)).perform(click())

            assertThat(navController.currentDestination?.id, `is`(R.id.maps_fragment))
        }
    }

    @Test
    fun showJoinedMeetupsOnlyShowJoinedMeetUps() = runTest {
        meetUpList.forEach { meetUpRepository.createMeetUp(it) }

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putBoolean("ShowOnlyJoined", true)
        }, navHostController = navController)

        scenario!!.use {
            scenario.onHiltFragment<MeetupListFragment> {
                it.viewModel.setSearchParamAndFetch(location = searchLocation)
                it.viewModel.fetchMeetUps()
            }

            val joinedMeetUps = meetUpList.filter { it.participantsId.contains(loggedUserId) }
                .map { withText(it.name) }

            onView(withId(R.id.meetup_list_recycler)).check(
                matches(
                    recyclerViewSizeMatcher(
                        joinedMeetUps.size
                    )
                )
            )
            for (i in (joinedMeetUps.indices)) {
                onView(
                    RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(
                        i,
                        R.id.meetup_title
                    )
                )
                    .check(matches(anyOf(joinedMeetUps)))
            }
        }
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

fun recyclerViewSizeMatcher(matcherSize: Int): Matcher<View?> {
    return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with list size: $matcherSize")
        }

        override fun matchesSafely(recyclerView: RecyclerView): Boolean {
            return matcherSize == recyclerView.adapter!!.itemCount
        }
    }
}