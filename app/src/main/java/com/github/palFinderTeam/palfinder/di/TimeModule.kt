package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.utils.time.RealTimeService
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/**
 * Inject time when needed, it makes certain class more testable.
 */
abstract class TimeModule {

    @Singleton
    @Binds
    abstract fun bindTimeService(
        timeService: RealTimeService
    ): TimeService
}