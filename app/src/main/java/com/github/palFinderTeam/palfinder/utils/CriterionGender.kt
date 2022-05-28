package com.github.palFinderTeam.palfinder.utils

enum class CriterionGender(val genderName: String) {
    FEMALE("Female"),
    MALE("Male"),
    ALL("All");

    companion object{
        fun from(type: String?): CriterionGender = values().find { it.genderName == type } ?: ALL
    }
}