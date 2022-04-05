package com.github.palFinderTeam.palfinder.utils.time

import android.icu.util.Calendar

interface TimeService {
    fun now(): Calendar
}