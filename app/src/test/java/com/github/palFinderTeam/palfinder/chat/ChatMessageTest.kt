package com.github.palFinderTeam.palfinder.chat

import android.icu.util.Calendar
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*

class ChatMessageTest {
    private lateinit var chatMsg: ChatMessage

    @Before
    fun setup() {
        val sentAt = Mockito.mock(Calendar::class.java)
        Mockito.`when`(sentAt.time).thenReturn(Date(420))

        chatMsg = ChatMessage(sentAt, "userId", "content of msg")
    }

    @Test
    fun `to firebase document conversion keeps right values`() {
        val firebaseDoc = chatMsg.toFirebaseDocument()
        assertThat(firebaseDoc["sentAt"], `is`(Date(420)))
        assertThat(firebaseDoc["sentBy"], `is`(chatMsg.sentBy))
        assertThat(firebaseDoc["content"], `is`(chatMsg.content))
        assertThat(firebaseDoc["isEdited"], `is`(chatMsg.isEdited))
    }
}