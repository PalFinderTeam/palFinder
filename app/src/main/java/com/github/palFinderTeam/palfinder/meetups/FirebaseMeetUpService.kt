package com.github.palFinderTeam.palfinder.meetups

import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.utils.Location
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

    override suspend fun getMeetUpsAroundLocation(location: Location): Array<MeetUp>? {
        return null
    }
}