package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.profile.services.ProfileService
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockProfileService : ProfileService {
    val db: HashMap<String, ProfileUser> = hashMapOf()
    private var counter = 0
    var loggedUserId:String? = null

    fun setLoggedInUserID(value: String?){
        loggedUserId = value
    }
    override fun getLoggedInUserID(): String? = loggedUserId

    override suspend fun exists(uuid: String): Boolean {
        return db.containsKey(uuid)
    }

    override fun fetchAll(currentDate: Calendar?): Flow<List<ProfileUser>> {
        return flow{
            emit(db.values.toList())
        }
    }

    override suspend fun followUser(user: ProfileUser, targetId: String): Response<Unit> {
        return try {
            if (!user.canFollow(targetId)) {
                return Response.Failure("Cannot follow this user.")
            }
            db[user.uuid] = user.copy(following = user.following.plus(targetId))
            db[targetId] = db[targetId]!!.copy(followed = user.followed.plus(user.uuid))
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Failure(e.message.orEmpty())
        }
    }

    override suspend fun unfollowUser(user: ProfileUser, targetId: String): Response<Unit> {
        return try {
            if (!user.canUnFollow(targetId)) {
                return Response.Failure("Cannot unfollow this user.")
            }
            db[user.uuid] = user.copy(following = user.following.minus(targetId))
            db[targetId] = db[targetId]!!.copy(followed = user.followed.minus(user.uuid))
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Failure(e.message.orEmpty())
        }
    }

    override suspend fun muteMeetup(user: ProfileUser, meetup: String): Response<Unit> {
        return try {
            if (!user.canMuteMeetup(meetup)) {
                return Response.Failure("Cannot mute this meetup.")
            }
            db[user.uuid] = user.copy(mutedMeetups= user.following.plus(meetup))
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Failure(e.message.orEmpty())
        }
    }

    override suspend fun unMuteMeetup(user: ProfileUser, meetup: String): Response<Unit> {
        return try {
            if (!user.canUnMuteMeetup(meetup)) {
                return Response.Failure("Cannot unfollow this user.")
            }
            db[user.uuid] = user.copy(mutedMeetups = user.following.minus(meetup))
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Failure(e.message.orEmpty())
        }
    }

    override suspend fun fetch(uuid: String): ProfileUser? {
        return db[uuid]
    }

    override suspend fun fetch(uuids: List<String>): List<ProfileUser>? {
        return uuids.mapNotNull { fetch(it) }
    }

    override suspend fun edit(uuid: String, field: String, value: Any): String? {
        if (db.containsKey(uuid)) {
            val oldVal = db[uuid]!!
            db[uuid] = when (field) {
                ProfileUser.NAME_KEY -> oldVal.copy(name = value as String)
                ProfileUser.SURNAME_KEY -> oldVal.copy(surname = value as String)
                ProfileUser.USERNAME_KEY -> oldVal.copy(username = value as String)
                ProfileUser.JOIN_DATE_KEY -> oldVal.copy(joinDate = value as Calendar)
                ProfileUser.PICTURE_KEY -> oldVal.copy(pfp = ImageInstance(value as String))
                ProfileUser.DESCRIPTION_KEY -> oldVal.copy(description = value as String)
                ProfileUser.JOINED_MEETUPS_KEY -> oldVal.copy(joinedMeetUps = value as List<String>)
                ProfileUser.GENDER -> oldVal.copy(gender = value as Gender)
                else -> oldVal
            }
            return uuid
        }
        return null
    }

    override suspend fun edit(uuid: String, obj: ProfileUser): String? {
        return if (db.containsKey(uuid)) {
            db[uuid] = obj
            uuid
        } else {
            null
        }
    }

    override suspend fun create(obj: ProfileUser): String {
        val key = obj.uuid
        db[key] = obj.copy(uuid = key)
        counter.inc()
        return key
    }

    override fun fetchFlow(uuid: String): Flow<Response<ProfileUser>> {
        return flow {
            val profile = db[uuid]!!
            emit(Response.Success(profile))
        }
    }

    fun clearDB() {
        db.clear()
    }
}