package com.github.palFinderTeam.palfinder.chat

import android.content.Intent
import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.init
import androidx.test.espresso.intent.Intents.release
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.github.palFinderTeam.palfinder.ProfileActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.activities.RecyclerViewMatcher
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.UIMockProfileServiceModule
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class ChatActivityTest {
    private lateinit var messages1: List<ChatMessage>
    private lateinit var chat1: String
    private lateinit var profile1: ProfileUser
    private lateinit var profile2: ProfileUser

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var chatService: ChatService
    @Inject
    lateinit var profilService: ProfileService

    @Before
    fun setup() {
        hiltRule.inject()

        val date1 = Calendar.getInstance()
        date1.set(2022, 2, 8)
        val date2 = Calendar.getInstance()
        date2.set(2022, 1, 6)
        val date3 = Calendar.getInstance()
        date3.set(2022, 1, 10)

        chat1 = "chat1"

        profile1 = ProfileUser(
            "dummy",
            "Mike",
            "ljor",
            "dan",
            date1,
            ImageInstance("imageURL"),
            "Hi I'm Mike."
        )
        profile2 = ProfileUser(
            "dummy2",
            "Mike2",
            "ljor",
            "dan",
            date1,
            ImageInstance("imageURL"),
            "Hi I'm Mike."
        )
        val profileMock = (profilService as UIMockProfileServiceModule.UIMockProfileService)
        val user1 = profileMock.syncCreateProfile(profile1)
        val user2 = profileMock.syncCreateProfile(profile2)
        profileMock.setLoggedInUserID(user2)

        messages1 = listOf(ChatMessage(date1,user1!!,"message 1", false),
                        ChatMessage(date2,user2!!,"message 2", false),
                        ChatMessage(date3,"no","message 3", false))

        (chatService as UIMockChatServiceModule.UIMockChatService).clear()
    }

    @Test
    fun testDisplayActivities() = runTest {
        messages1.forEach { chatService.postMessage(chat1, it) }
        val intent = Intent(getApplicationContext(), ChatActivity::class.java).apply {
            putExtra(CHAT, chat1)
        }

        val scenario = ActivityScenario.launch<ChatActivity>(intent)
        scenario.use {
            onView(
                RecyclerViewMatcher(R.id.chat_list).atPositionOnView(0,
                    R.id.msg_in_text
                ))
                .check(matches(withText(messages1[1].content)))
            onView(
                RecyclerViewMatcher(R.id.chat_list).atPositionOnView(0,
                    R.id.msg_in_sender_name
                ))
                .check(matches(withText(profile2.username)))
            onView(
                RecyclerViewMatcher(R.id.chat_list).atPositionOnView(1,
                    R.id.msg_in_sender_name
                ))
                .check(matches(withText(R.string.placeholder_name)))
        }
    }

    @Test
    fun testSendMessage() = runTest {
        val intent = Intent(getApplicationContext(), ChatActivity::class.java).apply {
            putExtra(CHAT, chat1)
        }

        val scenario = ActivityScenario.launch<ChatActivity>(intent)
        scenario.use {
            onView(withId(R.id.et_ChatMessageEdit)).perform(typeText("dummy 1234"))
            onView(withId(R.id.bt_SendMessage)).perform(click())
            onView(withId(R.id.et_ChatMessageEdit)).check(matches(withText("")))
        }
    }

    @Test
    fun testClickProfile() = runTest {
        chatService.postMessage(chat1, messages1[2])

        val intent = Intent(getApplicationContext(), ChatActivity::class.java).apply {
            putExtra(CHAT, chat1)
        }

        val scenario = ActivityScenario.launch<ChatActivity>(intent)
        init()
        scenario.use {
            onView(
                withId(R.id.msg_send_picture)
            ).check(matches(isClickable()))
            .perform(click())
        }
        Intents.intended(IntentMatchers.hasComponent(ProfileActivity::class.java.name))
        release()
    }
}