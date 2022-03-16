package com.github.palFinderTeam.palfinder.meetups.activities

import android.icu.util.Calendar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.maps.model.LatLng

class MeetUpCreationViewModel: ViewModel() {
    var uuid: String = "dummy"
    val endDate: MutableLiveData<Calendar> by lazy {
        MutableLiveData<Calendar>()
    }
    val startDate: MutableLiveData<Calendar> by lazy {
        MutableLiveData<Calendar>()
    }
    val location: MutableLiveData<Location> by lazy {
        MutableLiveData<Location>()
    }
    var capacity: Int = Int.MAX_VALUE
    var hasMaxCapacity: Boolean = false

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