package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tags.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class MockMeetUpRepository : MeetUpRepository {
    val db: HashMap<String, MeetUp> = hashMapOf()
    private var counter = 0

    override suspend fun fetch(uuid: String): MeetUp? {
        return db[uuid]
    }

    override suspend fun create(obj: MeetUp): String {
        val key = counter.toString()
        db[key] = obj.copy(uuid = key)
        counter.inc()
        return key
    }

    override suspend fun edit(uuid: String, field: String, value: Any): String? {
        if (db.containsKey(uuid)) {
            val oldVal = db[uuid]!!
            db[uuid] = when (field) {
                "name" -> oldVal.copy(name = value as String)
                "capacity" -> oldVal.copy(capacity = value as Int)
                "creator" -> oldVal.copy(creatorId = value as String)
                "description" -> oldVal.copy(description = value as String)
                "startDate" -> oldVal.copy(startDate = value as Calendar)
                "endDate" -> oldVal.copy(endDate = value as Calendar)
                "hasMaxCapacity" -> oldVal.copy(hasMaxCapacity = value as Boolean)
                "icon" -> oldVal.copy(iconImage = value as ImageInstance)
                "location" -> oldVal.copy(location = value as Location)
                "participants" -> oldVal.copy(participantsId = value as List<String>)
                "tags" -> oldVal.copy(tags = value as Set<Category>)
                else -> oldVal
            }
            return uuid
        }
        return null
    }

    override fun fetchFlow(uuid: String): Flow<Response<MeetUp>> {
        return flow{
            if (db.containsKey(uuid)){
                emit(Response.Success(db[uuid]!!))
            } else {
                emit(Response.Failure("Could not find obj."))
            }
        }
    }

    override suspend fun edit(uuid: String, obj: MeetUp): String? {
        return if (db.containsKey(uuid)) {
            db[uuid] = obj
            uuid
        } else {
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
        return flow {
            var meetUps = db.values.filter { meetUp ->
                meetUp.location.distanceInKm(location) <= radiusInKm && additionalFilter(profile, meetUp, showParam)
            }
            if (currentDate != null) {
                meetUps = meetUps.filter { !it.isFinished(currentDate) }
            }

            emit(Response.Success(meetUps))
        }
    }

    override fun fetchAll(currentDate: Calendar?): Flow<List<MeetUp>> {
        return flow {
            var meetUps = db.values.toList()
            if (currentDate != null) {
                meetUps = meetUps.filter { !it.isFinished(currentDate) }
            }
            emit(meetUps)
        }
    }

    override suspend fun joinMeetUp(
        meetUpId: String,
        userId: String,
        now: Calendar,
        profile: ProfileUser
    ): Response<Unit> {
        return if (db.containsKey(meetUpId)) {
            val meetUp = db[meetUpId] ?: return Response.Failure("Could not find meetup")
            if (meetUp.isParticipating(userId)) {
                return Response.Success(Unit)
            }
            // We ignore the date because it is tedious to mock
            if (meetUp.isFull()) {
                return Response.Failure("Cannot join, it is full.")
            }
            if (meetUp.creatorId == userId) {
                return Response.Failure("Cannot leave your own meetup.")
            }

            db[meetUpId] = meetUp.copy(participantsId = meetUp.participantsId.plus(userId))
            Response.Success(Unit)
        } else {
            Response.Failure("Could not join meetup")
        }
    }

    override suspend fun leaveMeetUp(meetUpId: String, userId: String): Response<Unit> {
        return try {
            val meetUp =
                fetch(meetUpId) ?: return Response.Failure("Could not find meetup.")
            if (!meetUp.isParticipating(userId)) {
                return Response.Failure("Cannot leave a meetup which was not joined before")
            }
            db[meetUpId] = meetUp.copy(participantsId = meetUp.participantsId.minus(userId))
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Failure(e.message.orEmpty())
        }
    }

    override fun getUserMeetups(
        userId: String,
        currentDate: Calendar?
    ): Flow<Response<List<MeetUp>>> {
        val userMeetUps = fetchAll(currentDate).map { Response.Success(it.filter { it.isParticipating(userId) }) }
        if (currentDate != null) {
            return userMeetUps.map { Response.Success(it.data.filter { !it.isFinished(currentDate) }) }
        } else {
            return userMeetUps
        }
    }

    override suspend fun getAllJoinedMeetupID(): List<String> {
        return fetchAll(Calendar.getInstance()).toList()[0].filter { it.isParticipating("user1") }.map { it.uuid }
    }

    private fun getRankingScore(meetUp: MeetUp): Double {
        return meetUp.participantsId.size.toDouble()
    }

    override suspend fun updateRankingScore(meetUp: MeetUp): Double {
        return try {
            val score = getRankingScore(meetUp)
            edit(meetUp.uuid, "rankingScore", score)
            score
        } catch (e: Exception) {
            -1.0
        }
    }

    override suspend fun exists(uuid: String): Boolean {
        return db.containsKey(uuid)
    }

    override suspend fun fetch(uuids: List<String>): List<MeetUp> {
        return uuids.mapNotNull { db[it] }
    }

    fun clearDB() {
        db.clear()
    }
}