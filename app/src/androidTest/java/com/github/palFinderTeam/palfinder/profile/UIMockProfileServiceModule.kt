package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.di.ProfileModule
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.ACHIEVEMENTS_OBTAINED
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.BLOCKED_USERS
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.DESCRIPTION_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWED_BY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWING_PROFILES
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.GENDER
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.JOINED_MEETUPS_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.JOIN_DATE_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.MUTED_MEETUPS
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.NAME_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.PICTURE_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.PRIVACY_SETTINGS_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.SURNAME_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.USERNAME_KEY
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.PrivacySettings
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
                    BLOCKED_USERS -> oldVal.copy(blockedUsers = value as List<String>)
                    ACHIEVEMENTS_OBTAINED -> oldVal.copy(achievements = value as List<String>)
                    PRIVACY_SETTINGS_KEY -> oldVal.copy(privacySettings = value as PrivacySettings)
                    MUTED_MEETUPS -> oldVal.copy(mutedMeetups = value as List<String>)
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

        override suspend fun create(obj: ProfileUser): String? {
            return syncCreateProfile(obj)
        }

        override fun fetchFlow(uuid: String): Flow<Response<ProfileUser>> {
            return flow {
                if (db.containsKey(uuid)) {
                    val profile = db[uuid]!!
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

        fun syncCreateProfile(newUserProfile: ProfileUser): String {
            val key = counter.toString()
            db[key] = newUserProfile.copy(uuid = key)
            counter++
            return key
        }

        override fun getLoggedInUserID(): String? = loggedUserId

        override suspend fun exists(uuid: String): Boolean {
            return db.containsKey(uuid)
        }

        override fun fetchAll(currentDate: Calendar?): Flow<List<ProfileUser>> {
            return flow{emit(db.values.toList())}
        }

        override suspend fun followUser(user: ProfileUser, targetId: String): Response<Unit> {
            return try {
                if (!user.canFollow(targetId)) {
                    return Response.Failure("Cannot follow this user.")
                }
                val targetProfile = fetch(targetId)!!
                db[user.uuid] = user.copy(following = user.following.plus(targetId))
                if (updateAchievementsFollower(db[user.uuid]!!).isNotEmpty()) {
                    db[user.uuid] = db[user.uuid]!!.copy(achievements = user.achievements().map{it.aName}.
                    plus(updateAchievementsFollower(db[user.uuid]!!)[0]))
                }
                db[targetId] = db[targetId]!!.copy(followed = db[targetId]!!.followed.plus(user.uuid))
                if (updateAchievementsFollowed(db[targetId]!!).isNotEmpty()) {
                    db[targetId] = db[targetId]!!.copy(achievements = targetProfile.achievements().map{it.aName}.plus(
                        updateAchievementsFollower(db[targetId]!!)[0]
                    ))
                }
                Response.Success(Unit)
            } catch (e: Exception) {
                Response.Failure(e.message.orEmpty())
            }
        }

        override suspend fun unfollowUser(user: ProfileUser, targetId: String): Response<Unit> {
            return try {
                if (!user.canUnFollow(targetId)) {
                    return Response.Failure("Cannot mute this meetup.")
                }
                db[user.uuid] = user.copy(following = user.following.minus(targetId))
                db[targetId] = db[targetId]!!.copy(followed = db[targetId]!!.followed.minus(user.uuid))
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
                db[user.uuid] = user.copy(mutedMeetups = user.mutedMeetups.plus(meetup))

                Response.Success(Unit)
            } catch (e: Exception) {
                Response.Failure(e.message.orEmpty())
            }
        }

        override suspend fun unMuteMeetup(user: ProfileUser, meetup: String): Response<Unit> {
            return try {
                if (!user.canUnMuteMeetup(meetup)) {
                    return Response.Failure("Cannot unmute this meetup.")
                }
                db[user.uuid] = user.copy(mutedMeetups = user.mutedMeetups.minus(meetup))
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
