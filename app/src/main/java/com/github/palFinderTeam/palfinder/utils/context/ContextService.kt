package com.github.palFinderTeam.palfinder.utils.context

import android.content.Context

/**
 * Represents a service that provides access to the application context.
 */
interface ContextService {
    fun get(): Context
}