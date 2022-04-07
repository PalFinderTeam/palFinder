package com.github.palFinderTeam.palfinder.utils.context

import android.content.Context

/**
 * Service to inject context. Use for static field were storing the context is a memory leak.
 */
interface ContextService {
    fun get(): Context
}