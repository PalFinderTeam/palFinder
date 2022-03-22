package com.github.palFinderTeam.palfinder.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.math.exp
import kotlin.math.pow

class MapsUtils constructor(
    private val meetUpRepository: MeetUpRepository = FirebaseMeetUpService
){

    private lateinit var map:GoogleMap
    private var meetUps = HashMap<String, MeetUp>()
    private var markers = HashMap<String, Marker>()
    var mapReady = false
    private var startingCameraPosition: LatLng = LatLng(0.0, 0.0)
    private var startingZoom: Float = 15f




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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchMeetups(){
        Log.d(null, "1")
        if(getZoom() < 7f){
            //TODO get only the joined meetup
        }else{
            val earthRadius = 6371000
            // at zoom 0, the map is of size 256x256 pixels and for every zoom, the number of pixel is multiplied by 2
            val radiusAtZoom0 = earthRadius/256
            val radius = radiusAtZoom0/2.0.pow(getZoom().toDouble())
            meetUpRepository.getAllMeetUps().asLiveData().value?.forEach{
                meetUp -> meetUps[meetUp.uuid] = meetUp
            }

        refresh()
        }
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

        refresh()
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
        refresh()
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
        val iterator = markers.iterator()
        while(iterator.hasNext()){
            val marker = iterator.next()
            marker.value.remove()
            iterator.remove()
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
        }else startingCameraPosition = position
    }

    /**
     * get the current camera position
     * if map not ready, return the starting camera position
     * @return the camera position, can be null
     */
    fun getCameraPosition():LatLng{
        return if(mapReady) map.cameraPosition.target
        else startingCameraPosition
    }


    fun getZoom(): Float{
        return if(mapReady) map.cameraPosition.zoom
        else startingZoom
    }

    fun setZoom(zoom: Float){
        if(mapReady) {
            map.moveCamera(CameraUpdateFactory.zoomTo(zoom))
        }else{
            startingZoom = zoom
        }
    }

    fun setPositionZoom(position: LatLng, zoom: Float){
        if(mapReady){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom))
        }else{
            startingCameraPosition = position
            startingZoom = zoom
        }
    }


}