package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.Location
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito

class MeetUpTest {
    @Test
    fun isFullTest(){
        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(0)

        val date2 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date2.timeInMillis).thenReturn(0)

        val m = MeetUp(
            TempUser("", "Bob"),
            "",
            "dummy",
            "dummy",
            date1,
            date2,
            Location(0.0,0.0),
            emptyList(),
            1,
            listOf(TempUser("", "Alice"))
        )
        assertEquals(true, m.isFull())
    }

    @Test
    fun canJoin(){
        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(0)

        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(0)

        val date2 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date2.timeInMillis).thenReturn(1)

        val m = MeetUp(
            TempUser("", "Bob"),
            "",
            "dummy",
            "dummy",
            date1,
            date2,
            Location(0.0,0.0),
            emptyList(),
            1,
            emptyList()
        )
        assertEquals(true, m.canJoin(now))
    }

    @Test
    fun iStarted(){
        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(0)

        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(0)

        val date2 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date2.timeInMillis).thenReturn(1)

        val m = MeetUp(
            TempUser("", "Bob"),
            "",
            "dummy",
            "dummy",
            date1,
            date2,
            Location(0.0,0.0),
            emptyList(),
            1,
            emptyList()
        )
        assertEquals( true, m.isStarted(now))
    }
}