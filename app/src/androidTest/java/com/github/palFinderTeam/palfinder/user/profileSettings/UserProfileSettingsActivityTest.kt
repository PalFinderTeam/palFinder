package com.github.palFinderTeam.palfinder.user.profileSettings

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.widget.DatePicker
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.github.palFinderTeam.palfinder.*
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity
import com.github.palFinderTeam.palfinder.profile.services.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.profile.USER_ID
import com.github.palFinderTeam.palfinder.login.CREATE_ACCOUNT_PROFILE
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.PrivacySettings
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.image.ImageUploader
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltAndroidTest
class UserProfileSettingsActivityTest {

    @Inject
    lateinit var profileService: ProfileService
    @Inject
    lateinit var imageUploader: ImageUploader
    @Inject
    lateinit var timeService: TimeService

    lateinit var viewModel: UserProfileSettingsViewModel
    lateinit var user: ProfileUser
    lateinit var bdFormat: SimpleDateFormat
    lateinit var bDay: Calendar

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() = runTest{
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
            bDay,
            gender = Gender.MALE,
            privacySettings = PrivacySettings.PUBLIC
        )

        bdFormat = SimpleDateFormat("d/M/y")

        // Create viewModel with mock profile service
        profileService = UIMockProfileServiceModule.provideProfileService()
        val uuid = profileService.create(user)
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(uuid)
        viewModel = UserProfileSettingsViewModel(profileService, imageUploader, timeService)
        //Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun cleanUp() {
        (profileService as UIMockProfileServiceModule.UIMockProfileService).clearDB()
    }


    @Test
    fun editExistingUserEditRightInDb() = runTest {

        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
            .apply{
                putExtra(USER_ID, profileService.getLoggedInUserID()!!)
            }

        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)
        scenario.use {
            onView(withId(R.id.SettingsUsernameText))
                .perform(clearText(), typeText("toto"))
            onView(withId(R.id.SettingsSubmitButton)).perform(scrollTo(), click())
            assertThat(profileService.fetch(profileService.getLoggedInUserID()!!)?.username, `is`("toto"))
        }
    }

    @Test
    fun loadExistingUserProfileYieldsCorrectValues() = runTest {

        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
            .apply{
                putExtra(USER_ID, profileService.getLoggedInUserID()!!)
            }

        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)
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
                .perform(scrollTo())
                .check(matches(withText(bdFormat.format(user.birthday))))
        }
    }

    @Test
    fun loadSpecificUserProfileYieldsCorrectValues() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)
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
                .perform(scrollTo())
                .check(matches(withText(bdFormat.format(user.birthday))))
        }
    }

    @Test
    fun loadSpecificUserProfileYieldsGenderCorrectly() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        // Male
        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)
        scenario.use {
            onView(withId(R.id.radioMale))
                .check(matches(isChecked()))
            onView(withId(R.id.radioFemale))
                .check(matches(not(isChecked())))
            onView(withId(R.id.radioOther))
                .check(matches(not(isChecked())))
        }
    }

    @Test
    fun selectingGendersModifiesRadios() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)
        scenario.use {
            // Female
            onView(withId(R.id.radioFemale))
                .perform(scrollTo(), click())
            onView(withId(R.id.radioMale))
                .check(matches(not(isChecked())))
            onView(withId(R.id.radioFemale))
                .check(matches(isChecked()))
            onView(withId(R.id.radioOther))
                .check(matches(not(isChecked())))

            // Other
            onView(withId(R.id.radioOther))
                .perform(scrollTo(), click())
            onView(withId(R.id.radioMale))
                .check(matches(not(isChecked())))
            onView(withId(R.id.radioFemale))
                .check(matches(not(isChecked())))
            onView(withId(R.id.radioOther))
                .check(matches(isChecked()))
        }
    }

    @Test
    fun loadSpecificUserProfileYieldsPrivacySettingsCorrectly() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        // Male
        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)
        scenario.use {
            onView(withId(R.id.radioPublic))
                .check(matches(isChecked()))
            onView(withId(R.id.radioFriends))
                .check(matches(not(isChecked())))
            onView(withId(R.id.radioPrivate))
                .check(matches(not(isChecked())))
        }
    }

    @Test
    fun selectingPrivacySettingsModifiesRadios() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)
        scenario.use {
            onView(withId(R.id.radioFriends))
                .perform(scrollTo(), click())
            onView(withId(R.id.radioPrivate))
                .check(matches(not(isChecked())))
            onView(withId(R.id.radioFriends))
                .check(matches(isChecked()))
            onView(withId(R.id.radioPublic))
                .check(matches(not(isChecked())))

            onView(withId(R.id.radioPrivate))
                .perform(scrollTo(), click())
            onView(withId(R.id.radioPublic))
                .check(matches(not(isChecked())))
            onView(withId(R.id.radioFriends))
                .check(matches(not(isChecked())))
            onView(withId(R.id.radioPrivate))
                .check(matches(isChecked()))
        }
    }

    @Test
    fun submittingCorrectDataSendsToMeetupListActivity() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        ActivityScenario.launch<UserProfileSettingsActivity>(intent)
        Intents.init()
        onView(withId(R.id.SettingsSubmitButton)).perform(scrollTo(), click())
        Intents.intended(IntentMatchers.hasComponent(MainNavActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun submittingBadDataKeepsOnSameActivity() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)

        Intents.init()
        scenario.use {
            onView(withId(R.id.SettingsDeleteBDay)).perform(scrollTo(), click())
            assertThat(Intents.getIntents().size, `is`(0))
        }
        Intents.release()
    }

    @Test
    fun calendarOpensAndSetsDesiredTextInField() = runTest {
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)

        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)

        scenario.use {

            onView(withId(R.id.SettingsBirthdayText)).perform(scrollTo(), click())
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
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserProfileSettingsActivity::class.java)
            .apply{
                putExtra(CREATE_ACCOUNT_PROFILE, user)
            }

        val scenario = ActivityScenario.launch<UserProfileSettingsActivity>(intent)
        scenario.use {
            onView(withId(R.id.SettingsBirthdayText))
                .perform(scrollTo())
                .check(matches(withText(bdFormat.format(user.birthday))))
            onView(withId(R.id.SettingsDeleteBDay)).perform(click())
            onView(withId(R.id.SettingsBirthdayText))
                .check(matches(withText("")))
        }
    }

//    @Test
//    fun changeGenderYields

}