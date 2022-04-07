package com.github.palFinderTeam.palfinder.map

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.databinding.ActivityMapsBinding
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MapListSuperActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException


const val LOCATION_SELECT = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECT"
const val LOCATION_SELECTED = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECTED"

@AndroidEntryPoint
class MapsActivity : MapListSuperActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnCameraMoveCanceledListener, SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var button: FloatingActionButton
    private lateinit var navBar: View
    private lateinit var mapView: View

    private val mapSelection: MapsSelectionModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        button = findViewById(R.id.bt_locationSelection)
        navBar = findViewById(R.id.fc_navbar)
        viewModel.update()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapView = mapFragment.requireView()
        mapView.contentDescription = "MAP NOT READY"
        mapFragment.getMapAsync(this)

        viewModel.listOfMeetUpResponse.observe(this) {
            if (it is Response.Success) {
                viewModel.refresh()
            }
        }


        val searchLocation = findViewById<SearchView>(R.id.search_on_map)
        searchLocation.imeOptions = EditorInfo.IME_ACTION_DONE
        searchLocation.setOnQueryTextListener(this)


    }

    private fun loadSelectionButton() {
        if (intent.hasExtra(LOCATION_SELECT)) {
            val pos = intent.getParcelableExtra<LatLng>(LOCATION_SELECT)
            mapSelection.active.value = true
            navBar.isVisible = false
            navBar.isEnabled = false
            button.apply { this.isEnabled = false }
            if (pos != null) {
                setSelectionMarker(pos)
            }
        } else {
            button.apply { this.hide() }
            mapSelection.active.value = false
        }
    }


    /**
     * When a meetUp marker is clicked, open the marker description
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        val id = marker.title
        if (id != null) {
            val intent = Intent(this, MeetUpView::class.java).apply {
                putExtra(MEETUP_SHOWN, id)
            }
            startActivity(intent)
            return true
        }
        return false
    }

    private fun onMapClick(p0: LatLng) {
        // Add a marker if the map is used to select a location
        if (mapSelection.active.value!!) {
            setSelectionMarker(p0)
        }
    }

    /**
     * Add or Update the Position Selection Marker
     */
    private fun setSelectionMarker(p0: LatLng) {
        mapSelection.targetMarker.value?.remove()
        mapSelection.targetMarker.value = map.addMarker(
            MarkerOptions().position(p0).title("Here").draggable(true)
        )
        button.apply { this.isEnabled = mapSelection.targetMarker.value != null }
    }

    /**
     * Return the selected Location to the previous activity
     */
    fun onConfirm(v: View) {
        val resultIntent = Intent()
        resultIntent.putExtra(LOCATION_SELECTED, mapSelection.targetMarker.value!!.position)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        viewModel.setGmap(map)
        viewModel.mapReady = true
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        map.setOnCameraMoveCanceledListener(this)
        super.setUserLocation()


        map.setOnMapClickListener { onMapClick(it) }

        mapView.contentDescription = "MAP READY"
        loadSelectionButton()


    }

    override fun onCameraMoveCanceled() {
        viewModel.update()
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        var addressList: List<Address>? = null

        if (p0 == null || p0 == "") {
            Toast.makeText(applicationContext,getString(R.string.search_no_location),Toast.LENGTH_SHORT).show()
        }
        else{
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(p0, 1)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (addressList == null || addressList.isEmpty()) {
                Toast.makeText(applicationContext,getString(R.string.search_location_not_found),Toast.LENGTH_SHORT).show()
            } else {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                viewModel.setCameraPosition(latLng)
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                viewModel.update()
            }
        }
        return false
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        return false
    }

}