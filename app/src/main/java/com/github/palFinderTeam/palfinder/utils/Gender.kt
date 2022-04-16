package com.github.palFinderTeam.palfinder.utils

enum class Gender(val stringGender: String) {
    FEMALE("Female"),
    MALE("Male"),
    OTHER("other"),
    NON_SPEC("null");
    companion object{
        fun from(type: String?): Gender = Gender.values().find { it.stringGender == type } ?: NON_SPEC
    }
}