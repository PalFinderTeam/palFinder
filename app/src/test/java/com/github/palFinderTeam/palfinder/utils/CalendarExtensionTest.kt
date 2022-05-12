package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.time.isBefore
import com.github.palFinderTeam.palfinder.utils.time.isDeltaBefore
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class CalendarExtensionTest {
    @Test
    fun testIsBefore(){
        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(0)

        val date2 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date2.timeInMillis).thenReturn(1)

        Assert.assertEquals(true,date1.isBefore(date2))
        Assert.assertEquals(false, date2.isBefore(date1))
    }
    @Test
    fun testIsBeforeDelta(){
        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(0)

        val date2 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date2.timeInMillis).thenReturn(10)

        Assert.assertEquals(true,date1.isDeltaBefore(date2,9))
        Assert.assertEquals(false, date1.isDeltaBefore(date2,11))
    }
}