package com.github.palFinderTeam.palfinder.meetups.activities

import android.icu.util.Calendar
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MockMeetUpRepository
import com.github.palFinderTeam.palfinder.profile.MockProfileService
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.MockTimeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class MeetUpViewViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MeetUpViewViewModel
    private lateinit var meetUpRepository: MockMeetUpRepository
    private lateinit var profileService: MockProfileService
    private var timeService: MockTimeService = MockTimeService()
    private lateinit var testStartDate: Calendar
    private lateinit var testEndDate: Calendar

    @Before
    fun setup() {
        testStartDate = Mockito.mock(Calendar::class.java)
        testEndDate = Mockito.mock(Calendar::class.java)
        Mockito.`when`(testStartDate.timeInMillis).thenReturn(69)
        Mockito.`when`(testEndDate.timeInMillis).thenReturn(420)
        meetUpRepository = MockMeetUpRepository()
        meetUpRepository.clearDB()
        profileService = MockProfileService()
        profileService.db.clear()
        viewModel = MeetUpViewViewModel(meetUpRepository, profileService, timeService)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun cleanUp() {
        meetUpRepository.clearDB()
    }

    @Test
    fun `display infos from valid meetUp`() = runTest {
        val dummyMeetUp = MeetUp(
            "",
            "user",
            "icon",
            "name",
            "description",
            testStartDate,
            testEndDate,
            Location(1.0, 2.0),
            setOf(Category.CINEMA),
            true,
            2,
            mutableListOf()
        )

        val id = meetUpRepository.createMeetUp(dummyMeetUp)

        assertThat(id, notNullValue())
        viewModel.loadMeetUp(id!!)

        assertThat(viewModel.meetUp.value, `is`(dummyMeetUp.copy(uuid = id)))
    }
}