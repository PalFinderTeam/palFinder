package com.github.palFinderTeam.palfinder.di

import android.icu.util.Calendar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
/**
 * Inject calendar when needed, it makes certain class more testable.
 */
object CalendarModule {

    @Provides
    fun provideCalendar(): Calendar {
        return Calendar.getInstance()
    }
}