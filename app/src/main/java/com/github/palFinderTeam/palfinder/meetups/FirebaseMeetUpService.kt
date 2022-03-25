package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.Response.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/**
 * Object containing methods to query the database about MeetUps.
 */
class FirebaseMeetUpService @Inject constructor(
    private val db: FirebaseFirestore
) : MeetUpRepository {


    override suspend fun getMeetUpData(meetUpId: String): MeetUp? {
        return try {
            db.collection(MEETUP_COLL)
                .document(meetUpId).get().await().toMeetUp()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createMeetUp(newMeetUp: MeetUp): String? {
        return try {
            db.collection(MEETUP_COLL).add(newMeetUp.toFirestoreData()).await().id
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun editMeetUp(meetUpId: String, field: String, value: Any): String? {
        return try {
            if (!db.collection(MEETUP_COLL).document(meetUpId).get()
                    .await().data!!.contains(field)
            ) {
                return null
            }
            db.collection(MEETUP_COLL).document(meetUpId).update(field, value).await()
            meetUpId
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun editMeetUp(meetUpId: String, meetUp: MeetUp): String? {
        return try {
            db.collection(MEETUP_COLL).document(meetUpId).update(meetUp.toFirestoreData()).await()
            meetUpId
        } catch (e: Exception) {
            null
        }
    }

    override fun getMeetUpsAroundLocation(
        location: Location,
        radiusInM: Double
    ): Flow<Response<List<MeetUp>>> {

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
            emit(Failure(error.message.orEmpty()))
        }
    }

    override suspend fun joinMeetUp(
        meetUpId: String,
        userId: String,
        now: Calendar
    ): Response<Unit> {
        return try {
            val meetUp = getMeetUpData(meetUpId) ?: return Failure("Could not find meetup.")
            if (meetUp.isParticipating(userId)) {
                return Success(Unit)
            }
            if (!meetUp.canJoin(now)) {
                return Failure("Cannot join meetup now.")
            }
            if (meetUp.isFull()) {
                return Failure("Cannot join, it is full.")
            }

            db.collection(MEETUP_COLL).document(meetUpId)
                .update("participants", FieldValue.arrayUnion(userId)).await()
            Success(Unit)
        } catch (e: Exception) {
            Failure(e.message.orEmpty())
        }
    }

    override suspend fun leaveMeetUp(meetUpId: String, userId: String): Response<Unit> {
        return try {
            val meetUp = getMeetUpData(meetUpId) ?: return Failure("Could not find meetup.")
            if (!meetUp.isParticipating(userId)) {
                return Failure("Cannot leave a meetup which was not joined before")
            }
            db.collection(MEETUP_COLL).document(meetUpId)
                .update("participants", FieldValue.arrayRemove(userId)).await()
            Success(Unit)
        } catch (e: Exception) {
            Failure(e.message.orEmpty())
        }
    }

    @ExperimentalCoroutinesApi
    override fun getAllMeetUps(): Flow<List<MeetUp>> {
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

    @ExperimentalCoroutinesApi
    override fun getAllMeetUpsResponse(): Flow<Response<List<MeetUp>>> {
        return getAllMeetUps().map {
            Success(it)
        }
    }

    companion object {
        const val MEETUP_COLL = "meetups"
    }
}