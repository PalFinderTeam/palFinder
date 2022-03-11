package com.github.palFinderTeam.palfinder.profile
import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.PrettyDate
import java.io.Serializable

/**
 * A class to hold the data for a user to be displayed on the profile activity
 * Username as unique identifier
 */

data class ProfileUser(
    val username: String,
    val name: String,
    val surname: String,
    val joinDate: Calendar
) : Serializable {

    companion object{
        const val JOIN_FORMAT = "Joined %s"
    }

    fun fullName(): String {
        return "$name $surname"
    }

    fun atUsername(): String {
        return "@$username"
    }

    fun prettyJoinTime(): String {
        val prettyDate = PrettyDate()
        return String.format(JOIN_FORMAT, prettyDate.timeDiff(joinDate))
    }
}
