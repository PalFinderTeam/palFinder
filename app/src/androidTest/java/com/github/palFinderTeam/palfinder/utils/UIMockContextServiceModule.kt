package com.github.palFinderTeam.palfinder.utils


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.palFinderTeam.palfinder.di.ContextModule
import com.github.palFinderTeam.palfinder.di.TimeModule
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ContextModule::class]
)
/**
 * Provide a mock time service for every UI tests.
 */
object UIMockContextServiceModule {

    private val contextService = UIMockContextService()

    @Singleton
    @Provides
    fun provideContextService(): ContextService {
        return contextService
    }

    class UIMockContextService : ContextService {
        override fun get(): Context {
            return ApplicationProvider.getApplicationContext()
        }
    }
}