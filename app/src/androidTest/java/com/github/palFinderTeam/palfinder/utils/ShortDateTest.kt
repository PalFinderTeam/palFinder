package com.github.palFinderTeam.palfinder.utils

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.test.core.app.ApplicationProvider
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.time.ShortDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.util.*

class ShortDateTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testVeryShort() = runTest{
        val context: Context = ApplicationProvider.getApplicationContext()

        val calendar = Calendar.getInstance()
        calendar.set(2022,0,0,0,0)

        val now = Calendar.getInstance()
        now.set(2022,0,0,0,0)

        val result = ShortDate.format(context, calendar, now)
        val expected = SimpleDateFormat(context.getString(R.string.date_very_short_format)).format(Date(calendar.timeInMillis))

        assertThat(result, `is`(expected))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testShort() = runTest{
        val context: Context = ApplicationProvider.getApplicationContext()

        val calendar = Calendar.getInstance()
        calendar.set(2022,0,1,0,0)

        val now = Calendar.getInstance()
        now.set(2022,0,2,0,0)

        val result = ShortDate.format(context, calendar, now)
        val expected = SimpleDateFormat(context.getString(R.string.date_short_format)).format(Date(calendar.timeInMillis))

        assertThat(result, `is`(expected))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testMedium() = runTest{
        val context: Context = ApplicationProvider.getApplicationContext()

        val calendar = Calendar.getInstance()
        calendar.set(2022,0,0,0,0)

        val now = Calendar.getInstance()
        now.set(2023,0,0,0,0)

        val result = ShortDate.format(context, calendar, now)
        val expected = SimpleDateFormat(context.getString(R.string.date_medium_format)).format(Date(calendar.timeInMillis))

        assertThat(result, `is`(expected))
    }
}