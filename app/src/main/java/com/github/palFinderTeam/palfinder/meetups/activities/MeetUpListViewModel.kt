package com.github.palFinderTeam.palfinder.meetups.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsRepository
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.pow


@HiltViewModel
open class MeetUpListViewModel @Inject constructor(
    val meetUpRepository: MeetUpRepository
) : ViewModel() {
    lateinit var listOfMeetUpResponse: LiveData<Response<List<MeetUp>>>
    lateinit var meetupList: List<MeetUp>
    private val _tags: MutableLiveData<Set<Category>> = MutableLiveData(setOf())
    val tags: LiveData<Set<Category>> = _tags
    var startingCameraPosition: LatLng = LatLng(46.31, 6.38)
    var startingZoom: Float = 15f


    private lateinit var map: GoogleMap
    var mapReady = false
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

    open fun refresh() {
        val response = listOfMeetUpResponse.value
        meetupList = if(response is Response.Success){
            response.data
        }else emptyList()
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
}