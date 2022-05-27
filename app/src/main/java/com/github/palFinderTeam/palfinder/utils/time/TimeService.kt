package com.github.palFinderTeam.palfinder.utils.time

import android.icu.util.Calendar

/**
 * Represents a class that can give the current time.
 */
interface TimeService {
    fun now(): Calendar
}