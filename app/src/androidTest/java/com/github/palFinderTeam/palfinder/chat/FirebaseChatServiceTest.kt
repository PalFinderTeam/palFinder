package com.github.palFinderTeam.palfinder.chat

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.chat.ChatMessage.Companion.toChatMessage
import com.github.palFinderTeam.palfinder.chat.FirebaseChatService.Companion.CONVERSATION_COLL
import com.github.palFinderTeam.palfinder.chat.FirebaseChatService.Companion.MSG_COLL
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class FirebaseChatServiceTest {

    private lateinit var firebaseChatService: FirebaseChatService
    private lateinit var db: FirebaseFirestore
    private lateinit var message1: ChatMessage
    private lateinit var message2: ChatMessage
    private val groupId = "12345"

    @Before
    fun setUp() {
        // Connect db to local emulator
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)
            .build()
        db.firestoreSettings = settings

        firebaseChatService = FirebaseChatService(db)

        val date1 = Calendar.getInstance().apply { time = Date(0) }
        message1 = ChatMessage(
            date1,
            "michel",
            "Salut à tous moi c'est michel."
        )
        message2 = message1.copy(sentBy = "Gege", content = "Moi c'est gege.")
    }

    @Test
    fun postMessageAreInDb() = runTest {
        val id1 = firebaseChatService.postMessage(groupId, message1)
        val id2 = firebaseChatService.postMessage(groupId, message2)
        assertThat(id1, notNullValue())
        assertThat(id2, notNullValue())
        if (id1 != null && id2 != null) {
            val messagesInDb =
                db.collection(CONVERSATION_COLL).document(groupId).collection(MSG_COLL).get()
                    .await().documents.map { it.toChatMessage() }
            assertThat(messagesInDb, hasItems(message1, message2))
            // Make sure to clean for next tests
            db.collection(FirebaseMeetUpService.MEETUP_COLL).document(groupId).collection(MSG_COLL)
                .document(id1)
            db.collection(FirebaseMeetUpService.MEETUP_COLL).document(groupId).collection(MSG_COLL)
                .document(id2)
        }
    }

    @Test
    fun editMessageEditContentAndMarkAsEdited() = runTest {
        val id = firebaseChatService.postMessage(groupId, message1)
        assertThat(id, notNullValue())
        if (id != null) {
            firebaseChatService.editMessage(groupId, id, "Hello en fait je suis Jean Pierre.")
            val messagesInDb =
                db.collection(CONVERSATION_COLL).document(groupId).collection(MSG_COLL).document(id)
                    .get().await().toChatMessage()
            assertThat(
                messagesInDb,
                `is`(message1.copy(content = "Hello en fait je suis Jean Pierre.", isEdited = true))
            )
            // Make sure to clean for next tests
            db.collection(FirebaseMeetUpService.MEETUP_COLL).document(groupId).collection(MSG_COLL)
                .document(id)
        }
    }

    @Test
    fun fetchMessagesFetchRightInfo() = runTest {
        val id1 = firebaseChatService.postMessage(groupId, message1)
        val id2 = firebaseChatService.postMessage(groupId, message2)
        assertThat(id1, notNullValue())
        assertThat(id2, notNullValue())
        if (id1 != null && id2 != null) {
            val messagesInDbFlow = firebaseChatService.getAllMessageFromChat(groupId)
            val messagesInDb = messagesInDbFlow.first()

            assertThat(messagesInDb, hasItems(message1, message2))
            // Make sure to clean for next tests
            db.collection(FirebaseMeetUpService.MEETUP_COLL).document(groupId).collection(MSG_COLL)
                .document(id1)
            db.collection(FirebaseMeetUpService.MEETUP_COLL).document(groupId).collection(MSG_COLL)
                .document(id2)
        }
    }

    @Test
    fun fetchMessagesFetchDirectRightInfo() = runTest {
        val id1 = firebaseChatService.postMessage(groupId, message1)
        val id2 = firebaseChatService.postMessage(groupId, message2)
        assertThat(id1, notNullValue())
        assertThat(id2, notNullValue())
        if (id1 != null && id2 != null) {
            val messagesInDbFlow = firebaseChatService.fetchMessages(groupId)

            assertThat(messagesInDbFlow, hasItems(message1, message2))
            // Make sure to clean for next tests
            db.collection(FirebaseMeetUpService.MEETUP_COLL).document(groupId).collection(MSG_COLL)
                .document(id1)
            db.collection(FirebaseMeetUpService.MEETUP_COLL).document(groupId).collection(MSG_COLL)
                .document(id2)
        }
    }
}