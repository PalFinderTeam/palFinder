package com.github.palFinderTeam.palfinder.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.github.palFinderTeam.palfinder.R

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.github.palFinderTeam.palfinder.databinding.ActivityMapsBinding
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,  GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var map: GoogleMap

    companion object {
        private const val USER_LOCATION_PERMISSION_REQUEST_CODE = 1
        val utils = MapsUtils()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
                if(utils.getStartingCameraPosition() == null)map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, utils.BASE_ZOOM))
                else map.moveCamera(CameraUpdateFactory.newLatLngZoom(utils.getStartingCameraPosition()!!, utils.BASE_ZOOM))
            }
        }
    }



    /**
     * When a meetUp marker is clicked, open the marker description
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        val meetUp = utils.getMeetup(marker.title!!)
        if(meetUp != null){
            val intent = Intent(this, MeetUpView::class.java).apply {
                putExtra(MEETUP_SHOWN, meetUp)
            }
            startActivity(intent)
            return true
        }
        return false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        utils.setMap(map)
        utils.refresh()
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        setUserLocation()
    }


}