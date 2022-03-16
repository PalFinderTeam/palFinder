package com.github.palFinderTeam.palfinder.map

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.TempUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class MapUtilsTest {



    private val mockMap = Mockito.mock(GoogleMap::class.java)
    private val marker1 = Mockito.mock(Marker::class.java)
    private val marker2 = Mockito.mock(Marker::class.java)
    private val marker3 = Mockito.mock(Marker::class.java)
    private val marker4 = Mockito.mock(Marker::class.java)

    private val date1 = Mockito.mock(Calendar::class.java)
    private val date2 = Mockito.mock(Calendar::class.java)


    private val utils = MapUtils()


    private val meetup1 = MeetUp(
        "1",
        TempUser("", "tempUser1"),
        "",
        "meetUp1Name",
        "meetUp1Description",
        date1,
        date2,
        Location(0.0,0.0),
        emptyList(),
        false,
        2,
        mutableListOf(TempUser("", "tempUser2"))
    )

    private val meetup2 = MeetUp(
        "2",
        TempUser("", "tempUser2"),
        "",
        "meetUp2Name",
        "meetUp2Description",
        date1,
        date2,
        Location(15.0, -15.0),
        emptyList(),
        false,
        2,
        mutableListOf(TempUser("", "tempUser1"))
    )

    private val meetup3 = MeetUp(
        "3",
        TempUser("", "tempUser3"),
        "",
        "meetUp3Name",
        "meetUp3Description",
        date1,
        date2,
        Location(-30.0, 45.0),
        emptyList(),
        false,
        4,
        mutableListOf(TempUser("", "tempUser"))
    )

    private val meetup4 = MeetUp(
        "4",
        TempUser("", "tempUser4"),
        "",
        "meetUp4Name",
        "meetUp4Description",
        date1,
        date2,
        Location( 69.0, 42.0),
        emptyList(),
        false,
        1337,
        mutableListOf(TempUser("", "tempUser4"))
    )

    @Before
    fun init(){

        val position1 = LatLng(meetup1.location.latitude, meetup1.location.longitude)
        val position2 = LatLng(meetup2.location.latitude, meetup2.location.longitude)
        val position3 = LatLng(meetup3.location.latitude, meetup3.location.longitude)
        val position4 = LatLng(meetup4.location.latitude, meetup4.location.longitude)

        Mockito.`when`(mockMap.addMarker(MarkerOptions().position(position1).title(meetup1.uuid))).thenReturn(marker1)
        Mockito.`when`(mockMap.addMarker(MarkerOptions().position(position2).title(meetup1.uuid))).thenReturn(marker2)
        Mockito.`when`(mockMap.addMarker(MarkerOptions().position(position3).title(meetup1.uuid))).thenReturn(marker3)
        Mockito.`when`(mockMap.addMarker(MarkerOptions().position(position4).title(meetup1.uuid))).thenReturn(marker4)

    }

    @Test
    fun testSetMap(){
        utils.setMap(mockMap)
        Assert.assertEquals(true, utils.mapReady)
    }


    @Test
    fun testAddMeetupMarkerMapNotReady(){
        utils.mapReady = false
        utils.addMeetupMarker(meetup1)
        Assert.assertEquals(meetup1, utils.getMeetup(meetup1.uuid))
        Assert.assertEquals(null, utils.getMarker(meetup1.uuid))
    }

    @Test
    fun testAddMeetupMarkerMapReady(){
        utils.mapReady = true
        utils.addMeetupMarker(meetup2)
        Assert.assertEquals(meetup2, utils.getMeetup(meetup2.uuid))
        Assert.assertEquals(marker2, utils.getMarker(meetup2.uuid))
    }

    @Test
    fun testRemoveMarkerMapNotReady(){
        utils.mapReady = false
        utils.addMeetupMarker(meetup3)
        utils.removeMarker(meetup3.uuid)
        Assert.assertEquals(null, utils.getMeetup(meetup3.uuid))
    }

    @Test
    fun testRemoveMarkerMapReady(){
        utils.mapReady = true
        utils.addMeetupMarker(meetup3)
        utils.removeMarker(meetup3.uuid)
        Assert.assertEquals(null, utils.getMeetup(meetup3.uuid))
        Assert.assertEquals(null, utils.getMarker(meetup3.uuid))

    }

    @Test
    fun clearMarkers(){
        utils.mapReady = true
        utils.addMeetupMarker(meetup3)
        utils.addMeetupMarker(meetup4)
        utils.clearMarkers()
        Assert.assertEquals(meetup3, utils.getMeetup(meetup3.uuid))
        Assert.assertEquals(meetup4, utils.getMeetup(meetup4.uuid))
        Assert.assertEquals(null, utils.getMarker(meetup3.uuid))
        Assert.assertEquals(null, utils.getMarker(meetup4.uuid))
    }

    @Test
    fun clearMap(){
        utils.mapReady = true
        utils.addMeetupMarker(meetup3)
        utils.addMeetupMarker(meetup4)
        utils.clearMap()
        Assert.assertEquals(null, utils.getMeetup(meetup3.uuid))
        Assert.assertEquals(null, utils.getMeetup(meetup4.uuid))
        Assert.assertEquals(null, utils.getMarker(meetup3.uuid))
        Assert.assertEquals(null, utils.getMarker(meetup4.uuid))
    }

    @Test
    fun testRefresh(){
        utils.mapReady = false
        utils.addMeetupMarker(meetup1)
        utils.addMeetupMarker(meetup2)
        utils.refresh()
        Assert.assertEquals(meetup1, utils.getMeetup(meetup1.uuid))
        Assert.assertEquals(meetup2, utils.getMeetup(meetup2.uuid))
        Assert.assertEquals(null, utils.getMarker(meetup1.uuid))
        Assert.assertEquals(null, utils.getMarker(meetup2.uuid))
        utils.mapReady = true
        Assert.assertEquals(meetup1, utils.getMeetup(meetup1.uuid))
        Assert.assertEquals(meetup2, utils.getMeetup(meetup2.uuid))
        Assert.assertEquals(marker1, utils.getMarker(meetup1.uuid))
        Assert.assertEquals(marker2, utils.getMarker(meetup2.uuid))
    }

}