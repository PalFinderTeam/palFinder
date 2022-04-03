package com.github.palFinderTeam.palfinder.map

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.databinding.ActivityMapsBinding
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MapListSuperActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


const val LOCATION_SELECT = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECT"
const val LOCATION_SELECTED = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECTED"

@AndroidEntryPoint
class MapsActivity : MapListSuperActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnCameraMoveListener {

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


        val apiKey = getString(R.string.api_key)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        Places.createClient(this)

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        autocompleteFragment!!.setTypeFilter(TypeFilter.CITIES)
        autocompleteFragment.setPlaceFields(
            Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME
            )
        )
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Toast.makeText(applicationContext, place.name, Toast.LENGTH_SHORT).show()
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Toast.makeText(applicationContext, status.toString(), Toast.LENGTH_SHORT).show()
            }
        })


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

        map.setOnCameraMoveListener(this)
        super.setUserLocation()


        map.setOnMapClickListener { onMapClick(it) }

        mapView.contentDescription = "MAP READY"
        loadSelectionButton()


    }

    override fun onCameraMove() {
        viewModel.update()
    }


}