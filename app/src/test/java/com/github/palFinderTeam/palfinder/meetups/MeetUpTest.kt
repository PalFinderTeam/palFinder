package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.Location
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class MeetUpTest {
    var meetUp: MeetUp? = null

    @Before
    fun initMeetup(){
        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(0)

        val date2 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date2.timeInMillis).thenReturn(1)

        meetUp = MeetUp(
            "dummy",
            TempUser("", "Bob"),
            "",
            "dummy",
            "dummy",
            date1,
            date2,
            Location(0.0,0.0),
            emptyList(),
            true,
            2,
            listOf(TempUser("", "Alice"))
        )
    }

    @Test
    fun isFullTest(){
        assertEquals(false, meetUp!!.isFull())
    }

    @Test
    fun canJoin(){
        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(0)

        assertEquals(true, meetUp!!.canJoin(now))
    }

    @Test
    fun iStarted(){
        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(0)

        assertEquals( true, meetUp!!.isStarted(now))
    }
}