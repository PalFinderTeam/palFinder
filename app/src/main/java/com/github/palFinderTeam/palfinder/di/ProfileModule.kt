package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.profile.FirebaseProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ProfileModule {

    @Singleton
    @Provides
    fun provideFirebaseProfileService(): ProfileService {
        return FirebaseProfileService
    }
}