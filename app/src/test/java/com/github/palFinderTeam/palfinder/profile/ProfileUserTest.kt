package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.BIRTHDAY_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.BLOCKED_USERS
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.DESCRIPTION_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWED_BY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWING_PROFILES
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.GENDER
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.JOINED_MEETUPS_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.JOIN_DATE_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.NAME_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.PICTURE_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.SURNAME_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.USERNAME_KEY
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import java.util.*

@RunWith(JUnit4::class)
class ProfileUserTest {

    private lateinit var profile: ProfileUser

    @Before
    fun setup() {
        val joinDate = Mockito.mock(Calendar::class.java)
        Mockito.`when`(joinDate.time).thenReturn(Date(42069))
        val birthDay = Mockito.mock(Calendar::class.java)
        Mockito.`when`(birthDay.time).thenReturn(Date(42069))
        profile = ProfileUser(
            "1234",
            "leCat",
            "The",
            "Cat",
            joinDate,
            ImageInstance("whateverURL"),
            "My description.",
            birthDay,
            listOf("meetup1"),
            Gender.FEMALE,
            listOf("Michel"),
            listOf("Robert"),
            listOf("Antoane")
        )
    }

    @Test
    fun `profile convert to the right firebase representation`() {
        profile.toFirestoreData().let {
            assertThat(it[USERNAME_KEY], `is`(profile.username))
            assertThat(it[NAME_KEY], `is`(profile.name))
            assertThat(it[SURNAME_KEY], `is`(profile.surname))
            assertThat(it[PICTURE_KEY], `is`(profile.pfp.imgURL))
            assertThat(it[JOIN_DATE_KEY], `is`(profile.joinDate.time))
            assertThat(it[DESCRIPTION_KEY], `is`(profile.description))
            assertThat(it[BIRTHDAY_KEY], `is`(profile.birthday?.time))
            assertThat(it[JOINED_MEETUPS_KEY], `is`(profile.joinedMeetUps))
            assertThat(it[GENDER], `is`(profile.gender?.stringGender))
            assertThat(it[FOLLOWING_PROFILES], `is`(profile.following))
            assertThat(it[FOLLOWED_BY], `is`(profile.followed))
            assertThat(it[BLOCKED_USERS], `is`(profile.blockedUsers))
        }
    }
}