package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import com.github.palFinderTeam.palfinder.utils.generics.CachedRepository
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CachedProfileService @Inject constructor(
    private val db: FirebaseProfileService,
    private val time: TimeService,
    private val contextProvider: ContextService
) : ProfileService {
    companion object{
        const val REPOSITORY = "profile"
    }

    private val cache =
        CachedRepository(REPOSITORY, ProfileUser::class.java, db, time, contextProvider)

    override suspend fun create(obj: ProfileUser): String? = cache.create(obj)

    override suspend fun fetch(uuid: String): ProfileUser? = cache.fetch(uuid)

    override suspend fun fetch(uuids: List<String>): List<ProfileUser> = cache.fetch(uuids)

    override fun fetchAll(currentDate: Calendar?): Flow<List<ProfileUser>> =
        cache.fetchAll(currentDate)

    override fun fetchFlow(uuid: String): Flow<Response<ProfileUser>> = cache.fetchFlow(uuid)

    override suspend fun edit(uuid: String, obj: ProfileUser): String? = cache.edit(uuid, obj)

    override suspend fun edit(uuid: String, field: String, value: Any): String? =
        cache.edit(uuid, field, value)

    override suspend fun exists(uuid: String): Boolean = cache.exists(uuid)

    override fun getLoggedInUserID(): String? = db.getLoggedInUserID()

    override suspend fun followUser(user: ProfileUser, targetId: String): Response<Unit> =
        db.followUser(user, targetId)

    override suspend fun unfollowUser(user: ProfileUser, targetId: String): Response<Unit> =
        db.unfollowUser(user, targetId)

    override suspend fun muteMeetup(user: ProfileUser, meetup: String): Response<Unit> {
        cache.evict(user.uuid)
        return db.muteMeetup(user, meetup)
    }

    override suspend fun unMuteMeetup(user: ProfileUser, meetup: String): Response<Unit> {
        cache.evict(user.uuid)
        return db.unMuteMeetup(user, meetup)
    }
}