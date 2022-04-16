package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.ProfileViewModel
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.CriterionGender
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Location.Companion.toLocation
import com.github.palFinderTeam.palfinder.utils.isBefore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint

/**
 * @param uuid: Unique Identifier of the meetup
 * @param creatorId: Creator of the meetup
 * @param iconId: Path to the Icon
 * @param name: Name of the Meetup
 * @param description: Description of the meetup
 * @param startDate: Date & Time of the begin of the meetup
 * @param endDate: Date & Time of the end of the meetup
 * @param location: Location of the meetup
 * @param tags: Tags
 * @param hasMaxCapacity: Indicate if there is a limit to the number of people who can join
 * @param capacity: Limit number of people who can join. Not use if hasMaxCapacity = false
 * @param participantsId: List of participants
 */
data class MeetUp(
    val uuid: String,
    val creatorId: String,
    val iconId: String?,
    val name: String,
    val description: String,
    val startDate: Calendar,
    val endDate: Calendar,
    val location: Location,
    val tags: Set<Category>,
    val hasMaxCapacity: Boolean,
    val capacity: Int,
    val participantsId: List<String>,
    val criterionAge: Pair<Int?, Int?>?,
    val criterionGender: CriterionGender?,
) : java.io.Serializable {

    /**
     * @param currentLocation
     * @return distance from [currentLocation] to the event in km
     */
    fun distanceInKm(currentLocation: Location): Double {
        return location.distanceInKm(currentLocation)
    }

    /**
     * @return the number of participants currently in the meetup
     */
    fun numberOfParticipants() : Int {
        return participantsId.size
    }

    /**
     * @return if the event has reach its participant limit
     */
    fun isFull(): Boolean {
        return hasMaxCapacity && capacity <= participantsId.size
    }

    /**
     * @param now: current date
     * @return if a user can join
     */
    fun canJoin(now: Calendar, profile: ProfileUser): Boolean {
        return !isFull() && !isFinished(now) && criterionFulfilled(profile)
    }

    private fun criterionFulfilled(profile: ProfileUser): Boolean {
        return genderFulfilled(profile) && ageFulfilled(profile.getAge())
    }

    private fun ageFulfilled(age: Int): Boolean {
        if (criterionAge == Pair(null, null) || criterionAge == null) {
            return true
        }
        return (criterionAge!!.first!! <= age && criterionAge.second!! >= age)
    }
    private fun genderFulfilled(profile: ProfileUser): Boolean {
        return when (criterionGender) {
            CriterionGender.ALL -> true
            CriterionGender.FEMALE -> profile.gender == Gender.FEMALE
            CriterionGender.MALE -> profile.gender == Gender.MALE
            else -> true
        }
    }

    /**
     * @param now: current date
     * @return if the event is finished
     */
    fun isFinished(now: Calendar): Boolean {
        return endDate.isBefore(now)
    }

    /**
     * @param now: current date
     * @return if the meetup has started
     */
    fun isStarted(now: Calendar): Boolean {
        return startDate.isBefore(now)
    }

    /**
     *  @return if the user is taking part in the event
     */
    fun isParticipating(userId: String?): Boolean {
        return participantsId.contains(userId)
    }

    /**
     * @return a representation which is Firestore friendly of the MeetUp
     */
    fun toFirestoreData(): HashMap<String, Any?> {
        return hashMapOf(
            "capacity" to capacity,
            "creator" to creatorId,
            "description" to description,
            "startDate" to startDate.time,
            "endDate" to endDate.time,
            "hasMaxCapacity" to hasMaxCapacity,
            "capacity" to capacity.toLong(),
            "icon" to iconId,
            "location" to GeoPoint(location.latitude, location.longitude),
            "geohash" to GeoFireUtils.getGeoHashForLocation(
                GeoLocation(
                    location.latitude,
                    location.longitude
                )
            ),
            "name" to name,
            "participants" to participantsId.toList(),
            "tags" to tags.map { it.toString() },
            "criterionAgeFirst" to criterionAge?.first?.toLong(),
            "criterionAgeSecond" to criterionAge?.second?.toLong(),
            "criterionGender" to criterionGender?.genderName,
        )
    }

    companion object {

        /**
         * Provide a way to convert a Firestore query result, in a MeetUp
         */
        fun DocumentSnapshot.toMeetUp(): MeetUp? {
            try {
                val uuid = id
                val iconId = getString("icon")
                val creator = getString("creator")!!
                val capacity = getLong("capacity")!!
                val description = getString("description")!!
                val startDate = getDate("startDate")!!
                val endDate = getDate("endDate")!!
                val geoPoint = getGeoPoint("location")!!
                val name = getString("name")!!
                val tags = get("tags")!! as List<String>
                val hasMaxCapacity = getBoolean("hasMaxCapacity")!!
                val participantsId = get("participants")!! as List<String>
                // Convert Date to calendar
                val startDateCal = Calendar.getInstance()
                val endDateCal = Calendar.getInstance()
                startDateCal.time = startDate
                endDateCal.time = endDate
                var criterionGender = CriterionGender.from(getString("criterionGender"))
                val criterionAge = Pair(getLong("criterionAgeFirst")?.toInt(), getLong("criterionAgeSecond")?.toInt())
                return MeetUp(
                    uuid,
                    creator,
                    iconId,
                    name,
                    description,
                    startDateCal,
                    endDateCal,
                    geoPoint.toLocation(),
                    tags.map { Category.valueOf(it) }.toSet(),
                    hasMaxCapacity,
                    capacity.toInt(),
                    participantsId,
                    criterionAge,
                    criterionGender
                )
            } catch (e: Exception) {
                Log.e("Meetup", "Error deserializing meetup", e)
                return null
            }
        }
    }
}