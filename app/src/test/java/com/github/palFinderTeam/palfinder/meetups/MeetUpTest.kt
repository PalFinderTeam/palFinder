package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.Location
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import java.util.*

class MeetUpTest {
    @Test
    fun isFullTest(){
        val calendar = Mockito.mock(Calendar::class.java)

        val m = MeetUp(
            TempUser(null, "Bob"),
            null,
            "dummy",
            "dummy",
            calendar!!,
            calendar!!,
            Location(0.0,0.0),
            emptyList(),
            1,
            listOf(TempUser(null, "Alice"))
        )
        assertEquals(m.isFull(), true)
    }

    @Test
    fun canJoin(){
        val eventDate = Mockito.mock(Calendar::class.java)
        Mockito.`when`(eventDate.timeInMillis).thenReturn(1)

        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(0)

        val m = MeetUp(
            TempUser(null, "Bob"),
            null,
            "dummy",
            "dummy",
            eventDate!!,
            eventDate,
            Location(0.0,0.0),
            emptyList(),
            1,
            emptyList()
        )
        assertEquals(m.canJoin(now), true)
    }

    @Test
    fun iStarted(){
        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(1)

        val date2 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date2.timeInMillis).thenReturn(0)

        val m = MeetUp(
            TempUser(null, "Bob"),
            null,
            "dummy",
            "dummy",
            date2!!,
            date2,
            Location(0.0,0.0),
            emptyList(),
            1,
            emptyList()
        )
        assertEquals(m.isStarted(date1), true)
    }
}