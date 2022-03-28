package com.github.palFinderTeam.palfinder.user.settings

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.github.palFinderTeam.palfinder.ProfileActivity
import com.github.palFinderTeam.palfinder.R
import org.junit.Test

class UserSettingsActivityTest {
    @Test
    fun fullNameIsCorrectlyDisplayed() {
        // Create intent with data to inject
        val intent = Intent(ApplicationProvider.getApplicationContext(), UserSettingsActivity::class.java)
        // Launch activity
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            Espresso.onView(ViewMatchers.withId(R.id.settingsConfirm)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText("Save settings")
                )
            )
        }
    }
}