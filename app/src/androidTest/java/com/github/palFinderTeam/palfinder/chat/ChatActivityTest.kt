package com.github.palFinderTeam.palfinder.chat

import android.content.Intent
import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.activities.RecyclerViewMatcher
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

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var chatService: ChatService

    @Before
    fun setup() {
        hiltRule.inject()

        val date1 = Calendar.getInstance()
        date1.set(2022, 2, 8)
        val date2 = Calendar.getInstance()
        date2.set(2022, 1, 6)

        chat1 = "chat1"

        messages1 = listOf(ChatMessage(date1,"dummy","message 1", false),
                        ChatMessage(date2,"dummy","message 2", false))
    }

    @Test
    fun testDisplayActivities() = runTest {
        messages1.forEach { chatService.postMessage(chat1, it) }
        val intent = Intent(getApplicationContext(), ChatActivity::class.java)

        val scenario = ActivityScenario.launch<ChatActivity>(intent)
        scenario.use {
            onView(
                RecyclerViewMatcher(R.id.chat_list).atPositionOnView(0,
                    R.id.msg_in_text
                ))
                .check(matches(withText(messages1[1].content)))
        }
    }
}