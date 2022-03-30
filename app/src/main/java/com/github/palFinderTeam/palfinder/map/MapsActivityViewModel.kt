package com.github.palFinderTeam.palfinder.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpListViewModel
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.math.pow

@HiltViewModel
class MapsActivityViewModel @Inject constructor(
    meetUpRepository: MeetUpRepository
) : MeetUpListViewModel(
    meetUpRepository
) {



    private var markers = HashMap<String, Marker>()






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
    override fun refresh(){
        super.refresh()

        clearMarkers()

        meetupList?.forEach{ meetUp ->
            val position = LatLng(meetUp.location.latitude, meetUp.location.longitude)
            val marker = getMap().addMarker(MarkerOptions().position(position).title(meetUp.uuid))
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
    fun setCameraPosition(position: LatLng){
        if(mapReady) {
            getMap().moveCamera(CameraUpdateFactory.newLatLng(position))
        }else startingCameraPosition = position
    }

    /**
     * get the current camera position
     * if map not ready, return the starting camera position
     * @return the camera position
     */
    fun getCameraPosition():LatLng{
        return if(mapReady) getMap().cameraPosition.target
        else startingCameraPosition
    }




    /**
     * set the zoom
     * if map not ready, set the starting zoom
     * @param zoom: new zoom of the camera
     */
    fun setZoom(zoom: Float){
        if(mapReady) {
            getMap().moveCamera(CameraUpdateFactory.zoomTo(zoom))
        }else{
            startingZoom = zoom
        }
    }



}