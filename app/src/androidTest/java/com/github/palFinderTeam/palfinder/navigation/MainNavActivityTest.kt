package com.github.palFinderTeam.palfinder.navigation

import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.github.palFinderTeam.palfinder.profile.ProfileFragment
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.profile.USER_ID
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class MainNavActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var profileRepository: ProfileService

    @Before
    fun setup() {
        hiltRule.inject()

        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID("Michel")
    }


    @Test
    fun logoutMenuButtonActuallyLogout() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), MainNavActivity::class.java)
        init()
        launch<MainNavActivity>(intent)
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)

        // Click the item.
        onView(withText("Logout"))
            .perform(click())
        intended(hasComponent(LoginActivity::class.java.name))
        release()
    }


    @Test
    fun settingsMenuButtonBringsToSettings() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), MainNavActivity::class.java)
        init()
        launch<MainNavActivity>(intent)
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)

        // Click the item.
        onView(withText("Settings"))
            .perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
        release()
    }

    @Test
    fun navBarNavigateCorrectly() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), MainNavActivity::class.java)
        val scenario = launch<MainNavActivity>(intent)
        onView(withId(R.id.nav_bar_create)).perform(click())

        scenario.onActivity {
            assertThat(
                it.findNavController(R.id.main_content).currentDestination?.id,
                `is`(R.id.creation_fragment)
            )
        }

        onView(withId(R.id.nav_bar_find)).perform(click())

        scenario.onActivity {
            assertThat(
                it.findNavController(R.id.main_content).currentDestination?.id,
                `is`(R.id.find_fragment)
            )
        }

        // Change test to fragment check instead of intent when updated
        onView(withId(R.id.nav_bar_profile)).perform(click())
        scenario.onActivity {
            assertThat(
                it.findNavController(R.id.main_content).currentDestination?.id,
                `is`(R.id.profile_fragment)
            )
        }
    }

    @Test
    fun findTabsWorkAsExpected() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), MainNavActivity::class.java)
        val scenario = launch<MainNavActivity>(intent)
        scenario!!.use {
            var navController: NavController? = null
            scenario.onActivity {
                val hostFragment =
                    it.supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
                val fragment = hostFragment.childFragmentManager.fragments[0] as FindFragment
                navController = (fragment.childFragmentManager.findFragmentById(R.id.find_content) as NavHostFragment).navController
            }
            onView(withText(R.string.list)).perform(scrollTo(), click())
            assertThat(navController?.currentDestination?.id, `is`(R.id.list_fragment))
            onView(withText(R.string.map)).perform(scrollTo(), click())
            assertThat(navController?.currentDestination?.id, `is`(R.id.maps_fragment))
        }
    }
}