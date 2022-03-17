package com.github.palFinderTeam.palfinder.meetups.activities

import android.icu.util.Calendar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.maps.model.LatLng

import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsRepository

import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.isDeltaBefore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetUpCreationViewModel @Inject constructor(
    private val meetUpRepository: MeetUpRepository,
    private val calendar: Calendar
) : ViewModel() {
    private var uuid: String? = null

    private val _startDate: MutableLiveData<Calendar> = MutableLiveData(calendar)
    private val _endDate: MutableLiveData<Calendar> = MutableLiveData(calendar)
    private val _capacity: MutableLiveData<Int> = MutableLiveData()
    private val _hasMaxCapacity: MutableLiveData<Boolean> = MutableLiveData()
    private val _name: MutableLiveData<String> = MutableLiveData()
    private val _description: MutableLiveData<String> = MutableLiveData()
    private val _tags: MutableLiveData<Set<Category>> = MutableLiveData()

    private val _sendSuccess: MutableLiveData<Boolean> = MutableLiveData()

    val startDate: LiveData<Calendar> = _startDate
    val endDate: LiveData<Calendar> = _endDate
    val capacity: LiveData<Int> = _capacity
    val hasMaxCapacity: LiveData<Boolean> = _hasMaxCapacity
    val name: LiveData<String> = _name
    val description: LiveData<String> = _description
    val sendSuccess: LiveData<Boolean> = _sendSuccess
    val tags: LiveData<Set<Category>> = _tags

    /**
     * Fill every field with default value (in case of meetup creation)
     */
    fun fillWithDefaultValues() {
        _capacity.value = 1
        _hasMaxCapacity.value = false
        _name.value = ""
        _description.value = ""
        _tags.value = setOf()
    }

    fun setStartDate(date: Calendar) {
        _startDate.value = date
        checkDateIntegrity()
    }

    fun setEndDate(date: Calendar) {
        _endDate.value = date
        checkDateIntegrity()
    }

    fun setCapacity(capacity: Int) {
        _capacity.value = capacity
    }

    fun setHasMaxCapacity(hasMaxCapacity: Boolean) {
        _hasMaxCapacity.value = hasMaxCapacity
    }

    fun setName(name: String) {
        _name.value = name
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun getMeetUpId() = uuid

    /**
     * Load asynchronously a meetUp and update liveData on success.
     *
     * @param meetUpId Id of the meetUp to fetch
     */
    fun loadMeetUp(meetUpId: String) {
        viewModelScope.launch {
            val meetUp = meetUpRepository.getMeetUpData(meetUpId)
            if (meetUp != null) {
                uuid = meetUp.uuid
                _name.postValue(meetUp.name)
                _description.postValue(meetUp.description)
                _startDate.postValue(meetUp.startDate)
                _endDate.postValue(meetUp.endDate)
                _hasMaxCapacity.postValue(meetUp.hasMaxCapacity)
                _capacity.postValue(meetUp.capacity)
                _tags.postValue(meetUp.tags)
            } else {
                fillWithDefaultValues()
            }
        }
    }

    /**
     * Send every field as a MeetUp to DB.
     */
    fun sendMeetUp() {
        val meetUp = MeetUp(
            uuid.orEmpty(),
            // TODO Put real user
            ProfileUser("le miche 420", "Michel", "Francis", calendar, ImageInstance("icons/demo_pfp.jpeg")),
            // TODO Put real icon
            "whatever",
            name.value!!,
            description.value!!,
            startDate.value!!,
            endDate.value!!,
            Location(1.0, 2.0),
            tags.value.orEmpty(),
            hasMaxCapacity.value!!,
            capacity.value!!,
            // TODO Put real users
            mutableListOf()
        )
        if (uuid == null) {
            // create new meetup
            viewModelScope.launch {
                uuid = meetUpRepository.createMeetUp(meetUp)
                // Notify sending result
                _sendSuccess.postValue(uuid != null)
            }
        } else {
            // Edit existing one
            viewModelScope.launch {
                meetUpRepository.editMeetUp(uuid!!, meetUp)
                _sendSuccess.postValue(true)
            }
        }
    }

    /**
     * Enforce that End Date is After Start Date
     */
    private fun checkDateIntegrity() {
        if (startDate.value == null || endDate.value == null) {
            return
        }
        // Check if at least defaultTimeDelta between start and end
        if (!startDate.value!!.isDeltaBefore(endDate.value!!, defaultTimeDelta)) {
            val newCalendar = Calendar.getInstance()
            newCalendar.timeInMillis = startDate.value!!.timeInMillis
            newCalendar.add(Calendar.MILLISECOND, defaultTimeDelta)
            _endDate.value = newCalendar
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

    val location: MutableLiveData<Location> by lazy {
        MutableLiveData<Location>()
    }

    fun getLatLng(): LatLng?{
        return if (location.value != null){
            LatLng(location.value!!.latitude, location.value!!.longitude)
        } else {
            null
        }
    }
    fun setLatLng(p0: LatLng){
        location.value = Location(p0.longitude, p0.latitude)
    }
}