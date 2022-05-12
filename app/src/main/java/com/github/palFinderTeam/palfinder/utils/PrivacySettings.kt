package com.github.palFinderTeam.palfinder.utils

enum class PrivacySettings(val stringPrivacysettings: String) {
    PRIVATE("private"),
    PUBLIC("public"),
    FRIENDS("friends only");
    companion object{
        fun from(type: String?): PrivacySettings = values().find { it.stringPrivacysettings == type } ?: PRIVATE
    }
}