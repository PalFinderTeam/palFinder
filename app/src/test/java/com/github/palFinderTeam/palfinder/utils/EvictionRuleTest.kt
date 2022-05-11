package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mockito
import java.io.File

class EvictionRuleTest {

    @Test
    fun test(){
        val file = Mockito.mock(File::class.java)
        Mockito.`when`(file.lastModified()).thenReturn(0)
        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(100000)
        val timeService = MockTimeService()
        timeService.setDate(date1)
        assertThat(evictAfterXMinutes(1, timeService)(file), `is`(true))
    }
}