package com.github.palFinderTeam.palfinder.utils

import android.content.Context
import com.github.palFinderTeam.palfinder.R
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito

class LocationTest {
    @Test
    fun distanceLatitude(){
        val p1 = Location(0.0,0.0)
        val p2 = Location(0.0, 1.0)
        Assert.assertEquals(p1.distanceInKm(p2), 111.0, 0.5)
    }
    @Test
    fun distanceTest(){
        val p1 = Location(0.0,0.0)
        val p2 = Location(1.0, 1.0)
        Assert.assertEquals(p1.distanceInKm(p2), 157.0, 0.5)
    }
    @Test
    fun distancePretty(){
        val cont = Mockito.mock(Context::class.java)
        Mockito.`when`(cont.getString(R.string.pretty_location_too_small)).thenReturn("Here")
        Mockito.`when`(cont.getString(eq(R.string.pretty_location_m), any(String::class.java))).thenReturn("In M")
        Mockito.`when`(cont.getString(eq(R.string.pretty_location_km), any(String::class.java))).thenReturn("In Km")

        val p1 = Location(0.0,0.0)
        val p2 = Location(0.0, 0.00001)
        val p3 = Location(0.0, 0.001)
        val p4 = Location(0.0, 1.0)

        Assert.assertEquals(p1.prettyDistanceTo(cont, p2), "Here")
        Assert.assertEquals(p1.prettyDistanceTo(cont, p3), "In M")
        Assert.assertEquals(p1.prettyDistanceTo(cont, p4), "In Km")
    }
    @Test
    fun lonLatToLocationWorks(){
        val p1 = LatLng(0.0,1.0)
        val p2 = Location.latLngToLocation(p1)
        Assert.assertEquals(p2.latitude, p1.latitude, 0.0001)
    }
}