package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import java.util.*

@RunWith(JUnit4::class)
class ProfileUserTest {

    private lateinit var profile: ProfileUser

    @Before
    fun setup() {
        val joinDate = Mockito.mock(Calendar::class.java)
        Mockito.`when`(joinDate.time).thenReturn(Date(42069))
        profile = ProfileUser("1234","leCat","The","Cat",joinDate, ImageInstance("whateverURL"))
    }

    @Test
    fun `profile convert to the right firebase representation`() {
        profile.toFirestoreData().let {
            assertThat(it["name"], `is`(profile.name))
            assertThat(it["surname"], `is`(profile.surname))
            assertThat(it["username"], `is`(profile.username))
            assertThat(it["join_date"], `is`(profile.joinDate.time))
            assertThat(it["picture"], `is`(profile.pfp.imgURL))
        }
    }
}