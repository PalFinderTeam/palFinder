package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.R

/**
 * enum of all the badges and achievements you can get in palFinder
 */
enum class Achievement(val aName: String, val imageID : Int, val desc: String = "") {
    DEVELOPER("dev", R.drawable.ic_badge_dev), //for the palfinder team

    PAL_FINDER("pal finder", R.drawable.ic_badge_fan1), //obtained when you follow the first milestone number
    PAL_MINER("pal miner", R.drawable.ic_badge_fan2), //obtained when you follow the second milestone number
    PAL_TRACKER("pal tracker", R.drawable.ic_badge_fan3), //obtained when you follow the third milestone number
    PALDEX_COMPLETED("paldex", R.drawable.ic_badge_fan4), //obtained when you follow the fourth milestone number

    BEAUTY_AND_THE_PAL("the beauty and the pal", R.drawable.ic_badge_pal1), //obtained when you are followed by the first milestone number
    CRYPTO_PAL("cryptoPal", R.drawable.ic_badge_pal2), //obtained when you are followed by the second milestone number
    MASTER_OF_CATS("master of cats", R.drawable.ic_badge_pal3), //obtained when you are followed by the third milestone number
    ULTIMATE_PAL("verified user", R.drawable.ic_badge_pal4), //obtained when you are followed by the fourth milestone number

    VERIFIED("verified user", R.drawable.ic_checkmark), //obtained when you are trustworthy

    NON_SPEC("what are you searching there ?", R.drawable.not_found); //used when the Achievement is not in this list

    companion object {
        fun from(type: String?): Achievement = values().find{it.aName == type} ?: NON_SPEC
    }
}

/**
 * Milestone numbers for achievement levels
 */
object AchievementMilestones {
    const val MILESTONE1 = 1
    const val MILESTONE2 = 5
    const val MILESTONE3 = 10
    const val MILESTONE4 = 25

    /**
     * To change the triggering milestone condition
     */
    fun followCountAdapt(milestone: Int) = milestone - 1
}