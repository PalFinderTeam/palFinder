package com.github.palFinderTeam.palfinder.utils

enum class PrivacySettings(val stringPrivacysettings: String) {
    PUBLIC("public"),
    PRIVATE("private"),
    FRIENDS("friends only");
    companion object{
        fun from(type: String?): PrivacySettings = values().find { it.stringPrivacysettings == type } ?: PRIVATE
    }
}