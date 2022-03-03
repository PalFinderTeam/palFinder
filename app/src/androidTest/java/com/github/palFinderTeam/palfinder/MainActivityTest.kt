package com.github.palFinderTeam.palfinder


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val testRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSendMessage(){
        val message = "username"
        Intents.init()
        onView(ViewMatchers.withId(R.id.mainName))
            .perform(ViewActions.replaceText(message))
            .perform(ViewActions.closeSoftKeyboard())

        onView(ViewMatchers.withId(R.id.mainGoButton))
            .perform(ViewActions.click())

        intended(IntentMatchers.hasExtra(EXTRA_MESSAGE, message))
        Intents.release()
        }

}