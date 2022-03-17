package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.isBefore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint

/**
 * @param uuid: Unique Identifier of the meetup
 * @param creator: Creator of the meetup
 * @param icon: Path to the Icon
 * @param name: Name of the Meetup
 * @param description: Description of the meetup
 * @param startDate: Date & Time of the begin of the meetup
 * @param endDate: Date & Time of the end of the meetup
 * @param location: Location of the meetup
 * @param tags: Tags
 * @param hasMaxCapacity: Indicate if there is a limit to the number of people who can join
 * @param capacity: Limit number of people who can join. Not use if hasMaxCapacity = false
 * @param participants: List of participants
 */
data class MeetUp(
    val uuid: String,
    val creator: ProfileUser,
    val icon: String,
    val name: String,
    val description: String,
    val startDate: Calendar,
    val endDate: Calendar,
    val location: Location,
    val tags: Set<Category>,
    val hasMaxCapacity: Boolean,
    val capacity: Int,
    val participants: MutableList<ProfileUser>, // TODO -  Change to Real User
) : java.io.Serializable {

    /**
     * @param currentLocation
     * @return distance from [currentLocation] to the event in km
     */
    fun distanceInKm(currentLocation: Location): Double {
        return location.distanceInKm(currentLocation)
    }

    /**
     * @return if the event has reach its participant limit
     */
    fun isFull(): Boolean {
        return hasMaxCapacity && capacity <= participants.size
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
     *  Add [user] to the Event if [now] is a valid date to join
     *  @param now: current date
     */
    fun join(now: Calendar, user: ProfileUser) {
        if (canJoin(now) && !isParticipating(user)) {
            participants.add(user)
        }
    }

    /**
     *  Remove [user] from the event
     *  if user is not in the event, does nothing
     */
    fun leave(user: ProfileUser) {
        if (isParticipating(user)) {
            participants.remove(user)
        }
    }

    /**
     *  @return if the user is taking part in the event
     */
    fun isParticipating(user: ProfileUser): Boolean {
        return participants.contains(user)
    }

    /**
     * @return a representation which is Firestore friendly of the MeetUp
     */
    fun toFirestoreData(): HashMap<String, Any> {
        return hashMapOf(
            "capacity" to capacity,
            "creator" to creator.name,
            "description" to description,
            "startDate" to startDate.time,
            "endDate" to endDate.time,
            "hasMaxCapacity" to hasMaxCapacity,
            "capacity" to capacity.toLong(),
            "icon" to icon,
            "location" to GeoPoint(location.latitude, location.longitude),
            "geohash" to GeoFireUtils.getGeoHashForLocation(
                GeoLocation(
                    location.latitude,
                    location.longitude
                )
            ),
            "name" to name,
            "participants" to participants.map { it.name }.toList(),
            "tags" to tags.map { it.toString() },
        )
    }

    /**
     * Provide a way to convert a Firestore query result, in a MeetUp
     */
    companion object {

        fun DocumentSnapshot.toMeetUp(): MeetUp? {
            try {
                val uuid = id
                // TODO Make it fetch other document
                val creator = ProfileUser("michel", "whatever", "miche", Calendar.getInstance(), ImageInstance("icons/demo_pfp.jpeg"))
                val capacity = getLong("capacity")!!
                val description = getString("description")!!
                val startDate = getDate("startDate")!!
                val endDate = getDate("endDate")!!
                val location = getGeoPoint("location")!!
                val name = getString("name")!!
                // TODO find something
                // val participants = getField<List<Any>>("participants")!!
                val tags = get("tags")!! as List<String>
                val hasMaxCapacity = getBoolean("hasMaxCapacity")!!

                // Convert Date to calendar
                val startDateCal = Calendar.getInstance()
                val endDateCal = Calendar.getInstance()
                startDateCal.time = startDate
                endDateCal.time = endDate

                return MeetUp(
                    id,
                    creator,
                    "wathevericon",
                    name,
                    description,
                    startDateCal,
                    endDateCal,
                    Location(location.longitude, location.latitude),
                    tags.map { Category.valueOf(it) }.toSet(),
                    hasMaxCapacity,
                    capacity.toInt(),
                    mutableListOf()
                )
            } catch (e: Exception) {
                Log.e("Meetup", "Error deserializing meetup", e)
                return null
            }
        }
    }
}