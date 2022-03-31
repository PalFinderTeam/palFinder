package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import org.mockito.Mockito

class MockTimeService : TimeService {
    private var date = Mockito.mock(Calendar::class.java)
    init {
        Mockito.`when`(date.timeInMillis).thenReturn(69)
    }
    override fun now(): Calendar {
        return date
    }
    fun setDate(now: Calendar){
        date = now
    }
}