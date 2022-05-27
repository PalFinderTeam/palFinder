package com.github.palFinderTeam.palfinder.utils.time

import android.icu.util.Calendar
import javax.inject.Inject

/**
 * Class that provides methods to get the current time.
 */
class RealTimeService @Inject constructor(): TimeService{
    override fun now(): Calendar {
        return Calendar.getInstance()
    }
}