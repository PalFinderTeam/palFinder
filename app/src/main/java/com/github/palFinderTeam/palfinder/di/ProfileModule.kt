package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.profile.FirebaseProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Singleton
    @Binds
    abstract fun bindProfileService(
        firebaseProfileService: FirebaseProfileService
    ): ProfileService

}