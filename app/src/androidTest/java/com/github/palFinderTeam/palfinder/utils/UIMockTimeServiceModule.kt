package com.github.palFinderTeam.palfinder.utils


import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.di.TimeModule
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [TimeModule::class]
)
/**
 * Provide a mock time service for every UI tests.
 */
object UIMockTimeServiceModule {

    private val timeService = UIMockTimeService()

    @Singleton
    @Provides
    fun provideTimeService(): TimeService {
        return timeService
    }

    class UIMockTimeService : TimeService {
        private var date = Calendar.getInstance()
        override fun now(): Calendar {
            return date
        }
        fun setDate(now: Calendar):TimeService{
            date = now
            return this
        }
    }
}