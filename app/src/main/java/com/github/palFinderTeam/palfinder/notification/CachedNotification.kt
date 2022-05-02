package com.github.palFinderTeam.palfinder.notification

import android.icu.util.Calendar

data class CachedNotification(
    val uuid: String,
    val time: Calendar,
    val title: String,
    val content: String,
    val icon: Int
)