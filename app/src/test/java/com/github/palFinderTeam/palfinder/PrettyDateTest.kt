package com.github.palFinderTeam.palfinder

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.time.PrettyDate
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito


class PrettyDateTest {

    private val millisToSec = 1000.toLong()
    private val secToMinutes = 60.toLong()
    private val secToHours = 3600.toLong()
    private val secToDays = (3600*24).toLong()

    @Test
    fun prettyDateJustNowDisplaysCorrect() {
        val d = Mockito.mock(Calendar::class.java)
        Mockito.`when`(d.timeInMillis).thenReturn(0)

        Assert.assertEquals(PrettyDate.ZERO, PrettyDate(d).timeDiff(d))
    }

    @Test
    fun prettyDatePastSingularMinuteDisplaysCorrect() {
        val d = Mockito.mock(Calendar::class.java)
        Mockito.`when`(d.timeInMillis).thenReturn(0)

        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(millisToSec * secToMinutes * 1)

        Assert.assertEquals("1 minute ago", PrettyDate(now).timeDiff(d))
    }

    @Test
    fun prettyDatePastPluralDaysDisplaysCorrect() {
        val d = Mockito.mock(Calendar::class.java)
        Mockito.`when`(d.timeInMillis).thenReturn(0)

        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(millisToSec * secToDays * 3)

        Assert.assertEquals("3 days ago", PrettyDate(now).timeDiff(d))
    }

    @Test
    fun prettyDateFutureSecondDisplaysCorrect() {
        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(0)

        val d = Mockito.mock(Calendar::class.java)
        Mockito.`when`(d.timeInMillis).thenReturn(millisToSec * 1)
        Assert.assertEquals("in 1 second", PrettyDate(now).timeDiff(d))
    }

}