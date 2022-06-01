package com.github.palFinderTeam.palfinder.notification

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.UIMockMeetUpRepositoryModule
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.chat.ChatMessage
import com.github.palFinderTeam.palfinder.chat.ChatService
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.CriterionGender
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.UIMockTimeServiceModule
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.thread

@ExperimentalCoroutinesApi
@HiltAndroidTest
class NotificationTest {
    private val uiDevice by lazy { UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()) }
    private val timeout = 10000L

    private lateinit var meetUp: MeetUp
    private lateinit var user1: ProfileUser
    private lateinit var user2: ProfileUser

    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(MainNavActivity::class.java)

    lateinit var notificationService: NotificationService

    @Rule
    @JvmField
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var meetUpRepository: MeetUpRepository

    @Inject
    lateinit var contextService: ContextService

    @Inject
    lateinit var profileRepository: ProfileService

    @Inject
    lateinit var chatService: ChatService

    @Inject
    lateinit var timeService: TimeService

    @Before
    fun setup() {
        hiltRule.inject()
        val context: Context = ApplicationProvider.getApplicationContext()
        DictionaryCache.clearAllTempCaches(context)

        user1 = ProfileUser(
            "0", "Michel", "Jordan", "Surimi", Calendar.getInstance(),
            ImageInstance(""), following = listOf("userId2")
        )
        user2 = ProfileUser(
            "1", "Michel", "Jordan", "Surimi", Calendar.getInstance(),
            ImageInstance(""), following = listOf("userId2")
        )

        val date1 = Calendar.getInstance().apply { time = Date(0) }
        val date2 = Calendar.getInstance().apply { time = Date(10) }
        val date3 = Calendar.getInstance().apply { time = Date(2) }

        (timeService as UIMockTimeServiceModule.UIMockTimeService).setDate(date3)
        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).timeService = timeService

        meetUp = MeetUp(
            "dummy",
            "0",
            null,
            "dummy",
            "dummy",
            date1,
            date2,
            Location(0.0, 0.0),
            setOf(Category.DRINKING),
            true,
            3,
            listOf("0"),
            Pair(null, null),
            CriterionGender.ALL
        )

        notificationService = NotificationService(contextService, timeService, meetUpRepository, profileRepository, chatService)
    }


    @Test
    fun postString() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()

        val expectedTitle = "title"
        val expectedContent = "content"

        val handler = NotificationHandler(context)
        handler.post(expectedTitle, expectedContent, R.drawable.icon_beer)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(expectedTitle)), timeout)
        val title: UiObject2 = uiDevice.findObject(By.text(expectedTitle))
        val text: UiObject2 = uiDevice.findObject(By.textStartsWith(expectedContent))
        assertEquals(expectedTitle, title.text)
        assertTrue(text.text.startsWith(expectedContent))
        uiDevice.findObject(By.textStartsWith("Clear all")).click()
    }

    @Test
    fun postID() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()

        val expectedTitle = "title"
        val expectedContent = "content"

        val handler = NotificationHandler(context)
        handler.post(R.string.testNotifTitle, R.string.testNotifContent, R.drawable.icon_beer)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(expectedTitle)), timeout)
        val title: UiObject2 = uiDevice.findObject(By.text(expectedTitle))
        val text: UiObject2 = uiDevice.findObject(By.textStartsWith(expectedContent))
        assertEquals(expectedTitle, title.text)
        assertTrue(text.text.startsWith(expectedContent))
        uiDevice.findObject(By.textStartsWith("Clear all")).click()

        val intent = Intent(context, MeetUpView::class.java)
        handler.post(R.string.testNotifTitle,R.string.testNotifContent, R.drawable.icon_beer, intent)
    }

    @Test
    fun cachedNotification() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()

        val handler = NotificationHandler(context)
        handler.schedule(
            Calendar.getInstance(),
            R.string.testNotifTitle,
            R.string.testNotifContent,
            R.drawable.icon_beer
        )
    }

    @Test
    fun followerWorks() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        val date1 = Calendar.getInstance().apply { time = Date(0) }
        val userId = profileRepository.create(user1)
        val userId2 = profileRepository.create(user2)

        uiDevice.openNotification()

        // Test Follower
        profileRepository.edit(userId2!!, "following", listOf(userId))
        profileRepository.edit(userId!!, "followed", listOf(userId2))

        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(
            userId
        )
        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).loggedUserID = userId

        notificationService.action()

        uiDevice.wait(Until.hasObject(By.textStartsWith("Clear all")), timeout)


        val newFollowerNotif: UiObject2? =
            uiDevice.findObject(By.text(context.getString(R.string.following_title)))
        assertTrue(newFollowerNotif != null)

        uiDevice.findObject(By.textStartsWith("Clear all")).click()
    }
    @Test
    fun meetupWorks() = runTest {
        val date1 = Calendar.getInstance().apply { time = Date(-1) }
        val userId = profileRepository.create(user1)!!
        val userId2 = profileRepository.create(user2)!!

        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(
            userId
        )
        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).loggedUserID = userId

        // Test MeetUp
        val id = meetUpRepository.create(meetUp.copy(creatorId = userId))
        meetUpRepository.joinMeetUp(id!!, userId, date1, profileRepository.fetch(userId)!!)
        (timeService as UIMockTimeServiceModule.UIMockTimeService).setDate(date1)

        uiDevice.openNotification()
        notificationService.action()
        uiDevice.wait(Until.hasObject(By.textStartsWith("Clear all")), timeout)

        val meetupNotif: UiObject2? = uiDevice.findObject(By.text(meetUp.name))
        assertTrue(meetupNotif != null)

        uiDevice.findObject(By.textStartsWith("Clear all")).click()

    }
    @Test
    fun chatWorks() = runTest {
        val date1 = Calendar.getInstance().apply { time = Date(-1) }
        val userId = profileRepository.create(user1)!!
        val userId2 = profileRepository.create(user2)!!

        (profileRepository as UIMockProfileServiceModule.UIMockProfileService).setLoggedInUserID(
            userId
        )
        (meetUpRepository as UIMockMeetUpRepositoryModule.UIMockRepository).loggedUserID = userId

        val id = meetUpRepository.create(meetUp.copy(creatorId = userId))
        meetUpRepository.joinMeetUp(id!!, userId, date1, profileRepository.fetch(userId)!!)
        (timeService as UIMockTimeServiceModule.UIMockTimeService).setDate(date1)

        // Test Chat
        chatService.postMessage(id, ChatMessage(date1, userId2, "hello world"))

        uiDevice.openNotification()
        notificationService.action()
        uiDevice.wait(Until.hasObject(By.textStartsWith("Clear all")), timeout)
        val chatNotif: UiObject2? = uiDevice.findObject(By.text("hello world"))
        assertTrue(chatNotif != null)

        uiDevice.findObject(By.textStartsWith("Clear all")).click()
    }
}