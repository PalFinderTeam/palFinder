package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.END_DATE
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.GEOHASH
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.PARTICIPANTS
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.meetups.activities.ShowParam
import com.github.palFinderTeam.palfinder.profile.FirebaseProfileService.Companion.PROFILE_COLL
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.JOINED_MEETUPS_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.toProfileUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.Response.*
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/**
 * Object containing methods to query the database about MeetUps.
 */
open class FirebaseMeetUpService @Inject constructor(
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

    override suspend fun getMeetUpsData(meetUpIds: List<String>): List<MeetUp>? {
        return try {
            // Firebase don't support more than 10 ids in query.
            val chunked = meetUpIds.chunked(10)
            val queries = chunked.map {
                db.collection(MEETUP_COLL).whereIn(FieldPath.documentId(), it).get()
            }
            val result = Tasks.whenAllSuccess<QuerySnapshot>(queries).await()
            return result.flatMap { it.documents.mapNotNull { it.toMeetUp() } }
        } catch (e: Exception) {
            Log.e("FirebaseMeetUp", e.message.orEmpty())
            null
        }
    }

    override suspend fun createMeetUp(newMeetUp: MeetUp): String? {
        return try {
            val id = db.collection(MEETUP_COLL).add(newMeetUp.toFirestoreData()).await().id
            db.collection(PROFILE_COLL).document(newMeetUp.creatorId)
                .update(JOINED_MEETUPS_KEY, FieldValue.arrayUnion(id)).await()
            id
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
        radiusInKm: Double,
        currentDate: Calendar?,
        showParam: ShowParam?,
        profile: ProfileUser?
    ): Flow<Response<List<MeetUp>>> {
        val geoLocation = GeoLocation(location.latitude, location.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(geoLocation, radiusInKm * 1000.0)
        //set bounds depending on the location
        val tasks = bounds.map {
            db.collection(MEETUP_COLL)
                .orderBy(GEOHASH)
                .startAt(it.startHash)
                .endAt(it.endHash)
        }
        var remaining = tasks.size

        return callbackFlow {
            trySend(Loading())

            val result = mutableSetOf<MeetUp>()

            val listeners = tasks.map { query ->
                query.addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Failure(error.message.orEmpty()))
                        cancel(
                            message = "Error fetching meetups",
                            cause = error
                        )
                        return@addSnapshotListener
                    }
                    //filter fetched meetups by location and showParam
                    var meetups = value?.documents
                        ?.mapNotNull { it.toMeetUp() }
                        ?.filter {
                            // Filter the last false positive
                            val docLocation = it.location
                            val distanceInKm =
                                docLocation.distanceInKm(location)
                            distanceInKm <= radiusInKm && additionalFilter(profile, it, showParam)
                        }
                    // Cannot combine queries, so perform things locally instead.
                    if (currentDate != null) {
                        meetups = meetups?.filter { !it.isFinished(currentDate) }
                    }

                    val deletedMeetups = value?.documentChanges
                        ?.filter {
                            it.type == DocumentChange.Type.REMOVED
                        }
                        ?.mapNotNull { it.document.toMeetUp() }
                    if (meetups != null) {
                        // They run on the main thread so this is thread safe
                        result.addAll(meetups)
                        deletedMeetups?.let {
                            result.removeAll(it)
                        }

                        if (remaining > 0) {
                            remaining -= 1
                        }
                        // We first wait that every task terminate once before sending, after
                        // that every task should always update the list when new meetups appear.
                        // They can't disappear btw (but we could add it).
                        if (remaining == 0) {
                            trySend(Success(result.toList()))
                        }
                    }
                }

            }
            awaitClose {
                listeners.forEach {
                    it.remove()
                }
            }
        }
    }

    override suspend fun joinMeetUp(
        meetUpId: String,
        userId: String,
        now: Calendar,
        profile: ProfileUser
    ): Response<Unit> {
        return try {
            val meetUp = getMeetUpData(meetUpId) ?: return Failure("Could not find meetup.")
            if (meetUp.isParticipating(userId)) {
                return Success(Unit)
            }

            if (!meetUp.canJoin(now, profile)) {
                return Failure("Cannot join meetup now, either it is full, out of time or you do not meet the requirements")
            }
            if (meetUp.isFull()) {
                return Failure("Cannot join, it is full.")
            }

            val batch = db.batch()
            batch.update(
                db.collection(MEETUP_COLL).document(meetUpId),
                PARTICIPANTS,
                FieldValue.arrayUnion(userId)
            )
            batch.update(
                db.collection(PROFILE_COLL).document(userId),
                JOINED_MEETUPS_KEY, FieldValue.arrayUnion(meetUpId)
            )
            batch.commit().await()
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
            if (meetUp.creatorId == userId) {
                return Failure("Cannot leave your own meetup.")
            }

            val batch = db.batch()
            batch.update(
                db.collection(MEETUP_COLL).document(meetUpId),
                PARTICIPANTS,
                FieldValue.arrayRemove(userId)
            )
            batch.update(
                db.collection(PROFILE_COLL).document(userId),
                JOINED_MEETUPS_KEY, FieldValue.arrayRemove(meetUpId)
            )
            batch.commit().await()
            Success(Unit)
        } catch (e: Exception) {
            Failure(e.message.orEmpty())
        }
    }

    @ExperimentalCoroutinesApi
    override fun getAllMeetUps(currentDate: Calendar?): Flow<List<MeetUp>> {
        var query: Query = db.collection(MEETUP_COLL)
        if (currentDate != null) {
            query = query.whereGreaterThan(END_DATE, currentDate.time)
        }

        return callbackFlow {
            val listenerRegistration = query
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

    override fun getUserMeetups(
        userId: String,
        currentDate: Calendar?
    ): Flow<Response<List<MeetUp>>> {
        val query = db.collection(PROFILE_COLL).document(userId)

        return flow {
            emit(Loading())
            val profile = query.get().await().toProfileUser()
            if (profile == null) {
                emit(Failure("Could not fetch profile."))
            } else {
                val meetUps = getMeetUpsData(profile.joinedMeetUps)
                if (meetUps == null) {
                    emit(Failure("Could not fetch meetups."))
                } else {
                    if (currentDate != null) {
                        val filtered = meetUps.filter { !it.isFinished(currentDate) }
                        emit(Success(filtered))
                    } else {
                        emit(Success(meetUps))
                    }
                }
            }
        }
    }

    companion object {
        const val MEETUP_COLL = "meetups"
    }
}