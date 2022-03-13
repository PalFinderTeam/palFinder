package com.github.palFinderTeam.palfinder.meetups

import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


/**
 * Object containing methods to query the database about MeetUps.
 */
object FirebaseMeetUpService : MeetUpRepository {

    private const val MEETUP_COLL = "meetups"

    override suspend fun getMeetUpData(meetUpId: String): MeetUp? {
        val db = FirebaseFirestore.getInstance()

        return try {
            db.collection(MEETUP_COLL)
                .document(meetUpId).get().await().toMeetUp()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createMeetUp(newMeetUp: MeetUp): String? {
        val db = FirebaseFirestore.getInstance()

        return try {
            db.collection(MEETUP_COLL).add(newMeetUp.toFirestoreData()).await().id
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun editMeetUp(meetUpId: String, field: String, value: Any): String? {
        val db = FirebaseFirestore.getInstance()

        return try {
            db.collection(MEETUP_COLL).document(meetUpId).update(field, value)
            meetUpId
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun editMeetUp(meetUpId: String, meetUp: MeetUp): String? {
        val db = FirebaseFirestore.getInstance()

        return try {
            db.collection(MEETUP_COLL).document(meetUpId).update(meetUp.toFirestoreData())
            meetUpId
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getMeetUpsAroundLocation(location: Location, radius: Double): List<MeetUp>? {
        val db = FirebaseFirestore.getInstance()

        val geoLocation = GeoLocation(location.latitude, location.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(geoLocation, radius)

        val tasks = bounds.map {
            db.collection(MEETUP_COLL)
                .orderBy(it.startHash)
                .endAt(it.endHash)
                .get()
        }

        val matchingDocs = mutableListOf<MeetUp>()

        Tasks.whenAllComplete(tasks).addOnSuccessListener {

            for (task in tasks) {
                val snap = task.result
                for (doc in snap.documents) {

                    val docLocation: GeoLocation = doc.get("position") as GeoLocation
                    val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, geoLocation)
                    if (distanceInM <= radius) {
                        matchingDocs.add(doc.toMeetUp()!!)
                    }
                }
            }
        }



        return matchingDocs
    }
}