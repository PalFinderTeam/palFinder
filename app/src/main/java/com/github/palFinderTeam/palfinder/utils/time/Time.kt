package com.github.palFinderTeam.palfinder.utils.time

import android.icu.util.Calendar
import androidx.fragment.app.FragmentManager
import com.github.palFinderTeam.palfinder.fragment.picker.DatePickerFragment
import com.github.palFinderTeam.palfinder.fragment.picker.TimePickerFragment
import java.util.concurrent.CompletableFuture

/**
 * Simple Class for the result of TimePickerFragment
 *
 * @param hour: Hour
 * @param minute: Minute
 */
data class SimpleTime(val hour: Int, val minute: Int)

/**
 * Simple Class for the result of DatePickerFragment
 *
 * @param year: Year
 * @param month: Month
 * @param day: Day
 */
data class SimpleDate(val year: Int, val month: Int, val day: Int) {

    /**
     * @param time: Time in the day
     * @return Calendar with this date and [time] time
     */
    fun withTime(time: SimpleTime): Calendar {
        val c = Calendar.getInstance()
        c.set(year, month, day, time.hour, time.minute)
        return c
    }
}

/**
 * @param other: Date to compare with
 * @return if this is before or equals to [other]
 */
fun Calendar.isBefore(other: Calendar): Boolean {
    return this.timeInMillis <= other.timeInMillis
}

/**
 * @param other: Date to compare with
 * @return if this + delta millisecond is before or equals to [other]
 */
fun Calendar.isDeltaBefore(other: Calendar, delta: Int): Boolean {
    return this.timeInMillis + delta <= other.timeInMillis
}

/**
 * Transform this Calendar to SimpleDate
 */
fun Calendar.toSimpleDate(): SimpleDate {
    return SimpleDate(
        this.get(Calendar.YEAR),
        this.get(Calendar.MONTH),
        this.get(Calendar.DAY_OF_MONTH)
    )
}


/**
 * Transform this Calendar to SimpleTime
 */
fun Calendar.toSimpleTime(): SimpleTime {
    return SimpleTime(this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE))
}

/**
 * Ask the user a date+time with a UI
 *
 * @param supportFragmentManager
 * @param date: Default Date
 * @param time: Default time
 * @return future of the Calendar
 */
fun askTime(
    supportFragmentManager: FragmentManager,
    date: SimpleDate? = null,
    time: SimpleTime? = null,
    minDate: Calendar? = null,
    maxDate: Calendar? = null
): CompletableFuture<Calendar> {
    val dateFrag = DatePickerFragment(date, minDate, maxDate)
    val timeFrag = TimePickerFragment(time)
    var dateRes: SimpleDate? = null

    dateFrag.show(supportFragmentManager, "datePicker")
    dateFrag.value.thenAccept {
        timeFrag.show(supportFragmentManager, "timePicker")
        dateRes = it
    }
    return timeFrag.value.thenApply {
        dateRes!!.withTime(it)
    }
}

/**
 * Ask the user a date with a UI
 *
 * @param supportFragmentManager
 * @param date: Default Date
 * @return future of the Calendar
 */
fun askDate(supportFragmentManager: FragmentManager, date: SimpleDate? = null): CompletableFuture<Calendar>{
    val dateFrag = DatePickerFragment(date)
    dateFrag.show(supportFragmentManager, "datePicker")

    return dateFrag.value.thenApply {
        it.withTime(SimpleTime(0,0))
    }
}

/**
 * Get the maximum starting date for a meetup
 */
val maxStartDate: Calendar
    get() {
        val maxDate = Calendar.getInstance()
        //event start max 5 days after today
        maxDate.add(Calendar.DAY_OF_MONTH, 7)
        return maxDate
    }

/**
 * Get the maximum end value for a meetup
 */
val maxEndDate: Calendar
    get() {
        val maxDate = maxStartDate
        //event end max 7 days after today
        maxDate.add(Calendar.DAY_OF_MONTH, 2)
        return maxDate
    }