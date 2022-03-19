package com.github.palFinderTeam.palfinder.meetups

import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.Response.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
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
            db.collection(MEETUP_COLL).document(meetUpId).update(field, value).await()
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

    override fun getMeetUpsAroundLocation(
        location: Location,
        radiusInM: Double
    ): Flow<Response<List<MeetUp>>> {

        val db = FirebaseFirestore.getInstance()

        val geoLocation = GeoLocation(location.latitude, location.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(geoLocation, radiusInM)
        val tasks = bounds.map {
            db.collection(MEETUP_COLL)
                .orderBy(it.startHash)
                .endAt(it.endHash)
                .get()
        }

        return flow {
            emit(Loading())

            val allTasks: Task<List<QuerySnapshot>> = Tasks.whenAllSuccess(tasks)

            val meetUps = allTasks.await().flatMap { snapshot ->
                snapshot.documents.map { it.toMeetUp()!! }.filter {
                    // Filter the last false positive
                    val docLocation = it.location
                    val distanceInM =
                        docLocation.distanceInKm(location) * 1000 // TODO find better unit conversion
                    distanceInM <= radiusInM
                }
            }
            emit(Success(meetUps))

        }.catch { error ->
            error.message?.let {
                emit(Failure(it))
            }
        }
    }

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