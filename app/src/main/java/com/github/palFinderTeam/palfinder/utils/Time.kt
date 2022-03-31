package com.github.palFinderTeam.palfinder.utils

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
data class SimpleDate(val year: Int, val month: Int, val day: Int){

    /**
     * @param time: Time in the day
     * @return Calendar with this date and [time] time
     */
    fun withTime(time: SimpleTime): Calendar{
        val c = Calendar.getInstance()
        c.set(year, month, day, time.hour, time.minute)
        return c
    }
}

/**
 * @param other: Date to compare with
 * @return if this is before or equals to [other]
 */
fun Calendar.isBefore(other: Calendar): Boolean{
    return this.timeInMillis <= other.timeInMillis
}

/**
 * @param other: Date to compare with
 * @return if this + delta millisecond is before or equals to [other]
 */
fun Calendar.isDeltaBefore(other: Calendar, delta: Int): Boolean{
    return this.timeInMillis + delta <= other.timeInMillis
}

fun Calendar.toSimpleDate(): SimpleDate {
    return SimpleDate(this.get(Calendar.YEAR), this.get(Calendar.MONTH), this.get(Calendar.DAY_OF_MONTH))
}

fun Calendar.toSimpleTime(): SimpleTime {
    return SimpleTime(this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE))
}

/**
 * Ask the user a date with a UI
 *
 * @param supportFragmentManager
 * @param date: Default Date
 * @param time: Default time
 * @return future of the Calendar
 */
fun askTime(supportFragmentManager: FragmentManager, date: SimpleDate? = null, time: SimpleTime? = null): CompletableFuture<Calendar>{
    val dateFrag = DatePickerFragment(date)
    val timeFrag = TimePickerFragment(time)
    var dateRes: SimpleDate? = null

    dateFrag.show(supportFragmentManager, "datePicker")
    dateFrag.value.thenAccept {
        timeFrag.show(supportFragmentManager, "timePicker")
        dateRes = it
    }
    return timeFrag.value.thenApply{
        dateRes!!.withTime(it)
    }
}