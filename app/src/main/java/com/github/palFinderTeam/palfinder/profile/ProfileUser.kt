package com.github.palFinderTeam.palfinder.profile
import com.github.palFinderTeam.palfinder.utils.PrettyDate
import java.util.*
import java.io.Serializable

/**
 * A class to hold the data for a user to be displayed on the profile activity
 * Username as unique identifier
 */

data class ProfileUser(
    val username: String,
    val name: String,
    val surname: String,
    val joinDate: Date
) : Serializable {

    fun fullName(): String {
        return "$name $surname"
    }

    fun atUsername(): String {
        return "@$username"
    }

    fun prettyJoinTime(): String {
        val prettyDate = PrettyDate()
        return "Joined " + prettyDate.timeSince(joinDate) + " ago"
    }
}
