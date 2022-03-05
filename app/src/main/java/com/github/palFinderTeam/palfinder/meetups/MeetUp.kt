package com.github.palFinderTeam.palfinder.meetups

import android.media.Image
import java.time.LocalDateTime
import java.util.*

data class MeetUp(
    val creator: TempUser, // TODO -  Change to Real User
    val icon: Image,
    val name: String,
    val description: String,
    val startDate: Calendar,
    val endDate: Calendar,
    val location: Location,
    val tags: List<Objects>, // TODO - Change to tag
    val capacity: Int,
    val participants: List<TempUser>, // TODO -  Change to Real User
) {
    fun distanceInKm(currentLocation: Location): Double{
        return location.distanceInKm(currentLocation)
    }
    fun isFull(): Boolean{
        return capacity <= participants.size
    }
    fun canJoin():Boolean{
        return !isFull()
    }
    fun isFinished(): Boolean {
        return Calendar.getInstance().timeInMillis >= endDate.timeInMillis
    }
    fun isStarted():Boolean{
        return Calendar.getInstance().timeInMillis >= startDate.timeInMillis
    }
}