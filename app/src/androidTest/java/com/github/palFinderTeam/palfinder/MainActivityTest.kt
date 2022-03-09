package com.github.palFinderTeam.palfinder


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val testRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSendMessage() {
        val message = "username"
        Intents.init()
        onView(ViewMatchers.withId(R.id.mainName))
            .perform(ViewActions.replaceText(message))
            .perform(ViewActions.closeSoftKeyboard())

        onView(ViewMatchers.withId(R.id.mainGoButton))
            .perform(click())

        intended(IntentMatchers.hasExtra(EXTRA_MESSAGE, message))
        Intents.release()
    }

    @Test
    fun logoutMenuButtonActuallyLogout() {

        //lateinit var auth: FirebaseAuth
        //auth = Firebase.auth
        Intents.init()
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        // Click the item.
        onView(withText("Logout"))
            .perform(click())
        intended(hasComponent(LoginActivity::class.java.name))
        Intents.release()
    }
}
