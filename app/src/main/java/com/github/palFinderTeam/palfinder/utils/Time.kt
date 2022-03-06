package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.fragment.picker.DatePickerFragment
import com.github.palFinderTeam.palfinder.fragment.picker.TimePickerFragment
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

data class SimpleTime(val hour: Int, val minute: Int)
data class SimpleDate(val year: Int, val month: Int, val day: Int){
    @RequiresApi(Build.VERSION_CODES.N)
    fun withTime(time: SimpleTime): Calendar{
        val c = Calendar.getInstance()
        c.set(year, month, day, time.hour, time.minute)
        return c
    }
}

@RequiresApi(Build.VERSION_CODES.N)
fun Calendar.toSimpleDate():SimpleDate {
    return SimpleDate(this.get(Calendar.YEAR), this.get(Calendar.MONTH), this.get(Calendar.DAY_OF_MONTH))
}

@RequiresApi(Build.VERSION_CODES.N)
fun Calendar.toSimpleTime():SimpleTime {
    return SimpleTime(this.get(Calendar.HOUR), this.get(Calendar.MINUTE))
}

@RequiresApi(Build.VERSION_CODES.N)
fun askTime(supportFragmentManager: FragmentManager): CompletableFuture<Calendar>{
    val dateFrag = DatePickerFragment()
    val timeFrag = TimePickerFragment()
    var date: SimpleDate? = null

    dateFrag.show(supportFragmentManager, "datePicker")
    dateFrag.value.thenAccept {
        timeFrag.show(supportFragmentManager, "timePicker")
        date = it
    }
    return timeFrag.value.thenApply{
        date!!.withTime(it)
    }
}