package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.view.View
import android.widget.DatePicker
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ScrollToAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.github.palFinderTeam.palfinder.ProfileActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.UIMockMeetUpRepositoryModule
import com.github.palFinderTeam.palfinder.chat.CHAT
import com.github.palFinderTeam.palfinder.chat.ChatActivity
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.*
import com.github.palFinderTeam.palfinder.utils.*
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltAndroidTest
class MeetupViewTest {

    private lateinit var meetup: MeetUp
    private lateinit var meetup2: MeetUp
    private lateinit var newMeetup: MeetUp
    private lateinit var user: ProfileUser
    private lateinit var user2: ProfileUser
    private val eventName = "dummy1"
    private val eventDescription = "dummy2"
    private lateinit var date1: Calendar
    private lateinit var date2: Calendar
    private lateinit var navController: TestNavHostController

    private val format = SimpleDateFormat("EEEE, MMMM d, yyyy \'at\' h:mm a")
    private lateinit var expectDate2: String
    private lateinit var expectDate1: String

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var meetUpRepository: MeetUpRepository

    @Inject
    lateinit var profileRepository: ProfileService

    @Inject
    lateinit var timeService: TimeService

    @Before
    fun setup() {
        hiltRule.inject()

        date1 = Calendar.getInstance()
        date1.add(Calendar.DAY_OF_MONTH, 1)
        date1.set(Calendar.HOUR_OF_DAY, 0)
        date1.set(Calendar.MINUTE, 0)
        date1.set(Calendar.SECOND, 0)
        //date1.set(2022, 2, 1, 0, 0, 0)
        date2 = Calendar.getInstance()
        //date2.set(2022, 2, 1, 1, 0, 0)
        date2.add(Calendar.DAY_OF_MONTH, 1)
        date2.set(Calendar.HOUR_OF_DAY, 1)
        date2.set(Calendar.MINUTE, 0)
        date2.set(Calendar.SECOND, 0)


        expectDate1 = format.format(date1)
        expectDate2 = format.format(date2)


        user = ProfileUser(
            "user",
            "Michou",
            "Jonas",
            "Martin",
            date1,
            ImageInstance(""),
            "Ne la laisse pas tomber"
        )
        user2 = ProfileUser(
            "user2",
            "Michou",
            "Jonas",
            "Martin",
            date1,
            ImageInstance(""),
            "Ne la laisse pas tomber"
        )
        meetup = MeetUp(
            "dummy",
            "user",
            null,
            eventName,
            eventDescription,
            date1,
            date2,
            Location(0.0, 0.0),
            emptySet(),
            true,
            2,
            mutableListOf("user"),
            null,
            null
        )

        meetup2 = MeetUp(
            "dummy",
            "user",
            null,
            eventName,
            eventDescription,
            date1,
            date2,
            Location(0.0, 0.0),
            emptySet(),
            true,
            2,
            mutableListOf("user"),
            Pair(13, Int.MAX_VALUE),
            null
        )

        UiThreadStatement.runOnUiThread {
            navController = TestNavHostController(getApplicationContext())
            navController.setViewModelStore(ViewModelStore())
            navController.setGraph(R.navigation.main_nav_graph)
            navController.setCurrentDestination(R.id.creation_fragment)
        }
        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(
            user2.uuid
        )
    }

