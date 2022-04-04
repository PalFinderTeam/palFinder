package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.UIMockMeetUpRepositoryModule
import com.github.palFinderTeam.palfinder.chat.CHAT
import com.github.palFinderTeam.palfinder.chat.ChatActivity
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.android.material.chip.Chip
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.notNullValue
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
    private lateinit var user: ProfileUser
    private val eventName = "dummy1"
    private val eventDescription = "dummy2"
    private lateinit var date1: Calendar
    private lateinit var date2: Calendar

    private val format = SimpleDateFormat("EEEE, MMMM d, yyyy \'at\' h:mm a")
    private lateinit var expectDate2: String
    private lateinit var expectDate1: String

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var meetUpRepository: MeetUpRepository
    @Inject
    lateinit var profileRepository: ProfileService

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
        meetup = MeetUp(
            "dummy",
            "user",
            "",
            eventName,
            eventDescription,
            date1,
            date2,
            Location(0.0, 0.0),
            emptySet(),
            true,
            2,
            mutableListOf("user")
        )
    }

    @After
    fun cleanUp() {
        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).clearDB()
        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).clearDB()
    }

    @Test
    fun editExistingMeetupDisplayRightFields() = runTest {
        val id = meetUpRepository.createMeetUp(meetup)
        assertThat(id, notNullValue())

        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
            .apply {
                putExtra(MEETUP_EDIT, id)
            }
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            onView(withId(R.id.et_EventName)).check(matches(withText(eventName)))
            onView(withId(R.id.et_Description)).check(matches(withText(eventDescription)))
            onView(withId(R.id.tv_StartDate)).check(matches(withText(expectDate1)))
            onView(withId(R.id.tv_EndDate)).check(matches(withText(expectDate2)))
            onView(withId(R.id.et_Capacity)).check(matches(isEnabled()))
            onView(withId(R.id.hasCapacityButton)).check(matches(isChecked()))
        }
    }

