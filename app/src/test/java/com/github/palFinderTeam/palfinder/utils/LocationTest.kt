package com.github.palFinderTeam.palfinder.utils

import org.junit.Assert
import org.junit.Test

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
}