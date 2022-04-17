package com.github.palFinderTeam.palfinder.navigation

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
        Intents.init()
        ActivityScenario.launch<MainNavActivity>(intent)
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)

        // Click the item.
        Espresso.onView(ViewMatchers.withText("Logout"))
            .perform(ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(LoginActivity::class.java.name))
        Intents.release()
    }


    @Test
    fun settingsMenuButtonBringsToSettings() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), MainNavActivity::class.java)
        Intents.init()
        ActivityScenario.launch<MainNavActivity>(intent)
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)

        // Click the item.
        Espresso.onView(ViewMatchers.withText("Settings"))
            .perform(ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(SettingsActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun navBarNavigateCorrectly() {

    }
}