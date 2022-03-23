package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.di.ProfileModule
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
                    "name" -> oldVal.copy(name = value as String)
                    "surname" -> oldVal.copy(surname = value as String)
                    "username" -> oldVal.copy(username = value as String)
                    "join_date" -> oldVal.copy(joinDate = value as Calendar)
                    "picture" -> oldVal.copy(pfp = ImageInstance(value as String))
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
                val profile = db[userId]!!
                emit(Response.Success(profile))
            }
        }

        fun clearDB() {
            db.clear()
        }

        fun syncCreateProfile(newUserProfile: ProfileUser): String? {
            val key = counter.toString()
            db[key] = newUserProfile.copy(uuid = key)
            counter.inc()
            return key
        }
    }
}