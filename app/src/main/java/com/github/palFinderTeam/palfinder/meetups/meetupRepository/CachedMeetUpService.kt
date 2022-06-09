package com.github.palFinderTeam.palfinder.meetups.meetupRepository

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.cache.FileCache
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.ShowParam
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import com.github.palFinderTeam.palfinder.utils.generics.CachedRepository
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * adds cache dictionaries to the meetupRepository, for both the meetups printed (temporary) and the meetups joined (permanent)
 */
class CachedMeetUpService @Inject constructor(
    private val db: FirebaseMeetUpService,
    time: TimeService,
    contextProvider: ContextService
) : MeetUpRepository {
    companion object{
        const val REPOSITORY = "meetup"
        const val REPOSITORY_JOINED = "meetup_joined"
    }
    private val cacheJoined =
        FileCache(REPOSITORY_JOINED, JoinedMeetupListWrapper::class.java, true, contextProvider.get())

    private val cache = CachedRepository(REPOSITORY, MeetUp::class.java, db, time, contextProvider)

    override suspend fun create(obj: MeetUp): String? {
        val ret = cache.create(obj)
        if (ret != null) {
            addJoinedMeetupToCache(ret)
            fetch(ret)
        }
        return ret
    }

    override suspend fun fetch(uuid: String): MeetUp? = cache.fetch(uuid)

    override suspend fun fetch(uuids: List<String>): List<MeetUp> = cache.fetch(uuids)

    override fun fetchAll(currentDate: Calendar?): Flow<List<MeetUp>> = cache.fetchAll(currentDate)

    override fun fetchFlow(uuid: String): Flow<Response<MeetUp>> = cache.fetchFlow(uuid)

    override suspend fun edit(uuid: String, obj: MeetUp): String? = cache.edit(uuid, obj)

    override suspend fun edit(uuid: String, field: String, value: Any): String? =
        cache.edit(uuid, field, value)

    override suspend fun exists(uuid: String): Boolean = cache.exists(uuid)

    private fun addJoinedMeetupToCache(meetUpId: String) {
        val jml = if (cacheJoined.exist()) {
            cacheJoined.get()
        } else {
            JoinedMeetupListWrapper(mutableListOf())
        }
        if (!jml.lst.contains(meetUpId)) {
            jml.lst.add(meetUpId)
            cacheJoined.store(jml)
        }
    }

    private fun removeJoinedMeetupFromCache(meetUpId: String) {
        val jml = cacheJoined.get()
        while (jml.lst.contains(meetUpId)) {
            jml.lst.remove(meetUpId)
        }
        cacheJoined.store(jml)
    }

    /**
     * Return List of all joined Meetup ID
     */
    override suspend fun getAllJoinedMeetupID(): List<String> {
        return if (cacheJoined.exist()) {
            cacheJoined.get().lst
        } else {
            mutableListOf()
        }
    }

    override suspend fun updateRankingScore(meetUp: MeetUp): Double {
        cache.evict(meetUp.uuid)
        return db.updateRankingScore(meetUp)
    }

    override fun getMeetUpsAroundLocation(
        location: Location,
        radiusInKm: Double,
        currentDate: Calendar?,
        showParam: ShowParam?,
        profile: ProfileUser?
    ): Flow<Response<List<MeetUp>>> {
        return db.getMeetUpsAroundLocation(location, radiusInKm, currentDate, showParam, profile)
    }


    override suspend fun joinMeetUp(
        meetUpId: String,
        userId: String,
        now: Calendar,
        profile: ProfileUser
    ): Response<Unit> {
        return when (val ret = db.joinMeetUp(meetUpId, userId, now, profile)) {
            is Response.Success -> {
                addJoinedMeetupToCache(meetUpId)
                ret
            }
            else -> ret
        }
    }

    override suspend fun leaveMeetUp(meetUpId: String, userId: String): Response<Unit> {
        return when (val ret = db.leaveMeetUp(meetUpId, userId)) {
            is Response.Success -> {
                removeJoinedMeetupFromCache(meetUpId)
                ret
            }
            else -> ret
        }
    }

    override fun getUserMeetups(
        userId: String,
        currentDate: Calendar?
    ): Flow<Response<List<MeetUp>>> {
        return db.getUserMeetups(userId, currentDate)
    }

    private data class JoinedMeetupListWrapper(val lst: MutableList<String>)
}