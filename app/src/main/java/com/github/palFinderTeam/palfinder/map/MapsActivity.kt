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
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.databinding.ActivityMapsBinding
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


const val LOCATION_SELECT = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECT"
const val LOCATION_SELECTED = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECTED"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var map: GoogleMap
    private lateinit var button: FloatingActionButton

    private val mapSelection: MapsSelectionModel by viewModels()

    companion object {
        private const val USER_LOCATION_PERMISSION_REQUEST_CODE = 1
        private var meetUps = LinkedList<MeetUp>()
        private var baseLocation: LatLng? = null

        /**
         * add a marker corresponding to a meetup
         */
        fun addMeetUpMarker(meetUp: MeetUp) {
            meetUps.add(meetUp)
            }

        fun setBaseLocation(location:LatLng){
            baseLocation = location
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        button = findViewById(R.id.bt_locationSelection)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun loadSelectionButton(){
        if (intent.hasExtra(LOCATION_SELECT)) {
            val pos = intent.getParcelableExtra<LatLng>(LOCATION_SELECT)
            button.apply { this.isEnabled = false }
            if (pos != null){
                // TODO add marker when ready
                //addSelectionMarker(pos)
            }
        } else {
            button.apply { this.hide() }
        }
    }

    private fun addMarkers(){
        meetUps.forEach{
            val position = LatLng(it.location.latitude, it.location.longitude)
            val marker = map?.addMarker(MarkerOptions().position(position).title(it.uuid))
            marker?.tag = it
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    // TODO add all meetup to map
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        setUserLocation()

        addMarkers()

        map.setOnMapClickListener(this::onMapClick)

        loadSelectionButton()
    }



    private fun setUserLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                USER_LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){
            location -> if(location != null){
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                if(baseLocation != null){
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(baseLocation!!, 15f))
                }else map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }



    /**
     * When a meetUp marker is clicked, open the marker description
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        if(marker.tag is MeetUp) {
            val meetUp: MeetUp = marker.tag as MeetUp
            val intent = Intent(this, MeetUpView::class.java).apply {
                putExtra(MEETUP_SHOWN, meetUp)
            }
            startActivity(intent)
            return true
        }
        return false
    }

    private fun onMapClick(p0: LatLng) {
        // Add a marker if the map is used to select a location
        if (intent.hasExtra(LOCATION_SELECT)) {
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

}