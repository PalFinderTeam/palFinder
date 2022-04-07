package com.github.palFinderTeam.palfinder.utils.context

import android.content.Context
import com.github.palFinderTeam.palfinder.PalFinderApplication
import javax.inject.Inject

class AppContextService @Inject constructor(): ContextService{
    override fun get(): Context {
        return PalFinderApplication.instance.applicationContext
    }
}