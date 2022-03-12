package com.github.palFinderTeam.palfinder.meetups

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MeetUpModule {

    @Singleton
    @Provides
    fun provideFirebaseMeetUpService(): MeetUpRepository {
        return FirebaseMeetUpService
    }
}