/*
    @Test
    fun editExistingMeetupEditTheRightOneInDB() = runTest {

        val id = meetUpRepository.createMeetUp(meetup)
        assertThat(id, notNullValue())

        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
            .apply {
                putExtra(MEETUP_EDIT, id)
            }
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
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
*/

    @Test
    fun createMeetUpDisplayBlankInfo() = runTest {
        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            onView(withId(R.id.et_EventName)).check(matches(withText("")))
            onView(withId(R.id.et_Description)).check(matches(withText("")))
            onView(withId(R.id.et_Capacity)).check(matches(isNotEnabled()))
            onView(withId(R.id.hasCapacityButton)).check(matches(isNotChecked()))
        }
    }

    @Test
    fun capacityFieldMatchesCapacityCheckBox() = runTest {

        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
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
        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            Intents.init()

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
    fun profileFragmentCorrectlyDisplayed() = runTest {
        val userid = profileRepository.createProfile(user)
        assertThat(userid, notNullValue())
        val newMeetup = MeetUp(
            "dummy",
            userid!!,
            "",
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
        val id = meetUpRepository.createMeetUp(newMeetup)
        assertThat(id, notNullValue())
        val intent = Intent(getApplicationContext(), MeetUpView::class.java)
            .apply{putExtra(MEETUP_SHOWN, id)}
        val scenario = ActivityScenario.launch<MeetUpView>(intent)
        scenario.use {
            onView(withId(R.id.show_profile_list_button)).perform(click())
        }
    }

//    @Test
//    fun userClickableInFragment() = runTest {
//        val userid = profileRepository.createProfile(user)
//        assertThat(userid, notNullValue())
//        val newMeetup = MeetUp(
//            "dummy",
//            userid!!,
//            "",
//            eventName,
//            eventDescription,
//            date1,
//            date2,
//            Location(0.0, 0.0),
//            emptySet(),
//            true,
//            2,
//            mutableListOf(userid)
//        )
//        val id = meetUpRepository.createMeetUp(newMeetup)
//        assertThat(id, notNullValue())
//        val intent = Intent(getApplicationContext(), MeetUpView::class.java)
//            .apply{putExtra(MEETUP_SHOWN, id)}
//        val scenario = ActivityScenario.launch<MeetUpView>(intent)
//        scenario.use {
//            onView(withId(R.id.show_profile_list_button)).perform(click())
//            onView(
//                RecyclerViewMatcher(R.id.profile_list_recycler).atPositionOnView(
//                    0,
//                    R.id.profile_name
//                )
//            )
//                .perform(click())
//        }
//
//    }

    @Test
    fun clickOnEditWorks() = runTest {
        val id = meetUpRepository.createMeetUp(meetup)
        assertThat(id, notNullValue())
        val intent = Intent(getApplicationContext(), MeetUpView::class.java)
            .apply{putExtra(MEETUP_SHOWN, id)}
        ActivityScenario.launch<MeetUpView>(intent)
        init()
        onView(withId(R.id.bt_EditMeetup)).perform(click())
        intended(hasComponent(MeetUpCreation::class.java.name))
        release()

    }

    @Test
    fun clickOnChatWorks() = runTest {
        val id = meetUpRepository.createMeetUp(meetup)
        assertThat(id, notNullValue())
        val intent = Intent(getApplicationContext(), MeetUpView::class.java)
            .apply{putExtra(MEETUP_SHOWN, id)}
        ActivityScenario.launch<MeetUpView>(intent)
        init()
        onView(withId(R.id.bt_ChatMeetup)).perform(click())
        intended(hasComponent(ChatActivity::class.java.name))
        release()

    }

    @Test
    fun addTagAddToDb() = runTest {
        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            Intents.init()

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
            onView(allOf(
                withText("working out"),
                withId(R.id.chip)
            )).perform(click())
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
        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            Intents.init()

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
            onView(allOf(
                withText("working out"),
                withId(R.id.chip)
            )).perform(click())
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
        val id = meetUpRepository.createMeetUp(meetup)
        assertThat(id, notNullValue())

        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            onView(withId(R.id.bt_Done)).perform(scrollTo(), click())
            onView(withText(R.string.meetup_creation_missing_name_desc_title)).check(matches(isDisplayed()));
        }
    }

    @Test
    fun testEditButton(){ runTest {
        val id = meetUpRepository.createMeetUp(meetup)
        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, id)
        }
        Intents.init()
        ActivityScenario.launch<MeetUpView>(intent)
        onView(withId(R.id.bt_EditMeetup)).perform(click())
        Intents.intended(IntentMatchers.hasComponent(MeetUpCreation::class.java.name))
        Intents.intended(IntentMatchers.hasExtra(MEETUP_EDIT, id))
        Intents.release()
    }
    }

    @Test
    fun testChatButton() = runTest {
        val id = meetUpRepository.createMeetUp(meetup)
        val intent = Intent(getApplicationContext(), MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, id)
        }
        Intents.init()
        ActivityScenario.launch<MeetUpView>(intent)
        onView(withId(R.id.bt_ChatMeetup)).perform(click())
        Intents.intended(IntentMatchers.hasComponent(ChatActivity::class.java.name))
        Intents.intended(IntentMatchers.hasExtra(CHAT, id))
        Intents.release()
    }

    @Test
    fun checkPickers() = runTest {
        val intent = Intent(getApplicationContext(), MeetUpCreation::class.java)
        val scenario = ActivityScenario.launch<MeetUpCreation>(intent)
        scenario.use {
            onView(withId(R.id.tv_StartDate)).perform(scrollTo(), click())

            onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
                PickerActions.setDate(date1.get(Calendar.YEAR), date1.get(Calendar.MONTH)+1, date1.get(Calendar.DAY_OF_MONTH)),
            )
            onView(withText("OK")).perform(click()) // Library is stupid and can't even press the f. button
            onView(withClassName(Matchers.equalTo(TimePicker::class.java.name))).perform(
                PickerActions.setTime(date1.get(Calendar.HOUR_OF_DAY), date1.get(Calendar.MINUTE)),
            )
            onView(withText("OK")).perform(click())
            onView(withId(R.id.tv_StartDate)).check(matches(withText(expectDate1)))


            onView(withId(R.id.tv_EndDate)).perform(scrollTo(), click())

            onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
                PickerActions.setDate(date2.get(Calendar.YEAR), date2.get(Calendar.MONTH) + 1, date2.get(Calendar.DAY_OF_MONTH)),
            )
            onView(withText("OK")).perform(click()) // Library is stupid and can't even press the f. button
            onView(withClassName(Matchers.equalTo(TimePicker::class.java.name))).perform(
                PickerActions.setTime(date2.get(Calendar.HOUR_OF_DAY), date2.get(Calendar.MINUTE)),
            )
            onView(withText("OK")).perform(click())
            onView(withId(R.id.tv_EndDate)).check(matches(withText(expectDate2)))
        }
    }
}

class ClickCloseIconAction : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(Chip::class.java)
    }

    override fun getDescription(): String {
        return "click drawable "
    }

    override fun perform(uiController: UiController, view: View) {
        val chip = view as Chip//we matched
        chip.performCloseIconClick()
    }


}