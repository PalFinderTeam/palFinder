package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import android.location.Location
import android.media.Image
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.util.*
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.N)
data class MeetUpDumb(
    val icon: Image?,
    val name: String,
    val description: String,
    val startDate: Calendar,
    val endDate: Calendar,
    val location: Location?,
    val tags: List<Objects>?, // TODO - Change to tag
    val capacity: Int,
) : Serializable{
    fun distanceInKm(currentLocation: Location): Double{
        return Random(45).nextDouble();
    }
    fun isFull(): Boolean{
        return capacity <= 45;
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