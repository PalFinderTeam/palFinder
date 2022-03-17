package com.github.palFinderTeam.palfinder.meetups.activities

import android.icu.util.Calendar
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MockMeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.*
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
class MeetUpCreationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MeetUpCreationViewModel
    private lateinit var meetUpRepository: MockMeetUpRepository
    private lateinit var testStartDate: Calendar
    private lateinit var testEndDate: Calendar

    @Before
    fun setup() {

        testStartDate = Mockito.mock(Calendar::class.java)
        testEndDate = Mockito.mock(Calendar::class.java)
        Mockito.`when`(testStartDate.timeInMillis).thenReturn( 69)
        Mockito.`when`(testEndDate.timeInMillis).thenReturn( 420)
//        Mockito.`when`(testStartDate.isDeltaBefore(Mockito.any(), defaultTimeDelta)).thenReturn(true)
//        Mockito.`when`(testEndDate.isDeltaBefore(Mockito.any(), defaultTimeDelta)).thenReturn(false)

        meetUpRepository = MockMeetUpRepository()
        meetUpRepository.clearDB()
        viewModel = MeetUpCreationViewModel(meetUpRepository, testStartDate)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun cleanUp() {
        meetUpRepository.clearDB()
    }

    @Test
    fun `fill with default values exposes those values`() {
        viewModel.fillWithDefaultValues()
        assertThat(viewModel.capacity.value, `is`(1))
        assertThat(viewModel.description.value, `is`(""))
        assertThat(viewModel.name.value, `is`(""))
        assertThat(viewModel.tags.value?.isEmpty() ?: false, `is`(true))
    }

    @Test
    fun `setters do work and get exposed`() {
//        viewModel.setStartDate(testStartDate)
//        viewModel.setEndDate(testEndDate)
        viewModel.setCapacity(4)
        viewModel.setHasMaxCapacity(true)
        viewModel.setDescription("manger des bananes")
        viewModel.setName("Manger")
//        assertThat(viewModel.startDate.value?.timeInMillis ?: 0, `is`(testStartDate.timeInMillis))
//        assertThat(viewModel.endDate.value?.timeInMillis ?: 0, `is`(testEndDate.timeInMillis))
        assertThat(viewModel.capacity.value, `is`(4))
        assertThat(viewModel.hasMaxCapacity.value, `is`(true))
        assertThat(viewModel.description.value, `is`("manger des bananes"))
        assertThat(viewModel.name.value, `is`("Manger"))
    }

    @Test
    fun `fetch and display infos from database`() = runTest {
        val dummyMeetUp = MeetUp(
            "",
            ProfileUser("username", "whatever", "surname", testStartDate, ImageInstance("icons/demo_pfp.jpeg")),
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

        assertThat(viewModel.startDate.value?.timeInMillis ?: 0, `is`(testStartDate.timeInMillis))
//        assertThat(viewModel.endDate.value?.timeInMillis ?: 0, `is`(testEndDate.timeInMillis))
        assertThat(viewModel.capacity.value, `is`(2))
        assertThat(viewModel.hasMaxCapacity.value, `is`(true))
        assertThat(viewModel.description.value, `is`("description"))
        assertThat(viewModel.name.value, `is`("name"))

    }

    @Test
    fun `create new meetup insert in DB`() = runTest {
        viewModel.setCapacity(4)
        viewModel.setHasMaxCapacity(true)
        viewModel.setDescription("manger des bananes")
        viewModel.setName("Manger")
        viewModel.sendMeetUp()
        assertThat(viewModel.sendSuccess.value, `is`(true))
        assertThat(viewModel.getMeetUpId(), notNullValue())
        val meetUp = meetUpRepository.db[viewModel.getMeetUpId()]
        assertThat(meetUp, notNullValue())
        if (meetUp != null) {
            assertThat(meetUp.capacity, `is`(4))
            assertThat(viewModel.hasMaxCapacity.value, `is`(true))
            assertThat(viewModel.description.value, `is`("manger des bananes"))
            assertThat(viewModel.name.value, `is`("Manger"))
        }
    }
}