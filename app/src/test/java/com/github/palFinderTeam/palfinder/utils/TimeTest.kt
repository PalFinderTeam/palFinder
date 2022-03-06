package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import org.junit.Assert.*
import org.junit.Test

class TimeTest {
    @Test
    fun combine(){
        val r = SimpleDate(2022,0,0).withTime(SimpleTime(10,0))
        val e = Calendar.getInstance()
        e.set(2022,0,0,10,0)
        assertEquals(r, e)
    }

    @Test
    fun calendarToSimpleDate(){
        val e = SimpleDate(2022,0,0)
        val r = Calendar.getInstance()
        r.set(2022,0,0,10,0)

        assertEquals(r.toSimpleDate(), e)
    }

    @Test
    fun calendarToSimpleTime(){
        val e = SimpleTime(10,0)
        val r = Calendar.getInstance()
        r.set(2022,0,0,10,0)

        assertEquals(r.toSimpleTime(), e)
    }
}