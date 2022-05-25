package com.github.palFinderTeam.palfinder.meetups.activities

/**
 * simple enum class used to pass parameters to the meetup fetch and retrieve only those matching conditions
 */
enum class ShowParam(val param: String) {
    ALL("all"),
    ONLY_JOINED("joined"),
    PAL_PARTICIPATING("pals are participating"),
    PAL_CREATOR("pal has created it");
    companion object{
        fun from(type: String?): ShowParam = ShowParam.values().find { it.param == type } ?: ALL
    }
}