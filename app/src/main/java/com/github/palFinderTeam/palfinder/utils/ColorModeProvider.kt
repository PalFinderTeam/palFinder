package com.github.palFinderTeam.palfinder.utils

import androidx.annotation.StyleRes
import com.github.palFinderTeam.palfinder.R

interface ColorModeProvider {

    @StyleRes
    fun getColoredTheme(): Int

}

object ColorModeProviderImpl : ColorModeProvider {

    private val settingsData: SettingsData = SettingsData(
        theme = "default"
    )

    override fun getColoredTheme(): Int {
        return when (settingsData.theme) {
            "default" -> R.style.palFinder_default_theme
            "warm" -> R.style.palFinder_warm_theme
            else -> R.style.palFinder_default_theme
        }
    }


}