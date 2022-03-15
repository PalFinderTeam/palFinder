package com.github.palFinderTeam.palfinder.map

import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.collections.HashMap

class MapsService: OnMapReadyCallback{

    private lateinit var map:GoogleMap
    private var meetUps = HashMap<String, MeetUp>()
    private var markers = HashMap<String, Marker>()
    private var mapReady = false
    private lateinit var activity:MapsActivity
    private var cameraPosition: LatLng? = null

    fun setActivity(activity: MapsActivity){
        this.activity = activity
    }


    private fun refresh(){
        markers.keys.minus(meetUps.keys).forEach{
            markers[it]?.remove()
            markers.remove(it)
        }
        meetUps.keys.minus(markers.keys).forEach {
                val position = LatLng(meetUps[it]!!.location.latitude, meetUps[it]!!.location.longitude)
            markers[it] = map.addMarker(MarkerOptions().position(position).title(it))!!

        }
    }


    fun addMeetupMarker(meetUp: MeetUp){
        meetUps.put(meetUp.uuid, meetUp)
        if(mapReady) refresh()
    }

    fun getMeetup(uuid: String): MeetUp?{
        return meetUps[uuid]
    }


    fun setCameraPosition(position : LatLng){
        if(mapReady) {
            map.moveCamera(CameraUpdateFactory.newLatLng(position))
            map.cameraPosition
        }else cameraPosition = position
    }

    fun getCameraPosition():LatLng{
        return map.cameraPosition.target
    }

    fun getMarker(id: String):Marker?{
        return markers[id]
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapReady = true


        refresh()
        activity.setUserLocation(cameraPosition)


    }


}