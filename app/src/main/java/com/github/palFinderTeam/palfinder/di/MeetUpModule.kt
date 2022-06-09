package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.meetups.meetupRepository.CachedMeetUpService
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.MeetUpRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/**
 * Inject meetUp when needed, it makes certain class more testable.
 */
abstract class MeetUpModule {

    @Singleton
    @Binds
    abstract fun bindMeetUpService(
        meetUpService: CachedMeetUpService
    ): MeetUpRepository
}