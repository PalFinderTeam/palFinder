package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.R

/**
 * enum of all the badges and achievements you can get in palFinder
 */
enum class Achievement(val string: String, val imageID : Int) {
    DEVELOPER("dev", R.drawable.icon_beer), //for the palfinder team

    PAL_FINDER("pal finder", R.drawable.icon_bowling), //obtained when you follow 5 pals
    PAL_MINER("pal miner", R.drawable.icon_football), //obtained when you follow 10 pals
    PAL_TRACKER("pal tracker", R.drawable.icon_group), //obtained when you follow 30 pals
    PALDEX_COMPLETED("paldex", R.drawable.icon_treadmill), //obtained when you follow 100 pals

    BEAUTY_AND_THE_PAL("the beauty and the pal", R.drawable.ic_baseline_alternate_email_24), //obtained when you are followed by 5 pals
    CRYPTOPAL("cryptoPal", R.drawable.ic_baseline_cake_24), //obtained when you are followed by 10 pals
    MASTER_OF_CATS("master of cats", R.drawable.ic_baseline_people_alt_24), //obtained when you are followed by 30 pals
    VERIFIED("verified user", R.drawable.ic_checkmark), //obtained when you are followed by 100 pals

    NON_SPEC("what are you searching there ?", R.drawable.not_found); //used when the Achievement is not in this list
    companion object {
        fun from(type: String?): Achievement = values().find{it.string == type} ?: NON_SPEC
    }
}