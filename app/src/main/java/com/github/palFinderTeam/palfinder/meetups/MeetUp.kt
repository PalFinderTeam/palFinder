package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.isBefore

data class MeetUp(
    val uuid: String,
    val creator: TempUser, // TODO -  Change to Real User
    val icon: String,
    val name: String,
    val description: String,
    val startDate: Calendar,
    val endDate: Calendar,
    val location: Location,
    val tags: List<String>, // TODO - Change to tag
    val capacity: Int,
    val participants: List<TempUser>, // TODO -  Change to Real User
): java.io.Serializable {
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
        return endDate.isBefore(now)
    }
    fun isStarted(now: Calendar):Boolean{
        return startDate.isBefore(now)
    }
}