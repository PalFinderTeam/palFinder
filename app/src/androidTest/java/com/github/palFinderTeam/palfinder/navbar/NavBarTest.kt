package com.github.palFinderTeam.palfinder.navbar

import android.content.Intent
import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.init
import androidx.test.espresso.intent.Intents.release
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.github.palFinderTeam.palfinder.MainActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.MapsFragment
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreation
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListFragment
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NavBarTest {
//    @get:Rule
//    val hiltRule = HiltAndroidRule(this)
//
//    @Inject
//    lateinit var profilService: ProfileService
//
//    lateinit var user: ProfileUser
//
//    @Before
//    fun setup(){
//        hiltRule.inject()
//
//        val date = Calendar.getInstance()
//        date.set(2022, 2, 1, 1, 0, 0)
//        user = ProfileUser(
//            "user",
//            "Michou",
//            "Jonas",
//            "Martin",
//            date,
//            ImageInstance(""),
//            "Ne la laisse pas tomber"
//        )
//    }
//
//    @Test
//    fun testCreateButton() = runTest {
//        val uuid = profilService.createProfile(user)
//        (profilService as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(uuid)
//
//        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
//        init()
//        ActivityScenario.launch<MainActivity>(intent)
//        Espresso.onView(
//            ViewMatchers.withId(R.id.nav_bar_create)
//        ).perform(ViewActions.click())
//        Intents.intended(IntentMatchers.hasComponent(MeetUpCreation::class.java.name))
//        release()
//    }
//
//    @Test
//    fun testMapButton(){
//        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
//        init()
//        ActivityScenario.launch<MainActivity>(intent)
//        Espresso.onView(
//            ViewMatchers.withId(R.id.nav_bar_find)
//        ).perform(ViewActions.click())
//        Intents.intended(IntentMatchers.hasComponent(MapsFragment::class.java.name))
//        release()
//    }
//
//    @Test
//    fun testGroupButton(){
//        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
//        init()
//        ActivityScenario.launch<MainActivity>(intent)
//        Espresso.onView(
//            ViewMatchers.withId(R.id.nav_bar_groups)
//        ).perform(ViewActions.click())
//        Intents.intended(IntentMatchers.hasComponent(MeetupListFragment::class.java.name))
//        Intents.intended(IntentMatchers.hasExtra(SHOW_JOINED_ONLY, true))
//        release()
//    }
}