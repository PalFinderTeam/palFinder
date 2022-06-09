package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.utils.context.AppContextService
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/**
 * Inject context when needed, it makes certain class more testable.
 */
abstract class ContextModule {

    @Singleton
    @Binds
    abstract fun bindContextService(
        contextService: AppContextService
    ): ContextService
}