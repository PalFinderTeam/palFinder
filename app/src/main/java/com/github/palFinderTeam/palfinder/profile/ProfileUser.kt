package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import android.util.Log
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.PrivacySettings
import com.github.palFinderTeam.palfinder.utils.time.PrettyDate
import com.github.palFinderTeam.palfinder.utils.generics.FirebaseObject
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable
import java.time.LocalDate
import java.time.Period

const val USER_ID = "com.github.palFinderTeam.palFinder.USER_ID"

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
    val gender: Gender? = Gender.NON_SPEC,
    val following: List<String> = emptyList(),
    val followed: List<String> = emptyList(),
    val blockedUsers: List<String> = emptyList(),
    private val achievements: List<String> = emptyList(),
    val privacySettings: PrivacySettings? = PrivacySettings.PUBLIC,
) : Serializable, FirebaseObject {

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
        const val FOLLOWING_PROFILES = "following"
        const val FOLLOWED_BY = "followed"
        const val BLOCKED_USERS = "blocked_users"
        const val ACHIEVEMENTS_OBTAINED = "achievements"
        const val PRIVACY_SETTINGS_KEY = "privacy_settings"

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
                val following = (get(FOLLOWING_PROFILES) as? List<String>).orEmpty()
                val followed = (get(FOLLOWED_BY) as? List<String>).orEmpty()
                val blockedUsers = (get(BLOCKED_USERS) as? List<String>).orEmpty()
                val achievements = (get(ACHIEVEMENTS_OBTAINED) as? List<String>).orEmpty()

                val privacySettings = PrivacySettings.from(getString(PRIVACY_SETTINGS_KEY))


                ProfileUser(
                    uuid,
                    username,
                    name,
                    surname,
                    joinDateCal,
                    ImageInstance(picture),
                    description,
                    birthdayCal,
                    joinedMeetUp,
                    gender,
                    following,
                    followed,
                    blockedUsers,
                    achievements,
                    privacySettings
                )

            } catch (e: Exception) {
                Log.e("ProfileUser", "Error deserializing user", e)
                null
            }
        }
    }

    /**
     * @return a representation which is Firestore friendly of the UserProfile.
     */
    override fun toFirestoreData(): HashMap<String, Any?> {
        return hashMapOf(
            NAME_KEY to name,
            SURNAME_KEY to surname,
            USERNAME_KEY to username,
            JOIN_DATE_KEY to joinDate.time,
            PICTURE_KEY to pfp.imgURL,
            DESCRIPTION_KEY to description,
            BIRTHDAY_KEY to birthday?.time,
            JOINED_MEETUPS_KEY to joinedMeetUps,
            GENDER to gender?.stringGender,
            FOLLOWING_PROFILES to following,
            FOLLOWED_BY to followed,
            BLOCKED_USERS to blockedUsers,
            ACHIEVEMENTS_OBTAINED to achievements,
            PRIVACY_SETTINGS_KEY to privacySettings
        )
    }

    override fun getUUID(): String = uuid

    fun fullName(): String {
        return "$name $surname"
    }

    fun getAge(): Int {
        return if (birthday == null) {
            -1
        } else {
            Period.between(
                LocalDate.of(
                    birthday.get(Calendar.YEAR),
                    birthday.get(Calendar.MONTH),
                    birthday.get(Calendar.DAY_OF_MONTH)
                ),
                LocalDate.now()
            ).years
        }
    }

    fun atUsername(): String {
        return "@$username"
    }

    fun canFollow(profileId: String): Boolean {
        return profileId != uuid && !following.contains(profileId)
    }

    fun canUnFollow(profileId: String): Boolean {
        return following.contains(profileId)
    }

    fun canProfileBeSeenBy(profileId: String): Boolean{
        return when (privacySettings) {
            PrivacySettings.PRIVATE -> false
            PrivacySettings.PUBLIC -> true
            PrivacySettings.FRIENDS -> followed.contains(profileId)
            else -> false
        }
    }

    fun prettyJoinTime(): String {
        val prettyDate = PrettyDate()
        return String.format(JOIN_FORMAT, prettyDate.timeDiff(joinDate))
    }

    fun achievements(): List<Achievement> {
        return achievements.map { Achievement.from(it) }
    }

    fun canBlock(uuid: String): Boolean {
        return !blockedUsers.contains(uuid)
    }

    fun badges(): List<Achievement> {
        return achievements().filter { it.cat == AchievementCategory.OTHER }
    }
}
