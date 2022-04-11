package com.github.palFinderTeam.palfinder.meetups.activities

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsRepository
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class MapListViewModel @Inject constructor(
    val meetUpRepository: MeetUpRepository,
    val profileService: ProfileService,
    application: Application
) : AndroidViewModel(application) {
    private val _listOfMeetUpResponse: MutableLiveData<Response<List<MeetUp>>> = MutableLiveData()
    val listOfMeetUpResponse: LiveData<Response<List<MeetUp>>> = _listOfMeetUpResponse

    private val _tags: MutableLiveData<Set<Category>> = MutableLiveData(setOf())
    val tags: LiveData<Set<Category>> = _tags

    private val locationClient =
        LocationServices.getFusedLocationProviderClient(getApplication<Application>().applicationContext)

    private val _userLocation: MutableLiveData<Location> = MutableLiveData()
    val userLocation: LiveData<Location> = _userLocation

    private val _requestPermissions = MutableLiveData(false)
    val requestPermissions: LiveData<Boolean> = _requestPermissions

    val zoom = MutableLiveData(STARTING_ZOOM)

    val searchRadius = MutableLiveData(INITIAL_RADIUS)
    val searchLocation = MutableLiveData(Location(45.0, 45.0))

    var showOnlyJoined = false

    private var startingCameraPosition = LatLng(45.0, 45.0)

    init {
        if (ActivityCompat.checkSelfPermission(
                getApplication<Application>().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getApplication<Application>().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _requestPermissions.value = true
        } else {
            locationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _userLocation.postValue(Location(location.longitude, location.latitude))
                }
            }
        }
    }


    private fun setUserLocation(value: Location) {
        _userLocation.value = value
        fetchMeetUps()
    }

    /**
     * Fetch asynchronously the meetups with the different parameters value set in the viewModel.
     * This is not triggered automatically when a parameter (like radius) is changed, views have to
     * call it by themself.
     */
    fun fetchMeetUps() {
        if (showOnlyJoined) {
            fetchUserMeetUps()
        } else {
            // TODO Fix radius
            //getMeetupAroundLocation(searchLocation.value!!, searchRadius.value ?: INITIAL_RADIUS)
            getMeetupAroundLocation(searchLocation.value!!, 500.0)
        }
    }

    /**
     * Retrieves meetups around a certain location, and post them to the
     * meetUpResponse liveData.
     *
     * @param position Location around which it will fetch.
     * @param radiusInKm Radius of the search, in Km.
     */
    fun getMeetupAroundLocation(
        position: Location,
        radiusInKm: Double
    ) {
        viewModelScope.launch {
            meetUpRepository.getMeetUpsAroundLocation(
                position,
                radiusInKm
            ).collect {
                _listOfMeetUpResponse.postValue(it)
            }
        }
    }

    /**
     * Fetch all meetUp the user is taking part to.
     */
    private fun fetchUserMeetUps() {
        val userId = profileService.getLoggedInUserID()
        if (userId != null) {
            viewModelScope.launch {
                meetUpRepository.getAllMeetUps().map {
                    it.filter { it.participantsId.contains(userId) }
                }.collect {
                    _listOfMeetUpResponse.postValue(Response.Success(it))
                }
            }
        } else {
            _listOfMeetUpResponse.value = Response.Failure("No login users.")
        }
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
    fun getUser(): String? {
        return profileService.getLoggedInUserID()
    }

    companion object {
        private const val STARTING_ZOOM = 15f
        private const val INITIAL_RADIUS: Double = 400.0
    }
}