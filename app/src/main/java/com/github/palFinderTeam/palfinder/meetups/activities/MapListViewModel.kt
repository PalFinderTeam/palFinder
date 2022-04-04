package com.github.palFinderTeam.palfinder.meetups.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsRepository
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.pow


@HiltViewModel
class MapListViewModel @Inject constructor(
    val meetUpRepository: MeetUpRepository,
    val profileService: ProfileService
) : ViewModel() {
    lateinit var listOfMeetUpResponse: LiveData<Response<List<MeetUp>>>
    lateinit var meetupList: List<MeetUp>
    private val _tags: MutableLiveData<Set<Category>> = MutableLiveData(setOf())
    val tags: LiveData<Set<Category>> = _tags
    var startingCameraPosition: LatLng = LatLng(46.31, 6.38)
    var startingZoom: Float = 15f
    var showOnlyJoined: Boolean = false


    private lateinit var map: GoogleMap
    var mapReady = false
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
     * set the map to which utils functions will be applied
     * @param map: GoogleMap
     */
    fun setMap(map : GoogleMap){
        this.map = map
        mapReady = true
    }
    fun getMap(): GoogleMap {
        return map
    }

    fun update(location: LatLng?){
        if(false){//getZoom() < 7f){
            //TODO get only the joined meetup

        } else{
            val earthRadius = 6371000.0
            // at zoom 0, the map is of size 256x256 pixels and for every zoom, the number of pixel is multiplied by 2
            val radiusAtZoom0 = earthRadius/256
            val radius = radiusAtZoom0/2.0.pow(getZoom().toDouble())

            /*if(meetUpRepository.getAllMeetUps().asLiveData().value != null) {
                meetUps = meetUpRepository.getAllMeetUps().asLiveData().value!!
            }else meetUps = emptyList()*/
            /*listOfMeetUpResponse = meetUpRepository.getMeetUpsAroundLocation(Location(location!!.longitude, location!!.latitude),
                earthRadius/1000.0).asLiveData()*/
            listOfMeetUpResponse = meetUpRepository.getAllMeetUpsResponse().asLiveData()
            refresh()

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

    /**
     * refresh the map to remove Marker that are not in the meetup list and
     * add those of the meetup list that are not in the map
     * if the map is not ready, do nothing
     */
    fun refresh() {
        if (!mapReady) return
        val response = listOfMeetUpResponse.value
        meetupList = if(response is Response.Success){
            response.data
        }else emptyList()
        clearMarkers()

        meetupList?.forEach{ meetUp ->
            val position = LatLng(meetUp.location.latitude, meetUp.location.longitude)
            val marker = getMap().addMarker(MarkerOptions().position(position).title(meetUp.uuid))
                ?.let { markers[meetUp.uuid] = it }
        }
    }

    /**
     * set the position and the zoom
     * if the map is not ready, set both starting values
     * @param position : the new camera position of the camera
     * @param zoom : the new zoom of the camera
     */
    fun setPositionAndZoom(position: LatLng, zoom: Float){
        if(mapReady){
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom))
        }else{
            startingCameraPosition = position
            startingZoom = zoom
        }
    }

    /**
     * get the current zoom
     * if map not ready, return the starting zoom
     * @return the zoom
     */
    fun getZoom(): Float{
        return if(mapReady) getMap().cameraPosition.zoom
        else startingZoom
    }
    /**
     *  Provides the tagContainer with the necessary tags and allows it to edit them.
     */
    val tagRepository = object : TagsRepository<Category> {
        override val tags: Set<Category>
            get() = _tags.value ?: setOf()

        override val isEditable = true
        override val allTags = Category.values().toSet()

        override fun removeTag(tag: Category): Boolean {
            val tags = _tags.value
            return if (tags == null || !tags.contains(tag)) {
                false
            } else {
                _tags.value = tags.minus(tag)
                true
            }
        }

        override fun addTag(tag: Category): Boolean {
            val tags = _tags.value
            return if (tags == null || tags.contains(tag)) {
                false
            } else {
                _tags.value = tags.plus(tag)
                true
            }
        }
    }

    /**
     * Return the currently logged in user id
     */
    fun getUser():String?{
        return profileService.getLoggedInUserID()
    }
}