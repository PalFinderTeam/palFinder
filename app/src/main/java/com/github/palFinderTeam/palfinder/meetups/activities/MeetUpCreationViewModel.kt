package com.github.palFinderTeam.palfinder.meetups.activities

import android.icu.util.Calendar
import androidx.lifecycle.ViewModel

class MeetUpCreationViewModel: ViewModel() {
    var uuid: String = "dummy"
    var endDate: Calendar = Calendar.getInstance()
    var startDate: Calendar = Calendar.getInstance()
    var capacity: Int = Int.MAX_VALUE
    var hasMaxCapacity: Boolean = false
}