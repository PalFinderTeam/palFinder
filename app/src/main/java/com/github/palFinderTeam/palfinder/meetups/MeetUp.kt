package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.meetups.fragments.CriterionsFragment.Companion.MIN_AGE
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.*
import com.github.palFinderTeam.palfinder.utils.Location.Companion.toLocation
import com.github.palFinderTeam.palfinder.utils.generics.FirebaseObject
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint

/**
 * @param uuid: Unique Identifier of the meetup
 * @param creatorId: Creator of the meetup
 * @param iconImage: The meetup's image (can be null)
 * @param markerId: id of the marker icon
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
    val iconImage: ImageInstance?,
    val name: String,
    val description: String,
    val startDate: Calendar,
    val endDate: Calendar,
    val location: Location,
    val tags: Set<Category>,
    val hasMaxCapacity: Boolean,
    val capacity: Int,
    val participantsId: List<String>,
    val criterionAge: Pair<Int?, Int?>? = null,
    val criterionGender: CriterionGender? = null,
    val markerId: Int? = null,
) : java.io.Serializable, FirebaseObject {

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
    fun numberOfParticipants(): Int {
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
        return (!isFull() && !isFinished(now) && criterionFulfilled(profile))
    }

    private fun criterionFulfilled(profile: ProfileUser): Boolean {
        return genderFulfilled(profile) && ageFulfilled(profile.getAge())
    }

    private fun ageFulfilled(age: Int): Boolean {
        if (criterionAge == Pair(null, null) || criterionAge == null || criterionAge == Pair(MIN_AGE, Int.MAX_VALUE)) {
            return true
        }
        return (criterionAge.first!! <= age && criterionAge.second!! >= age)
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

    override fun getUUID(): String = uuid

    /**
     * @return a representation which is Firestore friendly of the MeetUp
     */
    override fun toFirestoreData(): HashMap<String, Any?> {
        return hashMapOf(
            CREATOR to creatorId,
            DESCRIPTION to description,
            START_DATE to startDate.time,
            END_DATE to endDate.time,
            HAS_CAPACITY to hasMaxCapacity,
            CAPACITY to capacity.toLong(),
            ICON to iconImage?.imgURL,
            MARKER to markerId,
            LOCATION to GeoPoint(location.latitude, location.longitude),
            GEOHASH to GeoFireUtils.getGeoHashForLocation(
                GeoLocation(
                    location.latitude,
                    location.longitude
                )
            ),
            NAME to name,
            PARTICIPANTS to participantsId.toList(),
            TAGS to tags.map { it.toString() },
            CRITERION_AGE_FIRST to criterionAge?.first?.toLong(),
            CRITERION_AGE_SECOND to criterionAge?.second?.toLong(),
            CRITERION_GENDER to criterionGender?.genderName,
        )
    }

    companion object {

        const val CREATOR = "creator"
        const val DESCRIPTION = "description"
        const val START_DATE = "startDate"
        const val END_DATE = "endDate"
        const val HAS_CAPACITY = "hasMaxCapacity"
        const val CAPACITY = "capacity"
        const val ICON = "icon"
        const val MARKER = "marker"
        const val LOCATION = "location"
        const val GEOHASH = "geohash"
        const val NAME = "name"
        const val PARTICIPANTS = "participants"
        const val TAGS = "tags"
        const val CRITERION_AGE_FIRST = "criterionAgeFirst"
        const val CRITERION_AGE_SECOND = "criterionAgeSecond"
        const val CRITERION_GENDER = "criterionGender"

        /**
         * Provide a way to convert a Firestore query result, in a MeetUp
         */
        fun DocumentSnapshot.toMeetUp(): MeetUp? {
            try {
                val uuid = id
                val iconUrl = getString(ICON)
                val markerId = getLong(MARKER)
                val iconImage =
                    if (iconUrl == null) null else ImageInstance(iconUrl) // Now this field can be null, because meetups with no image made it crash
                val creator = getString(CREATOR)!!
                val capacity = getLong(CAPACITY)!!
                val description = getString(DESCRIPTION)!!
                val startDate = getDate(START_DATE)!!
                val endDate = getDate(END_DATE)!!
                val geoPoint = getGeoPoint(LOCATION)!!
                val name = getString(NAME)!!
                val tags = get(TAGS)!! as List<String>
                val hasMaxCapacity = getBoolean(HAS_CAPACITY)!!
                val participantsId = get(PARTICIPANTS)!! as List<String>
                // Convert Date to calendar
                val startDateCal = Calendar.getInstance()
                val endDateCal = Calendar.getInstance()
                startDateCal.time = startDate
                endDateCal.time = endDate
                val criterionGender = CriterionGender.from(getString(CRITERION_GENDER))
                val criterionAge = Pair(
                    getLong(CRITERION_AGE_FIRST)?.toInt(), getLong(
                        CRITERION_AGE_SECOND
                    )?.toInt()
                )
                return MeetUp(
                    uuid,
                    creator,
                    iconImage,
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
                    criterionGender,
                    markerId?.toInt()
                )
            } catch (e: Exception) {
                Log.e("Meetup", "Error deserializing meetup", e)
                return null
            }
        }

    }
}
