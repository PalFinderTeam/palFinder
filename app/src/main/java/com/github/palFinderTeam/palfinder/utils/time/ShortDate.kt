package com.github.palFinderTeam.palfinder.utils.time

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.R
import java.util.*

object ShortDate {
    /**
     * Return a short string version of the date
     *
     * @param context
     * @param calendar Date to convert
     * @param now Now
     */
    @SuppressLint("SimpleDateFormat")
    fun format(context: Context, calendar: Calendar, now: Calendar): String {
        return if ( // Same day
            now.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH) &&
            now.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
            now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
        ) {
            SimpleDateFormat(context.getString(R.string.date_very_short_format))
                .format(Date(calendar.timeInMillis))
        } else if (now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {  // Same year
            SimpleDateFormat(context.getString(R.string.date_short_format))
                .format(Date(calendar.timeInMillis))
        } else {
            SimpleDateFormat(context.getString(R.string.date_medium_format))
                .format(Date(calendar.timeInMillis))
        }
    }
}