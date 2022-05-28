package com.github.palFinderTeam.palfinder.profile

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.icu.util.Calendar
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ListView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.JOINED_MEETUPS_KEY
import com.github.palFinderTeam.palfinder.utils.EspressoIdlingResource
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.PrivacySettings
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.image.QRCode
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.notNullValue
import org.junit.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class ProfileFragmentTest {

    private lateinit var userLouca: ProfileUser
    private lateinit var userCat: ProfileUser
    private lateinit var userLongBio: ProfileUser
    private lateinit var userNoBio: ProfileUser
    private lateinit var userPrivate: ProfileUser
    private lateinit var someUser: ProfileUser

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var profileService: ProfileService

    @Inject
    lateinit var meetupService: MeetUpRepository

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
            "I am cat"
        )
        userLongBio = ProfileUser(
            "123456",
            "vlong",
            "Very",
            "Long",
            Calendar.getInstance(),
            ImageInstance(""),
            "Hello I am still a cat but now with a longer description. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam."
        )
        userNoBio = ProfileUser(
            "123",
            "no",
            "No",
            "Bio",
            Calendar.getInstance(),
            ImageInstance(""),
            "",
            achievements = Achievement.values().map { it.aName }
        )

        userPrivate = ProfileUser(
            "42",
            "somePrivateUser",
            "private",
            "user",
            Calendar.getInstance(),
            ImageInstance(""),
            "I like my private life",
            privacySettings = PrivacySettings.PRIVATE
        )

        someUser = ProfileUser(
            "1",
            "The",
            "only",
            "one",
            Calendar.getInstance(),
            ImageInstance("")
        )
    }

    @Before
    fun setBaseUser() = runTest {
        val id = profileService.create(someUser)
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(id)
    }

    @After
    fun cleanUp() {
        (profileService as UIMockProfileServiceModule.UIMockProfileService).clearDB()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun fullNameIsCorrectlyDisplayed() = runTest {
        val id = profileService.create(userLouca)
        // Create intent with data to inject
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply {
                    putExtra(USER_ID, id)
                }
        // Launch activity
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileName)).check(
                matches(
                    withText(userLouca.fullName())
                )
            )
        }
    }

    @Test
    fun userHasNoBioDisplaysTitleDifferently() = runTest {
        val id = profileService.create(userLouca)
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id) }
        // Launch activity
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileAboutTitle)).check(
                matches(
                    withText(getResourceString(R.string.no_desc))
                )
            )
        }
    }

    @Test
    fun userWithBioDisplaysShortBioEntirely() = runTest {
        val id = profileService.create(userCat)
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id) }

        // Launch activity
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileDescription)).check(
                matches(
                    withText(userCat.description)
                )
            )
        }
    }

    @Test
    fun noBioHasNoReadMore() = runTest {
        val id = profileService.create(userNoBio)
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id) }

        // Launch activity
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileDescOverflow)).check(
                matches(
                    withEffectiveVisibility(Visibility.GONE)
                )
            )
        }
    }

    @Test
    fun userExpandsBioCorrectly() = runTest {
        val id = profileService.create(userLongBio)
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id) }

        val scenario = ActivityScenario.launch<ProfileActivity>(intent)

        scenario.use {
            onView(withId(R.id.userProfileDescOverflow)).perform(
                NestedScrollViewScrollTo(),
                click()
            )
            onView(withId(R.id.userProfileDescription)).check(matches(withText(userLongBio.description)))
        }
    }

    @Test
    fun httpLoadImageAndClearCache() = runTest {
        val id = profileService.create(userLouca)
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id) }

        // Launch activity
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileName)).check(
                matches(
                    withText(userLouca.fullName())
                )
            )
            // Check status of image after cache clear
            userCat.pfp.clearImageCache()
            Assert.assertEquals(ImageInstance.NOT_LOADED, userCat.pfp.imgStatus)
        }
    }

    @Test
    fun testPrivateUserProfile() = runTest {
        val uuid = profileService.create(userPrivate)

        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, uuid) }

        // Launch activity
        val scenario = ActivityScenario.launch<ProfileActivity>(intent)
        scenario.use {
            onView(withId(R.id.userProfileDescription)).check(
                matches(
                    withText(R.string.private_desc)
                )
            )
        }
    }


    private fun getResourceString(id: Int): String {
        val targetContext: Context = InstrumentationRegistry.getTargetContext()
        return targetContext.resources.getString(id)
    }


    @Test
    fun followWorksOnProfileView() = runTest {
        val userid = profileService.create(userLouca)
        val id2 = profileService.create(userCat)

        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(userid!!)

        assertThat(userid, notNullValue())

        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id2) }
        val scenario =
            ActivityScenario.launch<ProfileActivity>(intent)
        scenario!!.use {

            assert(!profileService.fetch(userid)!!.following.contains(id2))
            assert(!profileService.fetch(id2!!)!!.followed.contains(userid))
            onView(
                withId(R.id.button_follow_profile)
            ).perform(click())
            assert(profileService.fetch(userid)!!.following.contains(id2))
            assert(profileService.fetch(id2)!!.followed.contains(userid))
            onView(
                withId(R.id.button_follow_profile)
            ).perform(click())
            assert(!profileService.fetch(userid)!!.following.contains(id2))
            assert(!profileService.fetch(id2)!!.followed.contains(userid))
        }
    }

    @Test
    fun canBlockUnBlock() = runTest {
        val userid = profileService.create(userLouca)
        val id2 = profileService.create(userCat)
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(userid)

        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id2) }
        val scenario =
            ActivityScenario.launch<ProfileActivity>(intent)
        scenario!!.use {

            assert(!profileService.fetch(userid!!)!!.blockedUsers.contains(id2))
            onView(withId(R.id.blackList)).perform(NestedScrollViewScrollTo(), click())
            assert(profileService.fetch(userid)!!.blockedUsers.contains(id2))
            onView(withId(R.id.blackList)).perform(NestedScrollViewScrollTo(), click())
            assert(!profileService.fetch(userid)!!.blockedUsers.contains(id2))
        }
    }

    @Test
    fun blockLeaveMeetupFromBlocked() = runTest {
        val userid = profileService.create(userLouca)
        val id2 = profileService.create(userCat)
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(userid)
        val meetup = MeetUp(
            "",
            id2!!,
            null,
            "fefe",
            "efefe",
            Calendar.getInstance(),
            Calendar.getInstance(),
            Location(1.0, 2.0),
            emptySet(),
            false,
            33,
            listOf(id2, userid!!)
        )
        val meetupId = meetupService.create(meetup)
        profileService.edit(userid, JOINED_MEETUPS_KEY, listOf(meetupId!!))
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id2) }
        val scenario =
            ActivityScenario.launch<ProfileActivity>(intent)
        scenario!!.use {

            assert(profileService.fetch(userid)!!.joinedMeetUps.contains(meetupId))
            onView(withId(R.id.blackList)).perform(NestedScrollViewScrollTo(), click())
            assert(!meetupService.fetch(meetupId)!!.participantsId.contains(userid))
        }
    }

    @Test
    fun qrCodeSaveExternalWorks() = runTest {
        val userid = profileService.create(userLouca)
        val id2 = profileService.create(userCat)
        (profileService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(userid)

        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ProfileActivity::class.java)
                .apply { putExtra(USER_ID, id2) }
        val scenario =
            ActivityScenario.launch<ProfileActivity>(intent)

        scenario.onActivity {
            //Initiate the barcode encoder
            val barcodeEncoder = BarcodeEncoder()
            //Encode text in editText into QRCode image into the specified size using barcodeEncoder
            val bitmap = barcodeEncoder.encodeBitmap(
                MEETUP_SHOWN,
                BarcodeFormat.QR_CODE,
                it.resources.getInteger(R.integer.QR_size),
                it.resources.getInteger(
                    R.integer.QR_size
                )
            )
            val uri = QRCode.saveImageExternal(bitmap, it)
            val decodedUri =
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(it.contentResolver, uri!!));
            assert(decodedUri.byteCount == bitmap.byteCount)
        }
    }
}

/**
 * Taken from https://medium.com/@devasierra/espresso-nestedscrollview-scrolling-via-kotlin-delegation-5e7f0aa64c09
 */
class NestedScrollViewScrollTo(scrollToAction: ViewAction = scrollTo()) :
    ViewAction by scrollToAction {
    override fun getConstraints(): Matcher<View> {
        return Matchers.allOf(
            withEffectiveVisibility(Visibility.VISIBLE),
            isDescendantOfA(
                Matchers.anyOf(
                    isAssignableFrom(NestedScrollView::class.java),
                    isAssignableFrom(ScrollView::class.java),
                    isAssignableFrom(HorizontalScrollView::class.java),
                    isAssignableFrom(ListView::class.java)
                )
            )
        )
    }
}
