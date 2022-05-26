package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.UIMockMeetUpRepositoryModule
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.fragments.MeetupFilterFragment
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.*
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltAndroidTest
class MeetUpListTest {

    private lateinit var dateNow: Calendar
    private lateinit var date1: Calendar
    private lateinit var date2: Calendar
    private lateinit var date3: Calendar
    private lateinit var date4: Calendar
    private lateinit var meetUpList: List<MeetUp>
    private lateinit var user1: String
    private lateinit var user2: String
    private lateinit var user3: ProfileUser
    private lateinit var navController: TestNavHostController

    private val searchLocation = Location(-122.0, 37.0)
    private val loggedUserId = "Marcel"

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var meetUpRepository: MeetUpRepository

    @Inject
    lateinit var profileService: ProfileService

    @Inject
    lateinit var realTimeService: TimeService

    @Before
    fun setup() {
        hiltRule.inject()

        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).clearDB()
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(
            loggedUserId
        )

        dateNow = Calendar.getInstance()
        dateNow.set(2022,0,0)
        date1 = Calendar.getInstance()
        date1.set(2022, 2, 6)
        date2 = Calendar.getInstance()
        date2.set(2022, 1, 8)
        date3 = Calendar.getInstance()
        date3.set(2022, 2, 1)
        date4 = Calendar.getInstance()
        date4.set(2022, 0, 1)

        (realTimeService as UIMockTimeServiceModule.UIMockTimeService).setDate(dateNow)

        //user1 = ProfileUser("User1", "Us", "er1", date1, ImageInstance("icons/demo_pfp.jpeg"))
        //user2 = ProfileUser("User2", "Us", "er2", date2, ImageInstance("icons/demo_pfp.jpeg"))

        user1 = "user1Id"
        user2 = "user2Id"
        user3 = ProfileUser("user3Id", "dfdf", "dfdsfds", "efsf",date1, ImageInstance("efe"), blockedUsers = listOf(user1))


