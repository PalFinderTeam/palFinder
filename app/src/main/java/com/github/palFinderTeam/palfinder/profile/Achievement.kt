package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.R



/**
 * enum of all the badges and achievements you can get in palFinder
 */
enum class Achievement(val cat: AchievementCategory, val aName: String, val imageID : Int, val milestone: Int) {
    DEVELOPER(AchievementCategory.OTHER, "Dev", R.drawable.ic_badge_dev, AchievementMilestones.NOT_FOUND), //for the palfinder team
    VERIFIED(AchievementCategory.OTHER,"Verified user", R.drawable.ic_checkmark, AchievementMilestones.NOT_FOUND), //obtained when you are trustworthy

    PAL_FINDER(AchievementCategory.FOLLOWER,"Pal finder", R.drawable.ic_badge_fan1, AchievementMilestones.MILESTONE1), //obtained when you follow the first milestone number
    PAL_MINER(AchievementCategory.FOLLOWER,"Pal miner", R.drawable.ic_badge_fan2, AchievementMilestones.MILESTONE2), //obtained when you follow the second milestone number
    PAL_TRACKER(AchievementCategory.FOLLOWER,"Pal tracker", R.drawable.ic_badge_fan3, AchievementMilestones.MILESTONE3), //obtained when you follow the third milestone number
    PALDEX_COMPLETED(AchievementCategory.FOLLOWER,"Palexpo", R.drawable.ic_badge_fan4, AchievementMilestones.MILESTONE4), //obtained when you follow the fourth milestone number

    BEAUTY_AND_THE_PAL(AchievementCategory.FOLLOWED,"the beauty and the pal", R.drawable.ic_badge_pal1, AchievementMilestones.MILESTONE1), //obtained when you are followed by the first milestone number
    CRYPTO_PAL(AchievementCategory.FOLLOWED,"cryptoPal", R.drawable.ic_badge_pal2, AchievementMilestones.MILESTONE2), //obtained when you are followed by the second milestone number
    MASTER_OF_CATS(AchievementCategory.FOLLOWED,"master of cats", R.drawable.ic_badge_pal3, AchievementMilestones.MILESTONE3), //obtained when you are followed by the third milestone number
    ULTIMATE_PAL(AchievementCategory.FOLLOWED,"paladin", R.drawable.ic_badge_pal4, AchievementMilestones.MILESTONE4), //obtained when you are followed by the fourth milestone number

    NON_SPEC(AchievementCategory.NO_ACH,"what are you searching there ?", R.drawable.not_found, AchievementMilestones.NOT_FOUND); //used when the Achievement is not in this list

    companion object {
        fun from(type: String?): Achievement = values().find{it.aName == type} ?: NON_SPEC
    }
}

enum class AchievementCategory {
    FOLLOWED,
    FOLLOWER,
    OTHER,
    NO_ACH
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

}