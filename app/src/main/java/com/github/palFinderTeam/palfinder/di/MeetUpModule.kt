package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.meetups.CachedMeetUpService
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MeetUpModule {

    @Singleton
    @Binds
    abstract fun bindMeetUpService(
        meetUpService: CachedMeetUpService
    ): MeetUpRepository
}