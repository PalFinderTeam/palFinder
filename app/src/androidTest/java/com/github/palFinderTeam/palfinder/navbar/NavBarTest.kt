package com.github.palFinderTeam.palfinder.navbar

import dagger.hilt.android.testing.HiltAndroidTest

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