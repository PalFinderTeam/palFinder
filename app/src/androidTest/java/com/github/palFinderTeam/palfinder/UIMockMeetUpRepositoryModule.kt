package com.github.palFinderTeam.palfinder

import com.github.palFinderTeam.palfinder.di.MeetUpModule
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.utils.Location
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
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

    val mockRepository = UIMockRepository()

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
            TODO("Not yet implemented")
        }

        override suspend fun editMeetUp(meetUpId: String, meetUp: MeetUp): String? {
            return if (db.containsKey(meetUpId)) {
                db[meetUpId] = meetUp
                meetUpId
            } else {
                null
            }
        }

        override suspend fun getMeetUpsAroundLocation(location: Location): Array<MeetUp>? {
            TODO("Not yet implemented")
        }
    }
}