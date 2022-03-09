package com.github.palFinderTeam.palfinder

import com.github.palFinderTeam.palfinder.utils.PrettyDate
import org.junit.Assert
import org.junit.Test
import java.util.*

class PrettyDateTest {

    @Test
    fun PrettyDateJustNowDisplaysCorrect() {
        val d = Date(122, 2, 6, 14, 1, 0)
        Assert.assertEquals(PrettyDate.ZERO, PrettyDate(d).timeSince(d))
    }

    @Test
    fun PrettyDateSingularMinuteDisplaysCorrect() {
        val from = Date(122, 2, 6, 14, 1, 0)
        val to =   Date(122, 2, 6, 14, 2, 4)
        Assert.assertEquals("1 minute", PrettyDate(to).timeSince(from))
    }

    @Test
    fun PrettyDatePluralMonthDisplaysCorrect() {
        val from = Date(122, 5, 6, 14, 1, 0)
        val to =   Date(122, 8, 6, 14, 1, 0)
        Assert.assertEquals("3 months", PrettyDate(to).timeSince(from))
    }

}