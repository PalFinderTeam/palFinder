package com.github.palFinderTeam.palfinder.map

import android.content.Intent
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltAndroidTest
class MapsViewModelTestComplement {


    @Inject
    lateinit var meetUpRepository: MeetUpRepository
    @Inject
    lateinit var profileService: ProfileService


    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var fineLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    var coarseLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)



    @Before
    fun init_() {
        hiltRule.inject()
    }
    
    @Test
    fun testSetZoom(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)

        val scenario = ActivityScenario.launch<MapsActivity>(intent)
        var zoom: Float = 15f
        scenario.use{
            scenario.onActivity {
                it.viewModel.setZoom(zoom)
                Assert.assertEquals(zoom, it.viewModel.getZoom())
            }
        }
    }

    @Test
    fun testSetCameraPosition(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        val scenario = ActivityScenario.launch<MapsActivity>(intent)
        val position = LatLng(-67.0, 34.5)
        scenario.use{
            scenario.onActivity {
                it.viewModel.setCameraPosition(position)
                Assert.assertTrue(
                    Location(position.longitude, position.latitude).distanceInKm(
                        Location(it.viewModel.getCameraPosition().longitude,
                        it.viewModel.getCameraPosition().latitude))  < 1.0)

            }
        }
    }


    @Test
    fun testSetZoomPosition(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        val scenario = ActivityScenario.launch<MapsActivity>(intent)
        val position = LatLng(55.5, -42.0)
        val zoom = 8f
        scenario.use{
            scenario.onActivity {
                it.viewModel.setPositionAndZoom(position, zoom)
                Assert.assertTrue(
                    Location(position.longitude, position.latitude).distanceInKm(
                        Location(it.viewModel.getCameraPosition().longitude,
                            it.viewModel.getCameraPosition().latitude))  < 1.0)

                Assert.assertEquals(zoom, it.viewModel.getZoom())
            }
        }
    }



}