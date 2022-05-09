package com.github.palFinderTeam.palfinder.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.chat.ChatService
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NotificationTest {
    private val uiDevice by lazy { UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()) }
    private val timeout = 10000L

    private lateinit var notificationService: NotificationService

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var meetUpRepository: MeetUpRepository

    @Inject
    lateinit var profileRepository: ProfileService

    @Inject
    lateinit var chatService: ChatService

    @Inject
    lateinit var timeService: TimeService

    @Before
    fun setup(){
        hiltRule.inject()
        notificationService = NotificationService(timeService, meetUpRepository, profileRepository, chatService)
        val context: Context = ApplicationProvider.getApplicationContext()
        DictionaryCache.clearAllTempCaches(context)
    }


    @Test
    fun postString() {
        val context: Context = ApplicationProvider.getApplicationContext()

        val expectedTitle = "title"
        val expectedContent = "content"

        val handler = NotificationHandler(context)
        handler.post(expectedTitle,expectedContent, R.drawable.icon_beer)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(expectedTitle)), timeout)
        val title: UiObject2 = uiDevice.findObject(By.text(expectedTitle))
        val text: UiObject2 = uiDevice.findObject(By.textStartsWith(expectedContent))
        assertEquals(expectedTitle, title.text)
        assertTrue(text.text.startsWith(expectedContent))
        uiDevice.findObject(By.textStartsWith("Clear all")).click()
    }

    @Test
    fun postID() {
        val context: Context = ApplicationProvider.getApplicationContext()

        val expectedTitle = "title"
        val expectedContent = "content"

        val handler = NotificationHandler(context)
        handler.post(R.string.testNotifTitle,R.string.testNotifContent, R.drawable.icon_beer)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(expectedTitle)), timeout)
        val title: UiObject2 = uiDevice.findObject(By.text(expectedTitle))
        val text: UiObject2 = uiDevice.findObject(By.textStartsWith(expectedContent))
        assertEquals(expectedTitle, title.text)
        assertTrue(text.text.startsWith(expectedContent))
        uiDevice.findObject(By.textStartsWith("Clear all")).click()
    }
/*
    @Test
    fun scheduleID() {
        val context: Context = ApplicationProvider.getApplicationContext()

        val expectedTitle = "title"
        val expectedContent = "content"

        val handler = NotificationHandler(context)
        handler.schedule(Calendar.getInstance(), R.string.testNotifTitle,R.string.testNotifContent, R.drawable.icon_beer)

        notificationService.action()
    }*/
}