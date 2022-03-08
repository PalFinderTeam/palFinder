package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.utils.PrettyDate
import java.util.*
import java.io.Serializable

/**
 * A class to hold the data for a user to be displayed on the profile activity
 * Username as unique identifier
 */

data class ProfileUser(
    private val username: String,
    private val name: String,
    private val surname: String,
    private val joinDate: Date
) : Serializable {

    fun getName(): String {
        return name
    }

    fun getSurname(): String {
        return surname
    }

    fun getFullName(): String {
        return "$name $surname"
    }

    fun getUsername(): String {
        return username
    }

    fun getAtUsername(): String {
        return "@$username"
    }

    fun getJoinTime(): Date {
        return joinDate
    }

    fun getPrettyJoinTime(): String {
        val prettyDate = PrettyDate()
        return "Joined " + prettyDate.timeSince(joinDate) + " ago"
    }
}