package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.Location
import kotlinx.serialization.Serializable

@Serializable
data class MeetUp(
    val creator: TempUser, // TODO -  Change to Real User
    val icon: String,
    val name: String,
    val description: String,
    val startDate: Long,
    val endDate: Long,
    val location: Location,
    val tags: List<String>, // TODO - Change to tag
    val capacity: Int,
    val participants: List<TempUser>, // TODO -  Change to Real User
) {
    fun distanceInKm(currentLocation: Location): Double{
        return location.distanceInKm(currentLocation)
    }
    fun isFull(): Boolean{
        return capacity <= participants.size
    }
    fun canJoin(now: Calendar):Boolean{
        return !isFull() && !isFinished(now)
    }
    fun isFinished(now: Calendar): Boolean {
        return now.timeInMillis > endDate
    }
    fun isStarted(now: Calendar):Boolean{
        return now.timeInMillis > startDate
    }
}