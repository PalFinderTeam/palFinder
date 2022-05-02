package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import kotlin.math.abs
import kotlin.math.floor

/**
 * Class to represent time elapsed since another date until today (or specified date)
 * in a pretty format "30 minutes ago"
 */
class PrettyDate(private val now: Calendar = Calendar.getInstance()) {

    companion object {
        const val ZERO = "just now"
        const val FUTURE = "in %s"
        const val PAST = "%s ago"
        const val MIN_AGE = 13
        const val MAX_AGE = 66
    }

    /**
     * Get time difference pretty printed (works for both times in the future and
     * in the past)
     */
    fun timeDiff(d: Calendar): String {
        val secDiff = secBetween(now, d)

        // Check if greater than a time unit
        TimeUnit.values().forEach {
            val interval = secDiff / it.toSecFactor
            if (interval >= 1) {
                val nb = floor(interval).toInt()
                val timeWithUnit = "$nb " + it.unitName + pluralInt(nb)

                return String.format(prettyFormat(now, d), timeWithUnit)
            }
        }

        return ZERO
    }

    /**
     * Get the string formatting for the pretty printing ("in" or "ago")
     */
    private fun prettyFormat(now: Calendar, target: Calendar): String {
        return if (now.timeInMillis < target.timeInMillis) FUTURE else PAST
    }

    /**
     * Get absolute time in seconds between 2 calendars
     */
    private fun secBetween(d1: Calendar, d2: Calendar): Double {
        return floor(abs((d1.timeInMillis - d2.timeInMillis) / 1000).toDouble())
    }

    // Helper method of `timeSince` to define plural
    private fun pluralInt(i: Int): String {
        return if (i == 1) "" else "s"
    }
}
