package com.github.palFinderTeam.palfinder.map

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class MapsViewModelTestComplement {


    @Inject
    lateinit var meetUpRepository: MeetUpRepository


    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var fineLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    var coarseLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)


    private lateinit var viewModel: MapListViewModel

    @Before
    fun init_() {
        hiltRule.inject()
        viewModel = MapListViewModel(meetUpRepository)
    }

    @Test
    fun testSetZoom(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        val scenario = ActivityScenario.launch<MapsActivity>(intent)
        var zoom: Float = 15f
        scenario.use{
            viewModel.setZoom(zoom)
            Assert.assertEquals(zoom, viewModel.getZoom())
        }
    }

    @Test
    fun testSetCameraPosition(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        val scenario = ActivityScenario.launch<MapsActivity>(intent)
        val position = LatLng(-67.0, 34.5)
        scenario.use{
            viewModel.setCameraPosition(position)
            Assert.assertEquals(position, viewModel.getCameraPosition())
        }
    }


    @Test
    fun testSetZoomPosition(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        val scenario = ActivityScenario.launch<MapsActivity>(intent)
        val position = LatLng(55.5, -42.0)
        val zoom = 8f
        scenario.use{
            viewModel.setPositionAndZoom(position, zoom)
            Assert.assertEquals(position, viewModel.getCameraPosition())
            Assert.assertEquals(zoom, viewModel.getZoom())
        }
    }



}