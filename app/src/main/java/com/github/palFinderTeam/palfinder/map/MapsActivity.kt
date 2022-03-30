package com.github.palFinderTeam.palfinder.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.databinding.ActivityMapsBinding
import com.github.palFinderTeam.palfinder.meetups.MeetupListAdapter
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MapListSuperActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

const val LOCATION_SELECT = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECT"
const val LOCATION_SELECTED = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECTED"

@AndroidEntryPoint
class MapsActivity : MapListSuperActivity(), OnMapReadyCallback,  GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveCanceledListener {

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapView = mapFragment.requireView()
        mapView.contentDescription = "MAP NOT READY"
        mapFragment.getMapAsync(this)

        viewModel = ViewModelProvider(this)[MapsActivityViewModel::class.java]
        viewModel.update((viewModel as MapsActivityViewModel).getCameraPosition())
        viewModel.listOfMeetUpResponse.observe(this) {
            viewModel.refresh()
        }

    }

    private fun loadSelectionButton(){
        if (intent.hasExtra(LOCATION_SELECT)) {
            val pos = intent.getParcelableExtra<LatLng>(LOCATION_SELECT)
            mapSelection.active.value = true
            navBar.isVisible = false
            navBar.isEnabled = false
            button.apply { this.isEnabled = false }
            if (pos != null){
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
        if(id != null){
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
    private fun setSelectionMarker(p0: LatLng){
        mapSelection.targetMarker.value?.remove()
        mapSelection.targetMarker.value = map.addMarker(
            MarkerOptions().position(p0).title("Here").draggable(true)
        )
        button.apply { this.isEnabled = mapSelection.targetMarker.value != null }
    }

    /**
     * Return the selected Location to the previous activity
     */
    fun onConfirm(v: View){
        val resultIntent = Intent()
        resultIntent.putExtra(LOCATION_SELECTED, mapSelection.targetMarker.value!!.position)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        viewModel.setMap(map)
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)


        super.setUserLocation()


        map.setOnMapClickListener { onMapClick(it) }

        mapView.contentDescription = "MAP READY"
        loadSelectionButton()


    }

    override fun onCameraMoveCanceled() {
        viewModel.update((viewModel as MapsActivityViewModel).getCameraPosition())
    }

}