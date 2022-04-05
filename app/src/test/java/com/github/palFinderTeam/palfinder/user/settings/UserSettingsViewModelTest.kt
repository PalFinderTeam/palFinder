package com.github.palFinderTeam.palfinder.user.settings

import android.icu.util.Calendar
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.palFinderTeam.palfinder.profile.MockProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsViewModel.Companion.FIELDS_LENGTH
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsViewModel.Companion.FIELD_NAME
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsViewModel.Companion.FIELD_USERNAME
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsViewModel.Companion.MSG_FIELD_TOO_LONG
import com.github.palFinderTeam.palfinder.user.settings.UserSettingsViewModel.Companion.MSG_FIELD_TOO_SHORT
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class UserSettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UserSettingsViewModel
    private lateinit var profileUserService: MockProfileService
    private lateinit var joinDate: Calendar
    private lateinit var birthday: Calendar
    private lateinit var imgInst: ImageInstance

    private lateinit var user: ProfileUser

    @Before
    fun setup() {
        // Calendar mockup
        joinDate = Mockito.mock(Calendar::class.java)
        Mockito.`when`(joinDate.time).thenReturn(Date(1))
        birthday = Mockito.mock(Calendar::class.java)
        Mockito.`when`(birthday.time).thenReturn(Date(2))

        imgInst = Mockito.mock(ImageInstance::class.java)

        // User
        user = ProfileUser(
            "0",
            "cato",
            "taco",
            "maco",
            joinDate,
            imgInst,
            "The cato is backo...",
            birthday
        )

        // Create viewModel with mock profile service
        profileUserService = MockProfileService()
        profileUserService.db.clear()
        viewModel = UserSettingsViewModel(profileUserService)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun destroy() {
        profileUserService.db.clear()
    }

    @Test
    fun `fill with default values exposes those values`() {
        viewModel.resetFieldsWithDefaults()
        assertThat(viewModel.userBio.value, `is`(""))
        assertThat(viewModel.username.value, `is`(""))
        assertThat(viewModel.name.value, `is`(""))
    }

    @Test
    fun `fill with profile values exposes those values`(){
        viewModel.loadUserInfo(user)
        assertThat(viewModel.userBio.value, `is`(user.description))
        assertThat(viewModel.username.value, `is`(user.username))
        assertThat(viewModel.name.value, `is`(user.name))
        assertThat(viewModel.birthday.value!!.time.toString(), `is`(user.birthday!!.time.toString()))
    }

    @Test
    fun `fill with db profile values exposes those values`() = runTest{
        viewModel.loggedUID = profileUserService.createProfile(user).toString()
        viewModel.loadUserInfo()

        assertThat(viewModel.userBio.value, `is`(user.description))
        assertThat(viewModel.username.value, `is`(user.username))
        assertThat(viewModel.name.value, `is`(user.name))
        assertThat(viewModel.birthday.value!!.time.toString(), `is`(user.birthday!!.time.toString()))
    }

    @Test
    fun `fill with undefined user in db returns empty fields`() = runTest{
        viewModel.loggedUID = "0"
        viewModel.loadUserInfo()

        assertThat(viewModel.userBio.value, `is`(""))
        assertThat(viewModel.username.value, `is`(""))
        assertThat(viewModel.name.value, `is`(""))
        assertThat(viewModel.birthday.value, nullValue())
    }

    @Test
    fun `setters do work and get exposed`() {
        viewModel.setUsername("name")
        viewModel.setBio("a bio")
        viewModel.setName("Jean")
        viewModel.setSurname("Michel")
        viewModel.setBirthday(birthday)
        assertThat(viewModel.username.value, `is`("name"))
        assertThat(viewModel.userBio.value, `is`("a bio"))
        assertThat(viewModel.name.value, `is`("Jean"))
        assertThat(viewModel.surname.value, `is`("Michel"))
        assertThat(viewModel.birthday.value!!.time, `is`(birthday.time))
    }

    @Test
    fun `check all fields succeeds`() {
        user = ProfileUser(
            "1",
            "cato", // Check fully goes through
            "taco",
            "maco",
            joinDate,
            ImageInstance(""),
            "The cato is backo...",
            birthday // Test for no checks
        )

        viewModel.loadUserInfo(user)
        assertThat(viewModel.checkAllFields(), `is`(UserSettingsViewModel.MSG_NO_MSG))
    }

    @Test
    fun `check all fields username fails too short`() {
        user = ProfileUser(
            "1",
            "", // Fails too short
            "taco",
            "maco",
            joinDate,
            ImageInstance(""),
            "The cato is backo...",
            birthday
        )

        viewModel.loadUserInfo(user)
        assertThat(viewModel.checkAllFields(), `is`(
            String.format(MSG_FIELD_TOO_SHORT, FIELD_USERNAME)
        ))
    }

    @Test
    fun `check all fields name fails too long`() {
        user = ProfileUser(
            "1",
            "cattalio_le_callico",
            "tacooooooooooooooooooooooooooooooooooooooooooo", // Fails too long
            "maco",
            joinDate,
            ImageInstance(""),
            "The cato is backo...",
            birthday
        )

        viewModel.loadUserInfo(user)
        assertThat(viewModel.checkAllFields(), `is`(
            String.format(
                MSG_FIELD_TOO_LONG,
                FIELD_NAME,
                FIELDS_LENGTH[FIELD_NAME]!!.second,
                user.name.length
            )
        ))
    }

    @Test
    fun `check all fields bad username format`() {
        user = ProfileUser(
            "1",
            "cattalio-le-+++", // Fails bad format
            "wow",
            "maco",
            joinDate,
            ImageInstance(""),
            "The cato is backo...",
            birthday
        )

        viewModel.loadUserInfo(user)
        assertThat(viewModel.checkAllFields(), `is`(
            String.format(UserSettingsViewModel.MSG_USERNAME_BAD, user.username)
        ))
    }

    @Test
    fun `creating new user settings in DB is successful`() = runTest {
        viewModel.loadUserInfo(user)

        print(user.uuid)

        viewModel.loggedUID = user.uuid
        assertThat(profileUserService.doesUserIDExist(user.uuid), `is`(false))

        viewModel.saveValuesIntoDatabase()

        assertThat(viewModel.username.value, `is`(user.username))
    }

    @Test
    fun `update existing user in db successful`() = runTest {
        // Create an existing user in DB
        val userIdInDB = profileUserService.createProfile(user).toString()

        // Create update data
        val userUpdate = ProfileUser(
            userIdInDB,
            "cattalio-le-callico",
            "wow", // Fails too long
            "maco",
            joinDate,
            ImageInstance(""),
            "The cato is backo...",
            birthday
        )

        viewModel.loggedUID = userIdInDB
        viewModel.loadUserInfo(userUpdate)

        assertThat(profileUserService.doesUserIDExist(userIdInDB), `is`(true))
    }

}