package com.github.palFinderTeam.palfinder

import android.icu.util.Calendar
import android.util.Log
import com.github.palFinderTeam.palfinder.di.MeetUpModule
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.activities.ShowParam
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [MeetUpModule::class]
)
/**
 * Provide a mock meetup database for every UI tests.
 */
object UIMockMeetUpRepositoryModule {

    private val mockRepository = UIMockRepository()

    @Singleton
    @Provides
    fun provideFirebaseMeetUpService(): MeetUpRepository {
        return mockRepository
    }

    /**
     * Copy of MockMeetUpRepository, just for scoping reason you cannot reuse it.
     */
    class UIMockRepository : MeetUpRepository {
        val db: HashMap<String, MeetUp> = hashMapOf()
        private var counter = 0

        public var loggedUserID = "user"

        override suspend fun fetch(uuid: String): MeetUp? {
            return db[uuid]
        }

        override suspend fun create(obj: MeetUp): String {
            val key = counter.toString()
            db[key] = obj.copy(uuid = key)
            counter += 1
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
                if (db.containsKey(uuid)) {
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
                emit(Response.Loading())
                var meetUps = db.values.filter { meetUp ->
                    meetUp.location.distanceInKm(location) <= radiusInKm && additionalFilter(profile, meetUp, showParam)
                }
                if (currentDate != null) {
                    meetUps = meetUps.filter { !it.isFinished(currentDate) }
                }

                emit(Response.Success(meetUps))
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
                if (!meetUp.canJoin(now, profile)) {
                    return Response.Failure("Cannot join meetup now.")
                }
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

        override fun fetchAll(
            currentDate: Calendar?,
        ): Flow<List<MeetUp>> {
            return flow {
                var meetUps = db.values.toList()
                if (currentDate != null) {
                    meetUps = meetUps.filter { !it.isFinished(currentDate) }
                }
                emit(meetUps)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
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
            return fetchAll(Calendar.getInstance()).toList()[0].filter { it.isParticipating(loggedUserID) }.map { it.uuid }
        }

        override suspend fun exists(uuid: String): Boolean {
            return db.containsKey(uuid)
        }

        override suspend fun fetch(uuids: List<String>): List<MeetUp>? {
            return uuids.mapNotNull { db[it] }
        }

        fun clearDB() {
            db.clear()
        }
    }
}