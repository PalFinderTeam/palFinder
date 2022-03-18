package com.github.palFinderTeam.palfinder

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.icu.util.Calendar
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable


@RunWith(AndroidJUnit4::class)
class
ProfileActivityTest {
    lateinit var p : ProfileUser
    lateinit var pImgHttps : ProfileUser

    @Before
    fun getProfile(){
        p = ProfileUser("gerussi", "Louca", "Gerussi", Calendar.getInstance(), ImageInstance("icons/cat.png"))
        pImgHttps = ProfileUser("gerussi", "Louca", "Gerussi", Calendar.getInstance(), ImageInstance("https://fail"), "Hello world I am cat")
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

    @Test
    fun userHasNoBioDisplaysTitleDifferently(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
            .apply{ putExtra(DUMMY_USER, p as Serializable) }
        // Launch activity
        val scenario = ActivityScenario.launch<GreetingActivity>(intent)
        try{
            onView(ViewMatchers.withId(R.id.userProfileAboutTitle)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText(getResourceString(R.string.no_desc))
                )
            )
        }finally{
            scenario.close()
        }
    }

    @Test
    fun userWithBioDisplaysShortBioEntirely(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
            .apply{ putExtra(DUMMY_USER, pImgHttps as Serializable) }
        // Launch activity
        val scenario = ActivityScenario.launch<GreetingActivity>(intent)
        try{
            onView(ViewMatchers.withId(R.id.userProfileDescription)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText("Hello world I am cat")
                )
            )
        }finally{
            scenario.close()
        }
    }

    @Test
    fun httpLoadImageAndClearCache() = runTest{
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
            .apply{
                putExtra(DUMMY_USER, pImgHttps as Serializable)
            }
        // Launch activity
        val scenario = ActivityScenario.launch<GreetingActivity>(intent)
        try{
            onView(ViewMatchers.withId(R.id.userProfileName)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText(p.fullName())
                )
            )
            // Check status of image after cache clear
            pImgHttps.pfp.clearImageCache()
            Assert.assertEquals(ImageInstance.NOT_LOADED, pImgHttps.pfp.imgStatus)
        }finally{
            scenario.close()
        }
    }


    private fun getResourceString(id: Int): String? {
        val targetContext: Context = InstrumentationRegistry.getTargetContext()
        return targetContext.getResources().getString(id)
    }

}