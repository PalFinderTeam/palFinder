package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.profile.CachedProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Singleton
    @Binds
    abstract fun bindProfileService(
        firebaseProfileService: CachedProfileService
    ): ProfileService

}