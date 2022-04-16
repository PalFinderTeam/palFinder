package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.PrettyDate
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.getField
import java.io.Serializable
import java.time.LocalDate
import java.time.Period
import java.time.Year
import java.util.*
import kotlin.collections.HashMap

/**
 * A class to hold the data for a user to be displayed on the profile activity
 * Username as unique identifier
 */
data class ProfileUser(
    val uuid: String,
    val username: String,
    val name: String,
    val surname: String,
    val joinDate: Calendar,
    val pfp: ImageInstance,
    val description: String = "",
    val birthday: Calendar? = null,
    val joinedMeetUps: List<String> = emptyList(),
    val gender: Gender? = Gender.NON_SPEC
) : Serializable {

    companion object {
        const val JOIN_FORMAT = "Joined %s"
        const val USERNAME_KEY = "username"
        const val NAME_KEY = "name"
        const val SURNAME_KEY = "surname"
        const val PICTURE_KEY = "picture"
        const val JOIN_DATE_KEY = "join_date"
        const val DESCRIPTION_KEY = "description"
        const val BIRTHDAY_KEY = "birthday"
        const val JOINED_MEETUPS_KEY = "joined_meetups"
        const val GENDER = "gender"

        /**
         * Provide a way to convert a Firestore query result, in a ProfileUser.
         */
        fun DocumentSnapshot.toProfileUser(): ProfileUser? {
            return try {
                val uuid = id
                val username = getString(USERNAME_KEY)!!
                val name = getString(NAME_KEY)!!
                val surname = getString(SURNAME_KEY)!!
                val picture = getString(PICTURE_KEY)!!
                val joinDate = getDate(JOIN_DATE_KEY)!!

                val joinDateCal = Calendar.getInstance().apply { time = joinDate }
                val description = getString(DESCRIPTION_KEY).orEmpty()
                val joinedMeetUp = (get(JOINED_MEETUPS_KEY) as? List<String>).orEmpty()

                var birthdayCal: Calendar? = null
                if (getDate(BIRTHDAY_KEY) != null) {
                    birthdayCal = Calendar.getInstance().apply {
                        time = getDate(BIRTHDAY_KEY)
                    }
                }
                var gender: Gender? = null
                if (getString(GENDER) != null) {
                    gender = Gender.from(getString(GENDER))
                }

                ProfileUser(uuid, username, name, surname, joinDateCal, ImageInstance(picture), description, birthdayCal, joinedMeetUp, gender)

            } catch (e: Exception) {
                Log.e("ProfileUser", "Error deserializing user", e)
                null
            }
        }
    }

    /**
     * @return a representation which is Firestore friendly of the UserProfile.
     */
    fun toFirestoreData(): HashMap<String, Any?> {
        //if
        return hashMapOf(
            NAME_KEY to name,
            SURNAME_KEY to surname,
            USERNAME_KEY to username,
            JOIN_DATE_KEY to joinDate.time,
            PICTURE_KEY to pfp.imgURL,
            DESCRIPTION_KEY to description,
            BIRTHDAY_KEY to birthday?.time,
            JOINED_MEETUPS_KEY to joinedMeetUps,
            GENDER to gender?.stringGender
        )
    }

    fun fullName(): String {
        return "$name $surname"
    }

    fun getAge(): Int {
        return if (birthday == null) {
            1
        } else {
            Period.between(
                LocalDate.of(birthday.get(Calendar.YEAR), birthday.get(Calendar.MONTH), birthday.get(Calendar.DAY_OF_MONTH)),
                LocalDate.now()
            ).years
        }
    }

    fun atUsername(): String {
        return "@$username"
    }

    fun prettyJoinTime(): String {
        val prettyDate = PrettyDate()
        return String.format(JOIN_FORMAT, prettyDate.timeDiff(joinDate))
    }
}