        meetUpList = listOf(
            MeetUp(
                iconImage = null,
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
                uuid = "ce",
                criterionAge = null,
                criterionGender = null
            ),
            MeetUp(
                iconImage = null,
                name = "cuire des patates",
                description = "nous aimerions bien nous atteler à la cuisson de patates au beurre",
                startDate = date2,
                endDate = date1,
                location = Location(-122.0, 38.0),
                tags = setOf(Category.WORKING_OUT, Category.SPORTS),
                capacity = 48,
                creatorId = user1,
                hasMaxCapacity = true,
                participantsId = listOf(
                    user2
                ),
                uuid = "ce",
                criterionAge = null,
                criterionGender = null
            ),
            MeetUp(
                iconImage = null,
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
                uuid = "ce",
                criterionAge = null,
                criterionGender = null
            ),
            MeetUp(
                iconImage = null,
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
                uuid = "ce",
                criterionAge = null,
                criterionGender = null
            ),
            MeetUp(
                iconImage = null,
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
                uuid = "ce2",
                criterionAge = null,
                criterionGender = null
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
        meetUpList.forEach { meetUpRepository.create(it) }

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
        meetUpList.forEach { meetUpRepository.create(it) }

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
        meetUpList.forEach { meetUpRepository.create(it) }

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
                listFrag.filter(setOf(Category.WORKING_OUT, Category.SPORTS))
                assertThat(listFrag.adapter.currentDataSet.size, `is`(1))
                listFrag.filter(setOf())
                assertThat(listFrag.adapter.currentDataSet.size, `is`(5))
            }
        }
    }

    @Test
    fun filterWorksAddTag() = runTest {
        meetUpList.forEach { meetUpRepository.create(it) }

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
        meetUpList.forEach { meetUpRepository.create(it) }

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
    //TODO fix
    fun showJoinedMeetupsOnlyShowJoinedMeetUps() = runTest {
        meetUpList.forEach { meetUpRepository.create(it) }

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putSerializable("ShowParam", ShowParam.ONLY_JOINED)
        }, navHostController = navController)

        scenario!!.use {

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

    @Test
    fun showFilterBlockedWorks() = runTest {
        meetUpList.forEach { meetUpRepository.create(it) }
        val user3Id = profileService.create(user3)
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(
            user3Id!!
        )

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putBoolean("ShowOnlyJoined", false)
        }, navHostController = navController)

        scenario!!.use {

            scenario.onHiltFragment<MeetupListFragment> {
                it.viewModel.setSearchParamAndFetch(location = searchLocation, filterBlockedMeetups = true, forceFetch = true)
            }

            val availableMeetUps = meetUpList.filter { it.creatorId != user1 }
                .map { withText(it.name) }

            onView(withId(R.id.meetup_list_recycler)).check(
                matches(
                    recyclerViewSizeMatcher(
                        availableMeetUps.size
                    )
                )
            )
            for (i in (availableMeetUps.indices)) {
                onView(
                    RecyclerViewMatcher(R.id.meetup_list_recycler).atPositionOnView(
                        i,
                        R.id.meetup_title
                    )
                )
                    .check(matches(anyOf(availableMeetUps)))
            }
        }

        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(
            loggedUserId
        )
    }

    @Test
    fun radioButtonReflectRightOptionOnStart() = runTest {
        var scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putSerializable("ShowParam", ShowParam.ONLY_JOINED)
        }, navHostController = navController)
        scenario!!.use {
            onView(withId(R.id.select_filters)).perform(click())
            onView(withId(R.id.joinedButton)).check(matches(isChecked()))
        }
        scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putSerializable("ShowParam", ShowParam.ALL)
        }, navHostController = navController)
        scenario!!.use {
            onView(withId(R.id.select_filters)).perform(click())
            onView(withId(R.id.button_all)).check(matches(isChecked()))
        }
        scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putSerializable("ShowParam", ShowParam.PAL_CREATOR)
        }, navHostController = navController)
        scenario!!.use {
            onView(withId(R.id.select_filters)).perform(click())
            onView(withId(R.id.created_button)).check(matches(isChecked()))
        }
        scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putSerializable("ShowParam", ShowParam.PAL_PARTICIPATING)
        }, navHostController = navController)
        scenario!!.use {
            onView(withId(R.id.select_filters)).perform(click())
            onView(withId(R.id.participate_button)).check(matches(isChecked()))
        }
    }

    @Test
    fun radioButtonFiltersWorkAsExpected() = runTest {

        meetUpList.forEach { meetUpRepository.create(it) }

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putSerializable("ShowParam", ShowParam.ALL)
        }, navHostController = navController)

        scenario!!.use {
            onView(withId(R.id.select_filters)).perform(click())
            onView(withId(R.id.joinedButton)).perform(click())
            Espresso.pressBack()
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

    @Test
    fun showJoinedHideCertainOptions() = runTest {
        meetUpList.forEach { meetUpRepository.create(it) }

        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putSerializable("ShowParam", ShowParam.ALL)
        }, navHostController = navController)

        scenario!!.use {
            onView(withId(R.id.select_filters)).perform(click())
            onView(withId(R.id.joinedButton)).perform(click())
            Espresso.pressBack()

            onView(withId(R.id.distance_slider)).check(matches(withEffectiveVisibility(Visibility.GONE)))
            onView(withId(R.id.search_place)).check(matches(withEffectiveVisibility(Visibility.GONE)))

            onView(withId(R.id.select_filters)).perform(click())
            onView(withId(R.id.button_all)).perform(click())
            Espresso.pressBack()

            onView(withId(R.id.distance_slider)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.search_place)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        }
    }

    @Test
    fun testTimePickers() = runTest {

        val format = SimpleDateFormat()
        val expectDate1 = format.format(date1)
        val expectDate2 = format.format(date2)


        val scenario = launchFragmentInHiltContainer<MeetupListFragment>(Bundle().apply {
            putSerializable("ShowParam", ShowParam.ALL)
        }, navHostController = navController)
        scenario.use {
            onView(withId(R.id.select_filters)).perform(click())
            onView(withId(R.id.tv_StartDate))
                .perform(ViewActions.click())

            onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
                PickerActions.setDate(
                    date1.get(Calendar.YEAR),
                    date1.get(Calendar.MONTH) + 1,
                    date1.get(Calendar.DAY_OF_MONTH)
                ),
            )
            onView(withText("OK")).perform(click()) // Library is stupid and can't even press the f. button
            onView(withClassName(Matchers.equalTo(TimePicker::class.java.name))).perform(
                PickerActions.setTime(date1.get(Calendar.HOUR_OF_DAY), date1.get(Calendar.MINUTE)),
            )
            onView(withText("OK")).perform(click())
            onView(withId(R.id.tv_StartDate)).check(matches(withText(expectDate1)))


            onView(withId(R.id.tv_EndDate)).perform(click())

            onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
                PickerActions.setDate(
                    date2.get(Calendar.YEAR),
                    date2.get(Calendar.MONTH) + 1,
                    date2.get(Calendar.DAY_OF_MONTH)
                ),
            )
            onView(withText("OK")).perform(click()) // Library is stupid and can't even press the f. button
            onView(withClassName(Matchers.equalTo(TimePicker::class.java.name))).perform(
                PickerActions.setTime(date2.get(Calendar.HOUR_OF_DAY), date2.get(Calendar.MINUTE)),
            )
            onView(withText("OK")).perform(click())
            onView(withId(R.id.tv_EndDate)).check(matches(withText(expectDate2)))
            Espresso.pressBack()
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
                        recyclerView.findViewHolderForAdapterPosition(position)?.itemView
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