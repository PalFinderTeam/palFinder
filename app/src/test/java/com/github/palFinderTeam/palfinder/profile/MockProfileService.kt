package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
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

    override suspend fun doesUserIDExist(userId: String): Boolean {
        return db.containsKey(userId)
    }

    override suspend fun fetchUserProfile(userId: String): ProfileUser? {
        return db[userId]
    }

    override suspend fun fetchUsersProfile(userIds: List<String>): List<ProfileUser>? {
        return userIds.mapNotNull { fetchUserProfile(it) }
    }

    override suspend fun editUserProfile(userId: String, field: String, value: Any): String? {
        if (db.containsKey(userId)) {
            val oldVal = db[userId]!!
            db[userId] = when (field) {
                ProfileUser.NAME_KEY -> oldVal.copy(name = value as String)
                ProfileUser.SURNAME_KEY -> oldVal.copy(surname = value as String)
                ProfileUser.USERNAME_KEY -> oldVal.copy(username = value as String)
                ProfileUser.JOIN_DATE_KEY -> oldVal.copy(joinDate = value as Calendar)
                ProfileUser.PICTURE_KEY -> oldVal.copy(pfp = ImageInstance(value as String))
                ProfileUser.DESCRIPTION_KEY -> oldVal.copy(description = value as String)
                ProfileUser.JOINED_MEETUPS_KEY -> oldVal.copy(joinedMeetUps = value as List<String>)
                else -> oldVal
            }
            return userId
        }
        return null
    }

    override suspend fun editUserProfile(userId: String, userProfile: ProfileUser): String? {
        return if (db.containsKey(userId)) {
            db[userId] = userProfile
            userId
        } else {
            null
        }
    }

    override suspend fun createProfile(newUserProfile: ProfileUser): String? {
        val key = newUserProfile.uuid
        db[key] = newUserProfile.copy(uuid = key)
        counter.inc()
        return key
    }

    override fun fetchProfileFlow(userId: String): Flow<Response<ProfileUser>> {
        return flow {
            val profile = db[userId]!!
            emit(Response.Success(profile))
        }
    }
}