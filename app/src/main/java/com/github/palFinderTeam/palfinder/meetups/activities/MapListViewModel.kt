package com.github.palFinderTeam.palfinder.meetups.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.icu.util.Calendar
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
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


@SuppressLint("MissingPermission")
@ExperimentalCoroutinesApi
@HiltViewModel
/**
 * A ViewModel for the MapsFragment and the MeetUpListFragment,
 * that fetches the list of meetups displayed around a location.
 * It also works with the geolocation to get the current phone location,
 * and with tags to filter the dataset
 * @param meetUpRepository the database for the meetups, from which we fetch
 * @param profileService the database for the profiles, mainly to get the currentLoggedUser
 * @param timeService database to retrieve the current time, so we don't display outdated meetups
 */
class MapListViewModel @Inject constructor(
    val meetUpRepository: MeetUpRepository,
    val profileService: ProfileService,
    val timeService: TimeService,
    application: Application
) : AndroidViewModel(application) {
    companion object {
        const val INITIAL_RADIUS: Double = 400.0
        val START_LOCATION = Location(6.5657, 46.5197)


        // Remove meetups created by blocked users
        private fun Response<List<MeetUp>>.filterBlocked(blockedUser: List<String>): Response<List<MeetUp>> {
            return if (this is Response.Success) {
                val filtered = this.data.filter { meetUp ->
                    !blockedUser.contains(meetUp.creatorId)
                }
                Response.Success(filtered)
            } else {
                this
            }
        }
    }

    //store the fetched meetups in real time, separated in 2 to be immutable
    private val _listOfMeetUpResponse: MutableLiveData<Response<List<MeetUp>>> = MutableLiveData()
    val listOfMeetUpResponse: LiveData<Response<List<MeetUp>>> = _listOfMeetUpResponse

    //store the current tags filtering the data, separated in 2 as well
    private val _tags: MutableLiveData<Set<Category>> = MutableLiveData(setOf())
    val tags: LiveData<Set<Category>> = _tags

    //stores the current user location and the client
    val locationClient =
        LocationServices.getFusedLocationProviderClient(getApplication<Application>().applicationContext)
    private val _userLocation: MutableLiveData<Location> = MutableLiveData()
    val userLocation: LiveData<Location> = _userLocation

    //allows delay for the permission answer
    private val _requestPermissions = MutableLiveData(false)
    val requestPermissions: LiveData<Boolean> = _requestPermissions

    // Search params.
    private val _searchRadius = MutableLiveData(INITIAL_RADIUS)
    private val _searchLocation = MutableLiveData(START_LOCATION)
    val searchLocation: LiveData<Location> = _searchLocation
    val searchRadius: LiveData<Double> = _searchRadius
    private val _showParam = MutableLiveData(ShowParam.ALL)
    val showParam: LiveData<ShowParam> = _showParam
    private var showOnlyAvailableInTime = true
    private var filterBlockedUserMeetups = true
    private var isFirstInit = true

    lateinit var startTime: MutableLiveData<Calendar>
    lateinit var endTime: MutableLiveData<Calendar>

    //updates the userLocation

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


    /**
     * Set search parameters that you want to apply.
     *
     * @param location Location around which to search.
     * @param radiusInKm Radius of the search.
     * @param showParam specify the meetUps to be displayed
     * @param showOnlyAvailable If true, only show meetup available at the current time.
     * @param filterBlockedMeetups If true, only show meetup where no blocked user participates.
     */
    fun setSearchParameters(
        location: Location? = null,
        radiusInKm: Double? = null,
        showParam: ShowParam? = null,
        showOnlyAvailable: Boolean? = null,
        filterBlockedMeetups: Boolean? = null,
    ) {
        location?.let {
            _searchLocation.value = it
        }
        radiusInKm?.let {
            _searchRadius.value = it
        }
        showParam?.let {
            _showParam.value = it
        }
        showOnlyAvailable?.let {
            showOnlyAvailableInTime = it
        }
        filterBlockedMeetups?.let {
            filterBlockedUserMeetups = it
        }
    }

    /**
     * Same as [setSearchParameters] but will also fetch meetups if needed.
     *
     * @param location Location around which to search.
     * @param radiusInKm Radius of the search.
     * @param showParam specify the meetUps to be displayed
     * @param showOnlyAvailable If true, only show meetup available at the current time.
     * @param filterBlockedMeetups If true, only show meetup where no blocked user participates.
     * @param forceFetch If true, always fetch after assigning the params.
     */
    fun setSearchParamAndFetch(
        location: Location? = null,
        radiusInKm: Double? = null,
        showParam: ShowParam? = null,
        showOnlyAvailable: Boolean? = null,
        forceFetch: Boolean = false,
        filterBlockedMeetups: Boolean? = null,
    ) {
        // In case the search params are the same we don't fetch again.
        if (
            (!forceFetch)
            && (location == null || searchLocation.value == location)
            && (radiusInKm == null || searchRadius.value == radiusInKm)
            && (this.showParam.value == showParam)
            && (showOnlyAvailable == null || showOnlyAvailableInTime == showOnlyAvailable)
            && (filterBlockedMeetups == null || filterBlockedMeetups == filterBlockedUserMeetups)
        ) {
            return
        }

        setSearchParameters(
            location,
            radiusInKm,
            showParam,
            showOnlyAvailable,
            filterBlockedMeetups
        )
        fetchMeetUps()
    }

    /**
     * Fetch asynchronously the meetups with the different parameters value set in the viewModel.
     * This is not triggered automatically when a parameter (like radius) is changed, views have to
     * call it by themself.
     */
    fun fetchMeetUps() {
        when (showParam.value) {
            ShowParam.ONLY_JOINED -> fetchUserMeetUps()
            else -> getMeetupAroundLocation(
                searchLocation.value!!,
                searchRadius.value ?: INITIAL_RADIUS,
                showParam.value!!
            )
        }
    }

    /**
     * Retrieves meetups around a certain location, and post them to the
     * meetUpResponse liveData.
     *
     * @param position Location around which it will fetch.
     * @param radiusInKm Radius of the search, in Km.
     */
    private fun getMeetupAroundLocation(
        position: Location,
        radiusInKm: Double,
        showParam: ShowParam
    ) {
        val date = if (showOnlyAvailableInTime) timeService.now() else null
        viewModelScope.launch {
            val userId = profileService.getLoggedInUserID()
            val blockedUser = getBlockedUser(userId)
            meetUpRepository.getMeetUpsAroundLocation(
                position,
                radiusInKm,
                currentDate = date,
                showParam,
                if (userId == null) userId else profileService.fetch(userId)
            ).collect {
                _listOfMeetUpResponse.postValue(it.filterBlocked(blockedUser))
            }
        }
    }

    private suspend fun getBlockedUser(uuid: String?): List<String> {
        return if (filterBlockedUserMeetups && uuid != null) {
            profileService.fetch(uuid)?.blockedUsers.orEmpty()
        } else {
            emptyList()
        }
    }

    /**
     * Fetch all meetUp the user is taking part to.
     */
    private fun fetchUserMeetUps() {
        val userId = profileService.getLoggedInUserID()
        val date = if (showOnlyAvailableInTime) timeService.now() else null
        if (userId != null) {
            viewModelScope.launch {
                val blockedUser = getBlockedUser(userId)
                meetUpRepository.getUserMeetups(userId, date)
                    .collect {
                        _listOfMeetUpResponse.postValue(it.filterBlocked(blockedUser))
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

    fun firstInit(): Boolean{
        return if(isFirstInit){
            isFirstInit = false
            true
        }else false

    }


}
