package com.github.palFinderTeam.palfinder


import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.github.palFinderTeam.palfinder.map.MapsActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.github.palFinderTeam.palfinder.meetups.activities.RecyclerViewMatcher
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    @Inject
    lateinit var profileRepository: ProfileService

    @Before
    fun setup(){
        hiltRule.inject()

        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID("dummy")
    }

    @Test
    fun logoutMenuButtonActuallyLogout() {
        //lateinit var auth: FirebaseAuth
        //auth = Firebase.auth
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        // Click the item.
        onView(withText("Logout"))
            .perform(click())
        intended(hasComponent(LoginActivity::class.java.name))
        release()
    }

    @Test
    fun openMeetupCreationPageIntentTest() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        onView(ViewMatchers.withId(R.id.mainGoButton3))
            .perform(click())
        intended(hasComponent(MeetUpCreation::class.java.name))
        release()
    }

    @Test
    fun profileLoucaTest() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        onView(ViewMatchers.withId(R.id.accessImgLoader))
            .perform(click())
        intended(hasComponent(ProfileActivity::class.java.name))
        release()
    }

    @Test
    fun profileCatTest() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        onView(ViewMatchers.withId(R.id.accessImgLoader2))
            .perform(click())
        intended(hasComponent(ProfileActivity::class.java.name))
        release()
    }

    @Test
    fun meetupListTest() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        onView(ViewMatchers.withId(R.id.mainGoButton))
            .perform(click())
        intended(hasComponent(MeetupListActivity::class.java.name))
        release()
    }

    @Test
    fun mapTest() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        onView(ViewMatchers.withId(R.id.mapGoButton))
            .perform(click())
        intended(hasComponent(MapsActivity::class.java.name))
        release()
    }

    @Test
    fun userSettingsTest() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        init()
        ActivityScenario.launch<MainActivity>(intent)
        onView(ViewMatchers.withId(R.id.mapGoButton2))
            .perform(click())
        intended(hasComponent(UserSettingsActivity::class.java.name))
        release()
    }
}
