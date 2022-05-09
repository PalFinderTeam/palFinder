package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import android.util.Log
import com.github.palFinderTeam.palfinder.di.ProfileModule
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.DESCRIPTION_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWED_BY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWING_PROFILES
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.GENDER
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.JOINED_MEETUPS_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.JOIN_DATE_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.NAME_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.PICTURE_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.SURNAME_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.USERNAME_KEY
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.firebase.firestore.FieldValue
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ProfileModule::class]
)
/**
 * Provide a mock profile database for every UI tests.
 */
object UIMockProfileServiceModule {

    private val mockService = UIMockProfileService()

    @Singleton
    @Provides
    fun provideProfileService(): ProfileService {
        return mockService
    }

    class UIMockProfileService : ProfileService {
        val db: HashMap<String, ProfileUser> = hashMapOf()
        var loggedUserId:String? = null

        private var counter = 0

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
                    NAME_KEY -> oldVal.copy(name = value as String)
                    SURNAME_KEY -> oldVal.copy(surname = value as String)
                    USERNAME_KEY -> oldVal.copy(username = value as String)
                    JOIN_DATE_KEY -> oldVal.copy(joinDate = value as Calendar)
                    PICTURE_KEY -> oldVal.copy(pfp = ImageInstance(value as String))
                    DESCRIPTION_KEY -> oldVal.copy(description = value as String)
                    JOINED_MEETUPS_KEY -> oldVal.copy(joinedMeetUps = value as List<String>)
                    GENDER -> oldVal.copy(gender = value as Gender)
                    FOLLOWING_PROFILES -> oldVal.copy(following = value as List<String>)
                    FOLLOWED_BY -> oldVal.copy(followed = value as List<String>)
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
            return syncCreateProfile(newUserProfile)
        }

        override fun fetchProfileFlow(userId: String): Flow<Response<ProfileUser>> {
            return flow {
                if (db.containsKey(userId)) {
                    val profile = db[userId]!!
                    emit(Response.Success(profile))
                }
                else{
                    emit(Response.Failure("Not Found"))
                }
            }
        }

        fun clearDB() {
            db.clear()
        }

        fun syncCreateProfile(newUserProfile: ProfileUser): String? {
            val key = counter.toString()
            db[key] = newUserProfile.copy(uuid = key)
            counter++
            return key
        }

        override fun getLoggedInUserID(): String? = loggedUserId

        override suspend fun doesUserIDExist(userId: String): Boolean {
            return db.containsKey(userId)
        }

        override suspend fun followUser(user: ProfileUser, targetId: String): Response<Unit> {
            return try {
                if (!user.canFollow(targetId)) {
                    return Response.Failure("Cannot follow this user.")
                }
                val targetProfile = fetchUserProfile(targetId)!!
                db[user.uuid] = user.copy(following = user.following.plus(targetId))
                if (updateAchievementsFollower(user) != null) {
                    db[user.uuid] = user.copy(achievements = user.achievements().map{it.string}.plus(updateAchievementsFollower(user)!!))
                }
                db[targetId] = db[targetId]!!.copy(followed = db[targetId]!!.followed.plus(user.uuid))
                if (updateAchievementsFollowed(targetProfile) != null) {
                    db[targetId] = user.copy(achievements = user.achievements().map{it.string}.plus(updateAchievementsFollower(targetProfile)!!))
                }
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
                db[targetId] = db[targetId]!!.copy(followed = db[targetId]!!.followed.minus(user.uuid))
                Response.Success(Unit)
            } catch (e: Exception) {
                Response.Failure(e.message.orEmpty())
            }
        }

        fun setLoggedInUserID(value: String?){
            loggedUserId = value
        }
    }
}