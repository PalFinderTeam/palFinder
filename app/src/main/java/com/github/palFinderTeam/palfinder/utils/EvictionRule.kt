package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import com.github.palFinderTeam.palfinder.utils.time.isBefore
import java.io.File

fun evictAfterXMinutes(delta: Int, time: TimeService): (File)->Boolean =
{
    val expirationDate = time.now()
    expirationDate.add(Calendar.MINUTE, -delta)

    val date = time.now()
    date.timeInMillis = it.lastModified()

    date.isBefore(expirationDate)
}