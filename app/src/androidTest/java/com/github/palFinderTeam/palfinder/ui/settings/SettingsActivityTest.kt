package com.github.palFinderTeam.palfinder.ui.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity.Companion.COLOR_KEY
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity.Companion.DEF_THEME
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity.Companion.THEME_KEY
import com.github.palFinderTeam.palfinder.ui.settings.SettingsActivity.Companion.WARM_THEME
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class SettingsActivityTest {

    @Test
    fun changeColorChangeTheme() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = Intent(context, SettingsActivity::class.java)
        val scenario = ActivityScenario.launch<SettingsActivity>(intent)
        scenario.use {
            onView(withText("Color themes")).perform(click())
            onView(withText("Warm")).perform(click())

            val sharedPref = context.getSharedPreferences(THEME_KEY, Context.MODE_PRIVATE)
            assertThat(sharedPref.getInt(THEME_KEY, 0), `is`(R.style.palFinder_warm_theme))

            onView(withText("Color themes")).perform(click())
            onView(withText("Default")).perform(click())

            assertThat(sharedPref.getInt(THEME_KEY, 0), `is`(R.style.palFinder_default_theme))
        }
    }

}