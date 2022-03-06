package com.github.palFinderTeam.palfinder.utils

import kotlin.math.*

private const val earthRadius = 6371e3

data class Location(val longitude: Double, val latitude: Double){
    fun distanceInKm(other: Location): Double{
        val phi1 = latitude * PI/180
        val phi2 = other.latitude * PI/180
        val deltaPhi = (other.latitude-latitude) * PI/180
        val deltaLambda = (other.longitude-longitude) * PI/180

        val a = sin(deltaPhi/2) * sin(deltaPhi/2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda/2) * sin(deltaLambda/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))

        val d = earthRadius * c
        return d / 1000
    }
}