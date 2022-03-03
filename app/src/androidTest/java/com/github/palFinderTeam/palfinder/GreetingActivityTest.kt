package com.github.palFinderTeam.palfinder

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GreetingActivityTest {

    @Test
    fun testGreetingMessage(){
        val message = "username"
        val intent = Intent(getApplicationContext(), GreetingActivity::class.java)
            .apply{
                putExtra(EXTRA_MESSAGE, message)
            }
        val scenario = ActivityScenario.launch<GreetingActivity>(intent)
        try{
            onView(withId(R.id.GreetingMessage)).check(matches(withText("Hello " + message)))
        }finally{
            scenario.close()
        }
    }
}