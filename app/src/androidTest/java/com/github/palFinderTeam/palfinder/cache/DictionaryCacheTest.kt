package com.github.palFinderTeam.palfinder.cache

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.github.palFinderTeam.palfinder.chat.ChatService
import com.github.palFinderTeam.palfinder.meetups.activities.MeetupListActivity
import com.github.palFinderTeam.palfinder.profile.ProfileService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun storeAndReadDictionary() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        val intent = Intent(context, MeetupListActivity::class.java)
        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use {
            val dummy = Dummy("dummy", 0)
            val cache = DictionaryCache("test", Dummy::class.java, false, context)

            cache.delete("dummy")
            assertThat(cache.contains("dummy"), `is`(false))

            cache.store("dummy", dummy)
            assertThat(cache.contains("dummy"), `is`(true))

            val result = cache.get("dummy")
            val res2 = cache.getAll()

            assertThat(res2.size,  `is`(1))
            assertThat(result.field1, `is`(dummy.field1))
            assertThat(result.field2, `is`(dummy.field2))

            cache.delete("dummy")
            assertThat(cache.contains("dummy"), `is`(false))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun evictTest() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        val intent = Intent(context, MeetupListActivity::class.java)
        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use {
            val dummy = Dummy("dummy", 0)
            val cache = DictionaryCache("test", Dummy::class.java, false, context)

            cache.store("dummy", dummy)
            cache.evict { true }

            assertThat(cache.contains("dummy"), `is`(false))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun storeAndReadFile() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        val intent = Intent(context, MeetupListActivity::class.java)
        val scenario = ActivityScenario.launch<MeetupListActivity>(intent)
        scenario.use {
            val dummy = Dummy("dummy", 0)
            val cache = FileCache("test", Dummy::class.java, false, context)

            cache.delete()
            assertThat(cache.exist(), `is`(false))

            cache.store(dummy)
            assertThat(cache.exist(), `is`(true))

            val result = cache.get()

            assertThat(result.field1, `is`(dummy.field1))
            assertThat(result.field2, `is`(dummy.field2))

            cache.delete()
            assertThat(cache.exist(), `is`(false))
        }
    }
    data class Dummy(val field1: String, val field2: Int)
}