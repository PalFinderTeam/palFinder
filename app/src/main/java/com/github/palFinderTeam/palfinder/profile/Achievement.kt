package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.R



/**
 * enum of all the badges and achievements you can get in palFinder
 */
enum class Achievement(val cat: AchievementCategory, val aName: String, val imageID : Int, val milestone: Int) {
    DEVELOPER(AchievementCategory.OTHER, "Dev", R.drawable.ic_badge_dev, AchievementMilestones.NOT_FOUND), //for the palfinder team
    VERIFIED(AchievementCategory.OTHER,"Verified user", R.drawable.ic_checkmark, AchievementMilestones.NOT_FOUND), //obtained when you are trustworthy

    PAL_FINDER(AchievementCategory.FOLLOWING,"Pal finder", R.drawable.ic_badge_fan1, AchievementMilestones.MILESTONE1), //obtained when you follow the first milestone number
    PAL_MINER(AchievementCategory.FOLLOWING,"Pal miner", R.drawable.ic_badge_fan2, AchievementMilestones.MILESTONE2), //obtained when you follow the second milestone number
    PAL_TRACKER(AchievementCategory.FOLLOWING,"Pal tracker", R.drawable.ic_badge_fan3, AchievementMilestones.MILESTONE3), //obtained when you follow the third milestone number
    PALDEX_COMPLETED(AchievementCategory.FOLLOWING,"Paldexpert", R.drawable.ic_badge_fan4, AchievementMilestones.MILESTONE4), //obtained when you follow the fourth milestone number

    BEAUTY_AND_THE_PAL(AchievementCategory.FOLLOWER,"the beauty and the pal", R.drawable.ic_badge_pal1, AchievementMilestones.MILESTONE1), //obtained when you are followed by the first milestone number
    CRYPTO_PAL(AchievementCategory.FOLLOWER,"cryptoPal", R.drawable.ic_badge_pal2, AchievementMilestones.MILESTONE2), //obtained when you are followed by the second milestone number
    MASTER_OF_CATS(AchievementCategory.FOLLOWER,"master of cats", R.drawable.ic_badge_pal3, AchievementMilestones.MILESTONE3), //obtained when you are followed by the third milestone number
    ULTIMATE_PAL(AchievementCategory.FOLLOWER,"verified user", R.drawable.ic_badge_pal4, AchievementMilestones.MILESTONE4), //obtained when you are followed by the fourth milestone number

    NON_SPEC(AchievementCategory.OTHER,"what are you searching there ?", R.drawable.not_found, AchievementMilestones.NOT_FOUND); //used when the Achievement is not in this list

    companion object {
        fun from(type: String?): Achievement = values().find{it.aName == type} ?: NON_SPEC
    }
}

enum class AchievementCategory {
    FOLLOWER,
    FOLLOWING,
    OTHER
}

/**
 * Milestone numbers for achievement levels
 */
object AchievementMilestones {
    const val NOT_FOUND = -1
    const val MILESTONE1 = 1
    const val MILESTONE2 = 5
    const val MILESTONE3 = 10
    const val MILESTONE4 = 25

    /**
     * To change the triggering milestone condition
     */
    fun followCountAdapt(milestone: Int) = milestone - 1
}