package com.github.palFinderTeam.palfinder.map

import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.collections.HashMap

class MapUtils{

    private lateinit var map:GoogleMap
    private var meetUps = HashMap<String, MeetUp>()
    private var markers = HashMap<String, Marker>()
    var mapReady = false
    private var startingCameraPosition: LatLng? = null


    /**
     * set the map to which utils functions will be applied
     * @param map: GoogleMap
     */
    fun setMap(map : GoogleMap){
        this.map = map
        mapReady = true
    }

    /**
     * get the Meetup in this utils memory corresponding to this id
     * @param id: Unique identifier of the meetup
     * @return the meetup corresponding to the id, null if non existent
     */
    fun getMeetup(id: String): MeetUp?{
        return meetUps[id]
    }

    /**
     * get the Marker in this utils memory corresponding to this id
     * @param id: Unique identifier of the meetup
     * @return the marker corresponding to the id, null if non existent
     */
    fun getMarker(id: String):Marker?{
        return markers[id]
    }

    /**
     * add a new meetup to the map meetup list, if the map is ready, refresh it
     * @param meetUp: The meetup to add
     */
    fun addMeetupMarker(meetUp: MeetUp){
        meetUps[meetUp.uuid] = meetUp

        if(mapReady) refresh()
    }

    /**
     * refresh the map to remove Marker that are not in the meetup list and
     * add those of the meetup list that are not in the map
     * if the map is not ready, do nothing
     */
    fun refresh(){
        if(!mapReady) return

        markers.keys.minus(meetUps.keys).forEach{
            markers[it]?.remove()
            markers.remove(it)
        }

        meetUps.keys.minus(markers.keys).forEach {
            val position = LatLng(meetUps[it]!!.location.latitude, meetUps[it]!!.location.longitude)
            markers[it] = map.addMarker(MarkerOptions().position(position).title(it))!!

        }
    }

    /**
     * remove a marker from the map, but keep it in the meetup list
     * @param id: uuid of the Marker to remove
     */
    fun removeMarker(id: String){
        meetUps.remove(id)
        if(mapReady) refresh()
    }

    /**
     * clear the map of all makers and the meetup lists
     */
    fun clearMap(){
        clearMarkers()
        meetUps.clear()
    }

    /**
     * clear the map of all markers
     */
    fun clearMarkers(){
        markers.forEach{
            it.value.remove()
            markers.remove(it.key)
        }
    }

    /**
     * set the camera of the map to a position,
     * if the map is not ready, set the starting location to this position
     * @param position: new position of the camera
     */
    fun setCameraPosition(position : LatLng){
        if(mapReady) {
            map.moveCamera(CameraUpdateFactory.newLatLng(position))
            map.cameraPosition
        }else startingCameraPosition = position
    }

    /**
     * getter of the current starting camera position
     * @return starting camera position, can be null
     */
    fun getStartingCameraPosition(): LatLng?{
        return startingCameraPosition
    }

    /**
     * get the current camera position
     * if map not ready, return the starting camera position
     * @return the camera position, can be null
     */
    fun getCameraPosition():LatLng?{
        return if(mapReady) map.cameraPosition.target
        else startingCameraPosition
    }




}