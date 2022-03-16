package com.github.palFinderTeam.palfinder.utils

import android.content.Context
import com.github.palFinderTeam.palfinder.R
import java.io.Serializable
import kotlin.math.*

private const val earthRadius = 6371e3
private const val degToRad = PI/180
private const val mToKm = 1/1000f

/**
 * Class to represent a position on a map.
 * Alternative to android.Location which is not Serializable
 * @param longitude
 * @param latitude
 */
data class Location(val longitude: Double, val latitude: Double): Serializable{
    companion object{
        const val MIN_M_DISTANCE = 0.01
        const val MIN_KM_DISTANCE = 1.0
        const val KM_TO_M = 0.001
    }

    fun distanceInKm(other: Location): Double{
        val phi1 = latitude * degToRad
        val phi2 = other.latitude * degToRad
        val deltaPhi = (other.latitude-latitude) * degToRad
        val deltaLambda = (other.longitude-longitude) * degToRad

        val a = sin(deltaPhi/2) * sin(deltaPhi/2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda/2) * sin(deltaLambda/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))

        val distance = earthRadius * c
        return distance * mToKm
    }

    override fun toString(): String{
        return "${longitude},${latitude}"
    }

    fun prettyDistanceTo(context: Context, other: Location):String{
        val dist = distanceInKm(other)
        return when {
            dist < MIN_M_DISTANCE -> {
                context.getString(R.string.pretty_location_too_small)
            }
            dist < MIN_KM_DISTANCE -> {
                val display = (dist * KM_TO_M).roundToInt().toString()
                context.getString(R.string.pretty_location_m, display)
            }
            else -> {
                val display = dist.roundToInt().toString()
                context.getString(R.string.pretty_location_m, display)
            }
        }
    }
}