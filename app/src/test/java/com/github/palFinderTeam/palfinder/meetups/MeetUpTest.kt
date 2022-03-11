package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Location
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*

class MeetUpTest {
    var meetUp: MeetUp? = null

    @Before
    fun initMeetup(){
        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(0)

        val date2 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date2.timeInMillis).thenReturn(1)

        val user = ProfileUser("dummy","dummy","dummy", Date())

        meetUp = MeetUp(
            "dummy",
            user,
            "",
            "dummy",
            "dummy",
            date1,
            date2,
            Location(0.0,0.0),
            emptyList(),
            true,
            2,
            mutableListOf(user)
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
    fun cannotJoin(){
        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(5)

        assertEquals(false, meetUp!!.canJoin(now))
    }

    @Test
    fun iStarted(){
        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(0)

        assertEquals( true, meetUp!!.isStarted(now))
    }

    @Test
    fun join(){
        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(0)
        val user = ProfileUser("dummy1","dummy2","dummy", Date())

        meetUp!!.join(now, user)
        assertEquals( true, meetUp!!.isParticipating(user))
    }

    @Test
    fun joinAndLeave(){
        val now = Mockito.mock(Calendar::class.java)
        Mockito.`when`(now.timeInMillis).thenReturn(0)
        val user = ProfileUser("dummy1","dummy2","dummy", Date())

        meetUp!!.join(now, user)
        meetUp!!.leave(user)
        assertEquals( false, meetUp!!.isParticipating(user))
    }


}