package com.github.palFinderTeam.palfinder.utils

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class EvictionRuleTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var timeService: TimeService

    @Before
    fun setup(){
        hiltRule.inject()
    }

    @Test
    fun test(){
        val file = Mockito.mock(File::class.java)
        Mockito.`when`(file.lastModified()).thenReturn(0)
        val date1 = Mockito.mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(100000)
        (timeService as UIMockTimeServiceModule.UIMockTimeService).setDate(date1)
        assertThat(evictAfterXMinutes(1, timeService)(file), `is`(true))
    }
}