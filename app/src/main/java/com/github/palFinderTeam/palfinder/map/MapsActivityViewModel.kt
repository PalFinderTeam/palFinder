package com.github.palFinderTeam.palfinder.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.math.pow

class MapsActivityViewModel constructor(
    private val meetUpRepository: MeetUpRepository = FirebaseMeetUpService(FirebaseFirestore.getInstance())
): ViewModel() {

    lateinit var meetUps: LiveData<List<MeetUp>>
    private lateinit var map:GoogleMap
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

    fun updateFetcherLocation(){
        if(false){//getZoom() < 7f){
            //TODO get only the joined meetup

        }else{
            val earthRadius = 6371000
            // at zoom 0, the map is of size 256x256 pixels and for every zoom, the number of pixel is multiplied by 2
            val radiusAtZoom0 = earthRadius/256
            val radius = radiusAtZoom0/2.0.pow(getZoom().toDouble())

            meetUps = meetUpRepository.getAllMeetUps().asLiveData()
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
     * refresh the map to remove Marker that are not in the meetup list and
     * add those of the meetup list that are not in the map
     * if the map is not ready, do nothing
     */
    fun refresh(){
        if(!mapReady) return

        clearMarkers()

        meetUps.value?.forEach{ meetUp ->
            val position = LatLng(meetUp.location.latitude, meetUp.location.longitude)
            val marker = map.addMarker(MarkerOptions().position(position).title(meetUp.uuid))
                ?.let { markers[meetUp.uuid] = it }
        }
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