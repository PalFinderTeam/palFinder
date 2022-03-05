package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi

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