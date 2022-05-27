package com.github.palFinderTeam.palfinder.utils.context

import android.content.Context
import com.github.palFinderTeam.palfinder.PalFinderApplication
import javax.inject.Inject

/**
 * Class to get the context of the application
 */
class AppContextService @Inject constructor(): ContextService{
    override fun get(): Context {
        return PalFinderApplication.instance
    }
}