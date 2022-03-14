package com.github.palFinderTeam.palfinder.meetups.activities

import android.icu.util.Calendar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MeetUpCreationViewModel: ViewModel() {
    var uuid: String = "dummy"
    val endDate: MutableLiveData<Calendar> by lazy {
        MutableLiveData<Calendar>()
    }
    val startDate: MutableLiveData<Calendar> by lazy {
        MutableLiveData<Calendar>()
    }
    var capacity: Int = Int.MAX_VALUE
    var hasMaxCapacity: Boolean = false
}