package com.github.palFinderTeam.palfinder.utils

import java.util.*
import kotlin.math.floor

const val EXTRA_MESSAGE = "com.github.palFinderTeam.palFinder.MESSAGE"

/**
 * Class to represent time elapsed since another date until today (or specified date)
 * in a pretty format "30 minutes ago"
 * TODO: If target date is after current date, then change text to "in 30 minutes"
 * TODO: Ask if min API can be changed to 24 so as to use the Calendar android class
 * TODO: Find a smarter way to inject a specific date for testing
 */
class PrettyDate(private val now: Date = Date()) {

    companion object{
        const val ZERO = "just now"
    }

    // Time since formatting
    fun timeSince(d: Date): String {
        val secDiff = floor(((now.time - d.time) / 1000).toDouble())

        // Check if greater than a time unit
        TimeUnit.values().forEach {
            val interval = secDiff / it.toSecFactor
            if (interval > 1) {
                val nb = floor(interval).toInt()
                return "$nb " + it.unitName + pluralInt(nb)
            }
        }

        return ZERO
    }

    // Helper method of `timeSince` to define plural
    private fun pluralInt(i: Int): String {
        return if (i == 1) "" else "s"
    }
}
