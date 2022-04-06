package com.github.palFinderTeam.palfinder.cache

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.github.palFinderTeam.palfinder.chat.ChatService
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.github.palFinderTeam.palfinder.profile.ProfileService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class DictionaryCacheTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var chatService: ChatService
    @Inject
    lateinit var profilService: ProfileService

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun storeAndRead() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        val intent = Intent(context, MeetupListActivity::class.java)
        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use {
            val date1 = Calendar.getInstance()

            val dummy = Dummy("dummy", 0)
            val cache = DictionaryCache(context,"test", Dummy::class.java)

            cache.delete("dummy")
            assertThat(cache.contains("dummy"), `is`(false))

            cache.store("dummy", dummy)
            assertThat(cache.contains("dummy"), `is`(true))

            val result = cache.get("dummy")

            assertThat(result.field1, `is`(dummy.field1))
            assertThat(result.field2, `is`(dummy.field2))

            cache.delete("dummy")
            assertThat(cache.contains("dummy"), `is`(false))
        }
    }
    data class Dummy(val field1: String, val field2: Int)
}