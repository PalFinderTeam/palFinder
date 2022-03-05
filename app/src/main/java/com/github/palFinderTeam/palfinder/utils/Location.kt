package com.github.palFinderTeam.palfinder.utils

import kotlin.math.*

data class Location(val longitude: Double, val latitude: Double){
    fun distanceInKm(other: Location): Double{
        return 2*asin(sqrt(
            sin((latitude - other.latitude) / 2).pow(2.0) +
                cos(latitude)*cos(other.latitude)* sin((longitude - other.longitude) / 2).pow(2)
        ))
    }
}