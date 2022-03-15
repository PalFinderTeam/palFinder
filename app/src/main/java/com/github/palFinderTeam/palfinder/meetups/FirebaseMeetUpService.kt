package com.github.palFinderTeam.palfinder.meetups

import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    override suspend fun getMeetUpsAroundLocation(
        location: Location,
        radiusInM: Double
    ): List<MeetUp>? {
        val db = FirebaseFirestore.getInstance()

        val geoLocation = GeoLocation(location.latitude, location.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(geoLocation, radiusInM)

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
                    if (distanceInM <= radiusInM) {
                        matchingDocs.add(doc.toMeetUp()!!)
                    }
                }
            }
        }



        return matchingDocs
    }

    /**
     * This function fetches all MeetUps from DB
     * It will be removed later, it just is better for experimentation, while fetchingAround
     * location is being build.
     */
    @ExperimentalCoroutinesApi
    override fun getAllMeetUps(): Flow<List<MeetUp>> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(MEETUP_COLL)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(
                            message = "Error fetching meetups",
                            cause = firebaseFirestoreException
                        )
                        return@addSnapshotListener
                    }
                    val map = querySnapshot?.documents
                        ?.mapNotNull { it.toMeetUp() }
                    if (map != null) {
                        trySend(map)
                    }
                }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }
}