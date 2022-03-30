package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.time.TimeService

class MockTimeService : TimeService {
    private var date = Calendar.getInstance()
    override fun now(): Calendar {
        return date
    }
    fun setDate(now: Calendar){
        date = now
    }
}