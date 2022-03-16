package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockMeetUpRepository : MeetUpRepository {
    val db: HashMap<String, MeetUp> = hashMapOf()
    private var counter = 0

    override suspend fun getMeetUpData(meetUpId: String): MeetUp? {
        return db[meetUpId]
    }

    override suspend fun createMeetUp(newMeetUp: MeetUp): String? {
        val key = counter.toString()
        db[key] = newMeetUp
        counter.inc()
        return key
    }

    override suspend fun editMeetUp(meetUpId: String, field: String, value: Any): String? {
        if (db.containsKey(meetUpId)) {
            val oldVal = db[meetUpId]!!
            db[meetUpId] = when(field) {
                "name" -> oldVal.copy(name = value as String)
                "capacity" -> oldVal.copy(capacity = value as Int)
                "creator" -> oldVal.copy(creator = value as ProfileUser)
                "description" -> oldVal.copy(description = value as String)
                "startDate" -> oldVal.copy(startDate = value as Calendar)
                "endDate" -> oldVal.copy(endDate = value as Calendar)
                "hasMaxCapacity" -> oldVal.copy(hasMaxCapacity = value as Boolean)
                "icon" -> oldVal.copy(icon = value as String)
                "location" -> oldVal.copy(location = value as Location)
                "participants" -> oldVal.copy(participants = value as MutableList<ProfileUser>)
                "tags" -> oldVal.copy(tags = value as Set<Category>)
                else -> oldVal
            }
            return meetUpId
        }
        return null
    }

    override suspend fun editMeetUp(meetUpId: String, meetUp: MeetUp): String? {
        return if (db.containsKey(meetUpId)) {
            db[meetUpId] = meetUp
            meetUpId
        } else {
            null
        }
    }

    override fun getMeetUpsAroundLocation(
        location: Location,
        radiusInM: Double
    ): Flow<Response<List<MeetUp>>> {
        return flow {
            val meetUps = db.values.filter { meetUp ->
                meetUp.location.distanceInKm(location)*1000 <= radiusInM
            }

            emit(Response.Success(meetUps))
        }
    }

    @ExperimentalCoroutinesApi
    override fun getAllMeetUps(): Flow<List<MeetUp>> {
        return flow {
            emit(db.values.toList())
        }
    }

    fun clearDB() {
        db.clear()
    }
}