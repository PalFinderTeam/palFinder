package com.github.palFinderTeam.palfinder

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.icu.util.Calendar
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.EspressoIdlingResource
import com.github.palFinderTeam.palfinder.utils.image.ImageFetcher
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable

@ExperimentalCoroutinesApi
@HiltAndroidTest
class ProfileActivityTest {

@RunWith(AndroidJUnit4::class)
class
ProfileActivityTest {
    lateinit var p : ProfileUser
    lateinit var pDesc : ProfileUser
    lateinit var pImgHttps : ProfileUser
    lateinit var imgFetch : ImageFetcher

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        imgFetch = object : ImageFetcher {
            override suspend fun fetchImage(): Bitmap? {
                return null
            }
        }
        p = ProfileUser("gerussi", "Louca", "Gerussi", Calendar.getInstance(), ImageInstance("icons/cat.png"))
        pDesc = ProfileUser("gerussi", "Louca", "Gerussi", Calendar.getInstance(), ImageInstance("icons/cat.png"), "Hello world I am cat")
        pImgHttps = ProfileUser(
            "gerussi",
            "Louca",
            "Gerussi",
            Calendar.getInstance(),
            ImageInstance("https://fail"),
            "Hello world I am cat"
        )
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun fullNameIsCorrectlyDisplayed() = runTest {
        val id = profileService.createProfile(userLouca)
        // Create intent with data to inject
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply {
                    putExtra(USER_ID, id)
                }
        // Launch activity
        val scenario = ActivityScenario.launch<GreetingActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileName)).check(
                matches(
                    withText(p.fullName())
                )
            )
        }
    }

    @Test
    fun userHasNoBioDisplaysTitleDifferently() = runTest {
        val id = profileService.createProfile(userLouca)
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id) }
        // Launch activity
        val scenario = ActivityScenario.launch<GreetingActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileAboutTitle)).check(
                matches(
                    withText(getResourceString(R.string.no_desc))
                )
            )
        }
    }

    @Test
    fun userWithBioDisplaysShortBioEntirely(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
            .apply{ putExtra(DUMMY_USER, pDesc as Serializable) }
        // Launch activity
        val scenario = ActivityScenario.launch<GreetingActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileDescription)).check(
                matches(
                    withText("Hello world I am cat")
                )
            )
        }
    }

    @Test
    fun httpLoadImageAndClearCache() = runTest {
        val id = profileService.createProfile(userLouca)
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id) }

        // Launch activity
        val scenario = ActivityScenario.launch<GreetingActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileName)).check(
                matches(
                    withText(p.fullName())
                )
            )
            // Check status of image after cache clear
            pImgHttps.pfp.clearImageCache()
            Assert.assertEquals(ImageInstance.NOT_LOADED, pImgHttps.pfp.imgStatus)
        }
    }

    private fun getResourceString(id: Int): String? {
        val targetContext: Context = InstrumentationRegistry.getTargetContext()
        return targetContext.getResources().getString(id)
    }

}

