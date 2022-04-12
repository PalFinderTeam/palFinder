package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Location.Companion.toLocation
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.isBefore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint

/**
 * @param uuid: Unique Identifier of the meetup
 * @param creatorId: Creator of the meetup
 * @param iconImage: The meetup's image (can be null)
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
    fun canJoin(now: Calendar): Boolean {
        return !isFull() && !isFinished(now)
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
            "icon" to iconImage?.imgURL,
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
        )
    }

    companion object {

        /**
         * Provide a way to convert a Firestore query result, in a MeetUp
         */
        fun DocumentSnapshot.toMeetUp(): MeetUp? {
            try {
                val uuid = id
                val iconUrl = getString("icon")
                val iconImage = if(iconUrl == null) { null } else { ImageInstance(iconUrl) } // Now this field can be null, because meetups with no image made it crash
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
                    participantsId
                )
            } catch (e: Exception) {
                Log.e("Meetup", "Error deserializing meetup", e)
                return null
            }
        }
    }
}