    @After
    fun cleanUp() {
        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).clearDB()
        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).clearDB()
    }

    @Test
    fun editExistingMeetupDisplayRightFields() = runTest {
        val id = meetUpRepository.create(meetup)
        assertThat(id, notNullValue())

        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", id)
            ), navHostController = navController
        )
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
    fun selectLocationBringsTheMap() = runTest {
        val id = meetUpRepository.create(meetup)
        assertThat(id, notNullValue())

        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", id)
            ), navHostController = navController
        )

        scenario.use {
            onView(withId(R.id.bt_locationSelect)).perform(click())
            assertThat(navController.currentDestination?.id, `is`(R.id.maps_fragment))

        }
    }

    @Test
    fun editExistingMeetupEditTheRightOneInDB() = runTest {

        val id = meetUpRepository.create(meetup)
        assertThat(id, notNullValue())

        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", id)
            ), navHostController = navController
        )
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
        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", null)
            ), navHostController = navController
        )
        scenario.use {
            onView(withId(R.id.et_EventName)).check(matches(withText("")))
            onView(withId(R.id.et_Description)).check(matches(withText("")))
            onView(withId(R.id.et_Capacity)).check(matches(isNotEnabled()))
            onView(withId(R.id.hasCapacityButton)).check(matches(isNotChecked()))
        }
    }

    @Test
    fun criterionFragmentWorksCorrectly() = runTest {
        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", null)
            ), navHostController = navController
        )
        scenario!!.use {
            onView(withId(R.id.criterionsSelectButton)).perform(scrollTo(), click())
            onView(withId(R.id.radioMaleAndFemale)).check(matches(isChecked()))
            onView(withId(R.id.radioMale)).check(matches(isNotChecked()))
            onView(withId(R.id.maxValueAge)).check(matches(withText("66+")))
            onView(withId(R.id.minValueAge)).check(matches(withText("13")))
            onView(withId(R.id.criterionButtonDone)).perform(click())
            scenario.onHiltFragment<MeetUpCreation> {
                it.viewModel.setCriterionAge(Pair(15, 54))
                it.viewModel.setCriterionGender(CriterionGender.MALE)
            }
            onView(withId(R.id.criterionsSelectButton)).perform(click())
            onView(withId(R.id.radioMaleAndFemale)).check(matches(isNotChecked()))
            onView(withId(R.id.radioMale)).check(matches(isChecked()))
            onView(withId(R.id.maxValueAge)).check(matches(withText("54")))
            onView(withId(R.id.minValueAge)).check(matches(withText("15")))
        }
    }

    @Test
    fun capacityFieldMatchesCapacityCheckBox() = runTest {

        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", null)
            ), navHostController = navController
        )
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
        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", null)
            ), navHostController = navController
        )
        scenario!!.use {
            Intents.init()

            scenario.onHiltFragment<MeetUpCreation> {
                it.viewModel.setLatLng(LatLng(0.0,0.0))
            }
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


    @Test
    fun userClickableInFragment() = runTest {
        val userid = profileRepository.create(user)
        val id2 = profileRepository.create(user2)
        val userListFactory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
                when (loadFragmentClass(classLoader, className)) {
                    ProfileListFragment::class.java -> ProfileListFragment(listOf(userid!!, id2!!))
                    else -> super.instantiate(classLoader, className)
                }
        }

        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(userid!!)

        assertThat(userid, notNullValue())
        newMeetup = MeetUp(
            "dummy",
            userid!!,
            null,
            eventName,
            eventDescription,
            date1,
            date2,
            Location(0.0, 0.0),
            emptySet(),
            true,
            2,
            mutableListOf(userid)
        )
        val id = meetUpRepository.create(newMeetup)
        assertThat(id, notNullValue())

        val scenario =
            launchFragmentInHiltContainer<ProfileListFragment>(fragmentFactory = userListFactory)
        scenario!!.use {

            assert(!profileRepository.fetch(userid)!!.following.contains(id2))
            assert(!profileRepository.fetch(id2!!)!!.followed.contains(userid))
            onView(
                RecyclerViewMatcher(R.id.profile_list_recycler).atPositionOnView(
                    1,
                    R.id.followButton
                )
            ).perform(click())
            assert(profileRepository.fetch(userid)!!.following.contains(id2))
            assert(profileRepository.fetch(id2)!!.followed.contains(userid))
            onView(
                RecyclerViewMatcher(R.id.profile_list_recycler).atPositionOnView(
                    1,
                    R.id.followButton
                )
            ).perform(click())
            assert(!profileRepository.fetch(userid)!!.following.contains(id2))
            assert(!profileRepository.fetch(id2)!!.followed.contains(userid))
            onView(
                RecyclerViewMatcher(R.id.profile_list_recycler).atPositionOnView(
                    1,
                    R.id.profile_name
                )
            )
                .perform(click())
        }
    }

    @Test
    fun DisplayAchievementInFragment() = runTest {
        val userid = profileRepository.createProfile(user)
        val id2 = profileRepository.createProfile(user2)
        val userListFactory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
                when (loadFragmentClass(classLoader, className)) {
                    ProfileListFragment::class.java -> ProfileListFragment(listOf(userid!!, id2!!))
                    else -> super.instantiate(classLoader, className)
                }
        }

        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(userid!!)

        assertThat(userid, notNullValue())
        newMeetup = MeetUp(
            "dummy",
            userid!!,
            null,
            eventName,
            eventDescription,
            date1,
            date2,
            Location(0.0, 0.0),
            emptySet(),
            true,
            2,
            mutableListOf(userid, id2!!)
        )
        val id = meetUpRepository.createMeetUp(newMeetup)
        assertThat(id, notNullValue())

        var scenario =
            launchFragmentInHiltContainer<ProfileListFragment>(fragmentFactory = userListFactory)
        scenario!!.use {
            onView(
                RecyclerViewMatcher(R.id.profile_list_recycler).atPositionOnView(
                    0,
                    R.id.AchPic1
                )
            ).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
            profileRepository.editUserProfile(
                userid,
                user.copy(
                    achievements = listOf(
                        Achievement.VERIFIED.string,
                        Achievement.CRYPTOPAL.string,
                        Achievement.PAL_MINER.string
                    )
                )
            )
        }
        scenario = launchFragmentInHiltContainer<ProfileListFragment>(fragmentFactory = userListFactory)
        scenario!!.use {
            onView(
                RecyclerViewMatcher(R.id.profile_list_recycler).atPositionOnView(
                    0,
                    R.id.AchPic1
                )
            ).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(
                RecyclerViewMatcher(R.id.profile_list_recycler).atPositionOnView(
                    0,
                    R.id.AchPic2
                )
            ).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(
                RecyclerViewMatcher(R.id.profile_list_recycler).atPositionOnView(
                    0,
                    R.id.AchPic3
                )
            ).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        }
    }

    @Test
    fun addTagAddToDb() = runTest {
        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", null)
            ), navHostController = navController
        )
        scenario!!.use {
            Intents.init()

            scenario.onHiltFragment<MeetUpCreation> {
                it.viewModel.setLatLng(LatLng(0.0,0.0))
            }

            onView(withId(R.id.et_EventName)).perform(typeText("Meetup name"), click())
            onView(withId(R.id.et_Description)).perform(typeText("Meetup description"), click())
            closeSoftKeyboard()
            onView(withId(R.id.addTagButton)).perform(scrollTo(), click())
            onView(withId(R.id.tag_selector_search)).perform(click(), typeText("working out"))
            closeSoftKeyboard()

            onView(
                RecyclerViewMatcher(R.id.tag_selector_recycler).atPositionOnView(
                    0,
                    R.id.chip
                )
            ).check(matches(withText("working out")))
            onView(
                allOf(
                    withText("working out"),
                    withId(R.id.chip)
                )
            ).perform(click())
            onView(withId(R.id.add_tag_button)).perform(click())
            onView(withId(R.id.bt_Done)).perform(scrollTo(), click())

            Intents.intended(IntentMatchers.hasComponent(MeetUpView::class.java.name))
            Intents.release()

            onView(withId(R.id.tv_ViewEventName)).check(matches(withText("Meetup name")))
            onView(withId(R.id.tv_ViewEventDescritpion)).check(matches(withText("Meetup description")))
            onView(withId(R.id.tag_group)).check(matches(hasChildCount(1)))
        }
    }

    @Test
    fun addTagAndRemoveAddsNothing() = runTest {
        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", null)
            ), navHostController = navController
        )
        scenario!!.use {
            Intents.init()

            scenario.onHiltFragment<MeetUpCreation> {
                it.viewModel.setLatLng(LatLng(0.0,0.0))
            }
            onView(withId(R.id.et_EventName)).perform(typeText("Meetup name"), click())
            onView(withId(R.id.et_Description)).perform(typeText("Meetup description"), click())
            closeSoftKeyboard()
            onView(withId(R.id.addTagButton)).perform(scrollTo(), click())
            onView(withId(R.id.tag_selector_search)).perform(click(), typeText("working out"))
            closeSoftKeyboard()

            onView(
                RecyclerViewMatcher(R.id.tag_selector_recycler).atPositionOnView(
                    0,
                    R.id.chip
                )
            ).check(matches(withText("working out")))
            onView(
                allOf(
                    withText("working out"),
                    withId(R.id.chip)
                )
            ).perform(click())
            onView(withId(R.id.add_tag_button)).perform(click())


            onView(withParent(withId(R.id.tag_group))).perform(ClickCloseIconAction())

            onView(withId(R.id.bt_Done)).perform(scrollTo(), click())

            Intents.intended(IntentMatchers.hasComponent(MeetUpView::class.java.name))
            Intents.release()

            onView(withId(R.id.tv_ViewEventName)).check(matches(withText("Meetup name")))
            onView(withId(R.id.tv_ViewEventDescritpion)).check(matches(withText("Meetup description")))
            onView(withId(R.id.tag_group)).check(matches(hasChildCount(0)))
        }
    }

    @Test
    fun checkErrorWork() = runTest {
        val uid = profileRepository.create(user)
        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(uid)

        val id = meetUpRepository.create(meetup)
        assertThat(id, notNullValue())

        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", null)
            ), navHostController = navController
        )
        scenario.use {
            onView(withId(R.id.bt_Done)).perform(scrollTo(), click())
            onView(withText(R.string.meetup_creation_missing_name_desc_title)).check(
                matches(
                    isDisplayed()
                )
            )
        }
    }

    @Test
    fun testEditButton() = runTest {
        val uid = profileRepository.create(user)
        val mid = meetUpRepository.create(meetup.copy(creatorId = uid!!))
        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(uid)

        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, mid)
        }
        Intents.init()
        ActivityScenario.launch<MeetUpView>(intent)
        onView(withId(R.id.bt_EditMeetup)).perform(click())
        Intents.intended(IntentMatchers.hasComponent(MeetUpEditCompat::class.java.name))
        Intents.intended(IntentMatchers.hasExtra(MEETUP_EDIT, mid))
        Intents.release()
    }

    @Test
    fun testHiddenEditButton() = runTest {
        val id = meetUpRepository.create(meetup)

        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, id)
        }
        ActivityScenario.launch<MeetUpView>(intent)
        onView(withId(R.id.bt_EditMeetup)).check(matches(isNotClickable()))
    }

    @Test
    fun testChatButton() = runTest {
        val uid = profileRepository.create(user)
        val mid = meetUpRepository.create(meetup.copy(participantsId = listOf(uid!!)))
        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(uid)

        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, mid)
        }
        Intents.init()
        ActivityScenario.launch<MeetUpView>(intent)
        onView(withId(R.id.bt_ChatMeetup)).perform(click())
        Intents.intended(IntentMatchers.hasComponent(ChatActivity::class.java.name))
        Intents.intended(IntentMatchers.hasExtra(CHAT, mid))
        Intents.release()
    }

    @Test
    fun testHiddenChatButton() = runTest {
        val mid = meetUpRepository.create(meetup)

        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, mid)
        }

        ActivityScenario.launch<MeetUpView>(intent)
        onView(withId(R.id.bt_ChatMeetup)).check(matches(isNotClickable()))
    }

    @Test
    fun ageAboveMaxReturnsPlusText() = runTest {
        val mid = meetUpRepository.create(meetup2)

        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, mid)
        }

        ActivityScenario.launch<MeetUpView>(intent)
        onView(withId(R.id.tv_ViewAge)).check(matches(withText("13-66+ years")))
    }

    @Test
    fun checkPickers() = runTest {
        val scenario = launchFragmentInHiltContainer<MeetUpCreation>(
            bundleOf(
                Pair("MeetUpId", null)
            ), navHostController = navController
        )
        scenario.use {
            onView(withId(R.id.tv_StartDate)).perform(scrollTo(), click())

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


            onView(withId(R.id.tv_EndDate)).perform(scrollTo(), click())

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
        }
    }

    @Test
    fun testJoinLeaveButton() = runTest {
        val mid = meetUpRepository.create(meetup)
        val uid = profileRepository.create(user2)
        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(uid)
        (timeService as UIMockTimeServiceModule.UIMockTimeService).setDate(date1)

        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, mid)
        }

        ActivityScenario.launch<MeetUpView>(intent)

        // Join
        onView(withId(R.id.bt_JoinMeetup)).perform(betterScrollTo()).perform(click())
        assertThat(meetUpRepository.fetch(mid!!)!!.isParticipating(uid!!), `is`(true))

        // Leave
        onView(withId(R.id.bt_JoinMeetup)).perform(betterScrollTo()).perform(click())
        assertThat(meetUpRepository.fetch(mid)!!.isParticipating(uid), `is`(false))
    }

    @Test
    fun clickOnCreator() = runTest {
        val mid = meetUpRepository.create(meetup)
        val uid = profileRepository.create(user)

        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(uid)
        (timeService as UIMockTimeServiceModule.UIMockTimeService).setDate(date1)

        Intents.init()
        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, mid)
        }

        ActivityScenario.launch<MeetUpView>(intent)
        onView(withId(R.id.tv_ViewEventCreator)).perform(betterScrollTo()).perform(click())

        Intents.intended(allOf(IntentMatchers.hasComponent(ProfileActivity::class.java.name), hasExtra(USER_ID, user.uuid)))
        Intents.release()
    }

    @Test
    fun testJoinLeaveCreatorButton() = runTest {
        val uid = profileRepository.create(user)
        val mid = meetUpRepository.create(meetup.copy(creatorId = uid!!))
        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(uid)
        (timeService as UIMockTimeServiceModule.UIMockTimeService).setDate(date1)

        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, mid)
        }
        ActivityScenario.launch<MeetUpView>(intent)

        onView(withId(R.id.bt_JoinMeetup)).check(matches(isNotClickable()))
    }
}

class ClickCloseIconAction : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(Chip::class.java)
    }

    override fun getDescription(): String {
        return "click drawable "
    }

    override fun perform(uiController: UiController, view: View) {
        val chip = view as Chip//we matched
        chip.performCloseIconClick()
    }


}



// scroll-to action that also works with NestedScrollViews
class BetterScrollToAction:ViewAction by ScrollToAction()
{
    override fun getConstraints():Matcher<View>
    {
        return allOf(
            withEffectiveVisibility(Visibility.VISIBLE),
            isDescendantOfA(anyOf(
                isAssignableFrom(ScrollView::class.java),
                isAssignableFrom(HorizontalScrollView::class.java),
                isAssignableFrom(NestedScrollView::class.java))))
    }
}

// convenience method
fun betterScrollTo():ViewAction
{
    return actionWithAssertions(BetterScrollToAction())
}