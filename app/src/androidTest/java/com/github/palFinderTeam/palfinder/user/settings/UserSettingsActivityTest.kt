package com.github.palFinderTeam.palfinder.user.settings

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.widget.DatePicker
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.github.palFinderTeam.palfinder.*
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.ui.login.CREATE_ACCOUNT_PROFILE
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class UserSettingsActivityTest {

    @Inject
    lateinit var profileService: ProfileService
    lateinit var viewModel: UserSettingsViewModel
    lateinit var user: ProfileUser
    lateinit var bdFormat: SimpleDateFormat
    lateinit var bDay: Calendar

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()

        // Set up static birthday for tests
        bDay = Calendar.getInstance()
        bDay.set(2000,3,10)

        // User
        user = ProfileUser(
            "0",
            "cato",
            "taco",
            "maco",
            Calendar.getInstance(),
            ImageInstance("null"),
            "The cato is backo...",
            bDay
        )

        bdFormat = SimpleDateFormat("d/M/y")

        // Create viewModel with mock profile service
        profileService = UIMockProfileServiceModule.provideProfileService()

        viewModel = UserSettingsViewModel(profileService)
        //Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun cleanUp() {
        (profileService as UIMockProfileServiceModule.UIMockProfileService).clearDB()
    }


//    @Test
//    fun loadSpecificUserIdYieldsCorrectValues() = runTest {
//        assertThat(profileService.createProfile(user), notNullValue())
//
//        val intent = Intent(ApplicationProvider.getApplicationContext(), UserSettingsActivity::class.java)
//            .apply{
//                putExtra(USER_ID, user.uuid)
//            }
//
//        val scenario = ActivityScenario.launch<UserSettingsActivity>(intent)
//        scenario.use {
//            onView(withId(R.id.SettingsUsernameText))
//                .check(matches(withText(user.username)))
//            onView(withId(R.id.SettingsNameText))
//                .check(matches(withText(user.name)))
//            onView(withId(R.id.SettingsSurnameText))
//                .check(matches(withText(user.surname)))
//            onView(withId(R.id.SettingsBioText))
//                .check(matches(withText(user.description)))
//            onView(withId(R.id.SettingsBirthdayText))
//                .check(matches(withText(bdFormat.format(user.birthday))))
//        }
//    }

    @Test
    fun loadSpecificUserProfileYieldsCorrectValues() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        val scenario = ActivityScenario.launch<UserSettingsActivity>(intent)
        scenario.use {
            onView(withId(R.id.SettingsUsernameText))
                .check(matches(withText(user.username)))
            onView(withId(R.id.SettingsNameText))
                .check(matches(withText(user.name)))
            onView(withId(R.id.SettingsSurnameText))
                .check(matches(withText(user.surname)))
            onView(withId(R.id.SettingsBioText))
                .check(matches(withText(user.description)))
            onView(withId(R.id.SettingsBirthdayText))
                .check(matches(withText(bdFormat.format(user.birthday))))
        }
    }

    @Test
    fun submittingCorrectDataSendsToMeetupListActivity() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        ActivityScenario.launch<UserSettingsActivity>(intent)
        Intents.init()
        onView(withId(R.id.SettingsSubmitButton)).perform(ViewActions.scrollTo(), click())
        Intents.intended(IntentMatchers.hasComponent(MeetupListActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun submittingBadDataKeepsOnSameActivity() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserSettingsActivity::class.java)
        val scenario = ActivityScenario.launch<UserSettingsActivity>(intent)

        scenario.use {
            onView(withId(R.id.SettingsDeleteBDay)).perform(ViewActions.scrollTo(), click())
            // Check if a field is still empty => stayed on same page
            onView(withId(R.id.SettingsUsernameText)).check(matches(withText("")))
        }
    }

    @Test
    fun calendarOpensAndSetsDesiredTextInField() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserSettingsActivity::class.java)

        val scenario = ActivityScenario.launch<UserSettingsActivity>(intent)

        scenario.use {
            onView(withId(R.id.SettingsBirthdayText)).perform(ViewActions.scrollTo(), click())
            val bDay = user.birthday!!

            onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
                PickerActions.setDate(bDay.get(Calendar.YEAR), bDay.get(Calendar.MONTH)+1, bDay.get(Calendar.DAY_OF_MONTH)),
            )
            onView(withText("OK")).perform(click()) // Library is stupid and can't even press the f. button

            onView(withId(R.id.SettingsBirthdayText)).check(matches(withText(bdFormat.format(user.birthday))))
        }


    }

    @Test
    fun clearBirthdayEmptiesField() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        val scenario = ActivityScenario.launch<UserSettingsActivity>(intent)
        scenario.use {
            onView(withId(R.id.SettingsBirthdayText))
                .check(matches(withText(bdFormat.format(user.birthday))))
            onView(withId(R.id.SettingsDeleteBDay)).perform(click())
            onView(withId(R.id.SettingsBirthdayText))
                .check(matches(withText("")))
        }
    }

}