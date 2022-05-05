package com.github.palFinderTeam.palfinder.meetups.activities

import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsRepository
import com.github.palFinderTeam.palfinder.utils.CriterionGender
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Location.Companion.toLocation
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.image.ImageUploader
import com.github.palFinderTeam.palfinder.utils.isBefore
import com.github.palFinderTeam.palfinder.utils.isDeltaBefore
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

const val defaultTimeDelta = 1000 * 60 * 60

@HiltViewModel
class MeetUpCreationViewModel @Inject constructor(
    private val meetUpRepository: MeetUpRepository,
    private val imageUploader: ImageUploader,
    private val profileService: ProfileService
) : ViewModel() {
    private var uuid: String? = null

    private val _canEditStartDate = MutableLiveData(true)
    private val _canEditEndDate = MutableLiveData(true)

    private val _startDate: MutableLiveData<Calendar> = MutableLiveData(Calendar.getInstance())
    private val _endDate: MutableLiveData<Calendar> = MutableLiveData(Calendar.getInstance())
    private val _capacity: MutableLiveData<Int> = MutableLiveData()
    private val _hasMaxCapacity: MutableLiveData<Boolean> = MutableLiveData()
    private val _name: MutableLiveData<String> = MutableLiveData()
    private val _description: MutableLiveData<String> = MutableLiveData()
    private val _icon: MutableLiveData<String?> = MutableLiveData()
    private val _marker: MutableLiveData<Int?> = MutableLiveData()
    private val _tags: MutableLiveData<Set<Category>> = MutableLiveData()
    private val _participantsId: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    private val _location: MutableLiveData<Location> = MutableLiveData()
    private val _iconUri: MutableLiveData<Uri> = MutableLiveData()
    private val _iconUrl: MutableLiveData<String> = MutableLiveData()
    private val _criterionAge: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    private val _criterionGender: MutableLiveData<CriterionGender> = MutableLiveData()

    private val _sendSuccess: MutableLiveData<Boolean> = MutableLiveData()

    val startDate: LiveData<Calendar> = _startDate
    val endDate: LiveData<Calendar> = _endDate
    val capacity: LiveData<Int> = _capacity
    val hasMaxCapacity: LiveData<Boolean> = _hasMaxCapacity
    val name: LiveData<String> = _name
    val description: LiveData<String> = _description
    val icon: LiveData<String?> = _icon
    val marker: LiveData<Int?> = _marker
    val sendSuccess: LiveData<Boolean> = _sendSuccess
    val tags: LiveData<Set<Category>> = _tags
    val participantsId: LiveData<List<String>> = _participantsId
    val iconUri: LiveData<Uri> = _iconUri
    val iconUrl: LiveData<String> = _iconUrl
    val criterionAge: LiveData<Pair<Int, Int>> = _criterionAge
    val criterionGender = _criterionGender

    val maxStartDate: Calendar
        get() {
            val maxDate = Calendar.getInstance()
            maxDate.add(Calendar.DAY_OF_MONTH, 7)
            return maxDate
        }

    val maxEndDate: Calendar
        get() {
            val maxDate = Calendar.getInstance()
            _startDate.value?.let {
                maxDate.time = it.time
            }
            maxDate.add(Calendar.DAY_OF_MONTH, 2)
            return maxDate
        }

    val canEditStartDate: LiveData<Boolean> = _canEditStartDate
    val canEditEndDate: LiveData<Boolean> = _canEditEndDate

    val location: LiveData<Location> = _location


    /**
     * Fill every field with default value (in case of meetup creation)
     */
    fun fillWithDefaultValues() {
        _capacity.value = 1
        _hasMaxCapacity.value = false
        _name.value = ""
        _description.value = ""
        _tags.value = emptySet()
        _participantsId.value = listOf(profileService.getLoggedInUserID()!!)
        _criterionAge.value = Pair(13, 66)
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

    fun setIcon(iconUri: Uri) {
        _iconUri.value = iconUri
    }

    fun setCriterionAge(interval: Pair<Int, Int>) {
        _criterionAge.value = interval
    }

    fun setCriterionGender(gender: CriterionGender) {
        _criterionGender.value = gender
    }

    fun setMarker(markerId: Int){
        _marker.value = markerId
    }

    /**
     * Load asynchronously a meetUp and update liveData on success.
     *
     * @param meetUpId Id of the meetUp to fetch
     */
    fun loadMeetUp(meetUpId: String) {
        if (meetUpId == uuid) {
            return
        }

        viewModelScope.launch {
            val meetUp = meetUpRepository.fetch(meetUpId)
            if (meetUp != null) {
                uuid = meetUp.uuid
                _name.postValue(meetUp.name)
                _icon.postValue(meetUp.iconImage?.imgURL)
                _marker.postValue(meetUp.markerId)
                _description.postValue(meetUp.description)
                _startDate.postValue(meetUp.startDate)
                _endDate.postValue(meetUp.endDate)
                _hasMaxCapacity.postValue(meetUp.hasMaxCapacity)
                _capacity.postValue(meetUp.capacity)
                _tags.postValue(meetUp.tags)
                _participantsId.postValue(meetUp.participantsId)
                _location.postValue(meetUp.location)
                meetUp.iconImage?.imgURL.let {
                    _iconUrl.postValue(it)
                }

                _canEditStartDate.postValue(!meetUp.isStarted(Calendar.getInstance()))
                _canEditEndDate.postValue(!meetUp.isFinished(Calendar.getInstance()))
                meetUp.criterionAge?.let {
                    _criterionAge.postValue(it as Pair<Int, Int>?)
                }
                meetUp.criterionGender?.let {
                    _criterionGender.postValue(it)
                }
            } else {
                _canEditStartDate.postValue(true)
                _canEditEndDate.postValue(true)
                fillWithDefaultValues()
            }
        }
    }

    /**
     * Send every field as a MeetUp to DB.
     */
    fun sendMeetUp() {
        viewModelScope.launch {
            val iconPath = iconUri.value?.let { uri ->
                val newPath = imageUploader.uploadImage(uri)
                if (newPath != null) {
                    // Also remove the previous icon from DB to avoid garbage.
                    iconUrl.value?.let { url ->
                        if (url.isNotEmpty()) {
                            try {
                                imageUploader.removeImage(url)
                            } catch (e: Exception) {
                                Log.e("UserSettings", e.message.orEmpty())
                            }
                        }
                    }
                    // We choose the new image only if not null
                    _icon.value = newPath
                    newPath
                } else {
                    null
                }
            }

            val iconUrl = _icon.value
            val imgInst = if (iconUrl == null) {
                null
            } else {
                ImageInstance(iconUrl)
            }

            val owner = profileService.getLoggedInUserID()!!
            var meetUp = MeetUp(
                uuid.orEmpty(),
                owner,
                imgInst,
                name.value!!,
                description.value!!,
                startDate.value!!,
                endDate.value!!,
                location.value!!,
                tags.value.orEmpty(),
                hasMaxCapacity.value!!,
                capacity.value!!,
                participantsId.value!!,
                criterionAge.value,
                criterionGender.value,
                marker.value
            )
            if (uuid == null) {
                // create new meetup
                // Make sure the meetup start at least now when it is created
                if (startDate.value!!.isBefore(Calendar.getInstance())) {
                    _startDate.value = Calendar.getInstance()
                    checkDateIntegrity()
                    meetUp = meetUp.copy(startDate = startDate.value!!, endDate = endDate.value!!)
                }
                uuid = meetUpRepository.create(meetUp)
                // Notify sending result
                _sendSuccess.postValue(uuid != null)
            } else {
                // Edit existing one
                meetUpRepository.edit(uuid!!, meetUp)
                // Notify sending result
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
        val startDateVal = startDate.value!!
        val endDateVal = endDate.value!!
//        // Check that startDate is not too much in the future
//        if (!startDateVal.isBefore(maxStartDate)) {
//            _startDate.value = maxStartDate
//        }
//        // Check that endDate is not too much in the future
        if (!endDateVal.isBefore(maxEndDate)) {
            _endDate.value = maxEndDate
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

    fun setLatLng(p0: LatLng) {
        _location.value = p0.toLocation()
    }
}
