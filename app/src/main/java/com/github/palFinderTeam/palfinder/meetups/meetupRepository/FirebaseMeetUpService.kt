package com.github.palFinderTeam.palfinder.meetups.meetupRepository

import android.icu.util.Calendar
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.END_DATE
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.GEOHASH
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.PARTICIPANTS
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.RANKING_SCORE
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.meetups.ShowParam
import com.github.palFinderTeam.palfinder.profile.services.FirebaseProfileService.Companion.PROFILE_COLL
import com.github.palFinderTeam.palfinder.profile.services.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.JOINED_MEETUPS_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.toProfileUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.Response.*
import com.github.palFinderTeam.palfinder.utils.generics.FirestoreRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/**
 * Object containing methods to query the database about MeetUps.
 */
open class FirebaseMeetUpService @Inject constructor(
    private val db: FirebaseFirestore,
    private val profileService: ProfileService
) : MeetUpRepository {

    private val wrapper = FirestoreRepository(db, MEETUP_COLL, END_DATE) { it.toMeetUp() }

    override suspend fun fetch(uuid: String): MeetUp? = wrapper.fetch(uuid)

    override fun fetchFlow(uuid: String): Flow<Response<MeetUp>> = wrapper.fetchFlow(uuid)

    override suspend fun fetch(uuids: List<String>): List<MeetUp> = wrapper.fetch(uuids)

    override suspend fun edit(uuid: String, field: String, value: Any): String? =
        wrapper.edit(uuid, field, value)

    override suspend fun edit(uuid: String, obj: MeetUp): String? = wrapper.edit(uuid, obj)

    override suspend fun create(obj: MeetUp): String? {
        return try {
            val id = db.collection(MEETUP_COLL).add(obj.toFirestoreData()).await().id
            db.collection(PROFILE_COLL).document(obj.creatorId)
                .update(JOINED_MEETUPS_KEY, FieldValue.arrayUnion(id)).await()
            id
        } catch (e: Exception) {
            null
        }
    }

    override fun fetchAll(currentDate: Calendar?): Flow<List<MeetUp>> =
        wrapper.fetchAll(currentDate)

    override suspend fun exists(uuid: String): Boolean = wrapper.exists(uuid)

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

                    // To avoid having twice the modified one and still have the deleted one
                    val deletedMeetups = value?.documentChanges
                        ?.filter {
                            it.type == DocumentChange.Type.REMOVED || it.type == DocumentChange.Type.MODIFIED
                        }
                        ?.mapNotNull { it.document.toMeetUp() }
                    if (meetups != null) {
                        // They run on the main thread so this is thread safe
                        result.addAll(meetups)
                        deletedMeetups?.let {
                            result.removeAll(it.toSet())
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
            val meetUp = fetch(meetUpId) ?: return Failure("Could not find meetup.")
            if (meetUp.isParticipating(userId)) {
                return Success(Unit)
            }

            if (meetUp.isFull()) {
                return Failure("Cannot join, it is full.")
            }
            if (!meetUp.canJoin(now, profile)) {
                return Failure("Cannot join meetup now, either it is full, out of time or you do not meet the requirements")
            }

            val batch = db.batch()
            batch.update(
                db.collection(MEETUP_COLL).document(meetUpId),
                PARTICIPANTS,
                FieldValue.arrayUnion(userId)
            )
            batch.update(
                db.collection(MEETUP_COLL).document(meetUpId),
                RANKING_SCORE,
                getRankingScore(meetUp.copy(participantsId = meetUp.participantsId.plus(userId)))
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
            val meetUp = fetch(meetUpId) ?: return Failure("Could not find meetup.")
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
                db.collection(MEETUP_COLL).document(meetUpId),
                RANKING_SCORE,
                getRankingScore(meetUp.copy(participantsId = meetUp.participantsId.minus(userId)))
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

    private suspend fun getRankingScore(meetUp: MeetUp): Double {
        val participants = profileService.fetch(meetUp.participantsId)
        return participants!!.sumOf { it.followed.size }.toDouble() / participants.size
    }

    /**
     * Update the ranking score of the meetup (Use in the case it was not set before)
     *
     * @return the new score if successful, -1 otherwise
     */
    override suspend fun updateRankingScore(meetUp: MeetUp): Double {
        return try {
            val batch = db.batch()
            val score = getRankingScore(meetUp)
            batch.update(
                db.collection(MEETUP_COLL).document(meetUp.uuid),
                RANKING_SCORE,
                score
            )
            batch.commit().await()
            score
        } catch (e: Exception) {
            -1.0
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
                val meetUps = fetch(profile.joinedMeetUps)
                if (currentDate != null) {
                    val filtered = meetUps.filter { !it.isFinished(currentDate) }
                    emit(Success(filtered))
                } else {
                    emit(Success(meetUps))
                }
            }
        }
    }

    override suspend fun getAllJoinedMeetupID(): List<String>{
        val lst = fetchAll(Calendar.getInstance()).take(1).toList()
        return lst[0].filter { it.isParticipating(profileService.getLoggedInUserID()) }.map { it.uuid }
    }

    companion object {
        const val MEETUP_COLL = "meetups"
    }
}