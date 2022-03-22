package com.github.palFinderTeam.palfinder

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.utils.EspressoIdlingResource
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class ProfileActivityTest {

    private lateinit var userLouca: ProfileUser
    private lateinit var userCat: ProfileUser

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var profileService: ProfileService

    @Before
    fun getProfile() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        hiltRule.inject()

        userLouca = ProfileUser(
            "1234",
            "gerussi",
            "Louca",
            "Gerussi",
            Calendar.getInstance(),
            ImageInstance("icons/cat.png")
        )
        userCat = ProfileUser(
            "12345",
            "gerussi",
            "Louca",
            "Gerussi",
            Calendar.getInstance(),
            ImageInstance("https://fail"),
            "Hello world I am cat"
        )
    }

    @After
    fun cleanUp() {
        (profileService as UIMockProfileServiceModule.UIMockProfileService).clearDB()
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
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(ViewMatchers.withId(R.id.userProfileName)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText(userLouca.fullName())
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
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(ViewMatchers.withId(R.id.userProfileAboutTitle)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText(getResourceString(R.string.no_desc))
                )
            )
        }
    }

    @Test
    fun userWithBioDisplaysShortBioEntirely() = runTest {
        val id = profileService.createProfile(userCat)
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id) }

        // Launch activity
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(ViewMatchers.withId(R.id.userProfileDescription)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText(userCat.description)
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
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(ViewMatchers.withId(R.id.userProfileName)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText(userLouca.fullName())
                )
            )
            // Check status of image after cache clear
            userCat.pfp.clearImageCache()
            Assert.assertEquals(ImageInstance.NOT_LOADED, userCat.pfp.imgStatus)
        }
    }


    private fun getResourceString(id: Int): String? {
        val targetContext: Context = InstrumentationRegistry.getTargetContext()
        return targetContext.getResources().getString(id)
    }

}

