package com.github.palFinderTeam.palfinder

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable
import java.util.*


@RunWith(AndroidJUnit4::class)
class ProfileActivityTest {
    lateinit var p : ProfileUser

    @Before
    fun getProfile(){
        val joinDate = Date(122, 2, 6, 14, 1, 0)
        p = ProfileUser("gerussi", "Louca", "Gerussi", joinDate)
    }

    @Test
    fun fullNameIsCorrectlyDisplayed(){
        // Create intent with data to inject
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
            .apply{
                putExtra(DUMMY_USER, p as Serializable)
            }
        // Launch activity
        val scenario = ActivityScenario.launch<GreetingActivity>(intent)
        try{
            onView(ViewMatchers.withId(R.id.userProfileName)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText(p.fullName())
                )
            )
        }finally{
            scenario.close()
        }
    }
}