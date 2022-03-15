package com.github.palFinderTeam.palfinder

/**
@RunWith(AndroidJUnit4::class)
class MapsActivityTest {

    @get:Rule
    val testRule = ActivityScenarioRule(MapsActivity::class.java)
    @get:Rule
    var fineLocationPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)
    @get:Rule
    var coarseLocationPermissionRule : GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)



    @Test
    fun testClickOnMarker(){
        val uuid = "uuid"
        val lat = 0.0
        val long = 0.0
        val date1 = Calendar.getInstance()
        date1!!.set(2022, 2,1,0,0,0)
        val date2 = Calendar.getInstance()
        date2!!.set(2022, 2,1,1,0,0)
        val meetup = MeetUp(
            uuid,
            TempUser("", "Bob"),
            "",
            "title",
            "eventDescription",
            date1,
            date2,
            com.github.palFinderTeam.palfinder.utils.Location(long, lat),
            emptyList(),
            true,
            2,
            mutableListOf(TempUser("", "Alice"))
        )

        MapsActivity.setBaseLocation(LatLng(lat, long))

        MapsActivity.addMeetUpMarker(meetup)

        Intents.init()
        val device = UiDevice.getInstance(getInstrumentation())
        val marker = device.findObject(UiSelector().descriptionContains("Google Map").childSelector(UiSelector().descriptionContains(uuid)))
        marker.waitForExists(5000)
        marker.click()
        intended(IntentMatchers.hasExtra(MEETUP_SHOWN, meetup))
        Intents.release()
    }
}
        **/