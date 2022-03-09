package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import androidx.fragment.app.FragmentManager
import com.github.palFinderTeam.palfinder.fragment.picker.DatePickerFragment
import com.github.palFinderTeam.palfinder.fragment.picker.TimePickerFragment
import java.util.concurrent.CompletableFuture

data class SimpleTime(val hour: Int, val minute: Int)
data class SimpleDate(val year: Int, val month: Int, val day: Int){
    fun withTime(time: SimpleTime): Calendar{
        val c = Calendar.getInstance()
        c.set(year, month, day, time.hour, time.minute)
        return c
    }
}

fun Calendar.isBefore(other: Calendar): Boolean{
    return this.timeInMillis <= other.timeInMillis
}
fun Calendar.isDeltaBefore(other: Calendar, delta: Int): Boolean{
    return this.timeInMillis + delta <= other.timeInMillis
}

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