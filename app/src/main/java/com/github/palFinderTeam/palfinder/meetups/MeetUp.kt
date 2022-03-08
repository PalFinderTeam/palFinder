package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import android.location.Location
import android.media.Image
import java.util.*

data class MeetUp(
    val creator: TempUser, // TODO -  Change to Real User
    val icon: Image?,
    val name: String,
    val description: String,
    val startDate: Calendar,
    val endDate: Calendar,
    val location: Location,
    val tags: List<Objects>, // TODO - Change to tag
    val capacity: Int,
    val participants: List<TempUser>, // TODO -  Change to Real User
) {
    fun distanceInKm(currentLocation: Location): Float{
        return location.distanceTo(currentLocation)
    }
    fun isFull(): Boolean{
        return capacity <= participants.size
    }
    fun canJoin(now: Calendar):Boolean{
        return !isFull() && !isFinished(now)
    }
    fun isFinished(now: Calendar): Boolean {
        return now.timeInMillis >= endDate.timeInMillis
    }
    fun isStarted(now: Calendar):Boolean{
        return now.timeInMillis >= startDate.timeInMillis
    }
}