package com.github.palFinderTeam.palfinder.chat

import com.github.palFinderTeam.palfinder.chat.ChatMessage.Companion.toChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/**
 * Object containing methods to query the database about chat messages.
 */
open class FirebaseChatService @Inject constructor(
    private val db: FirebaseFirestore
) : ChatService {

    /**
     * fetch all the message from the database
     *
     * @param chatId id of the group, the same as the meetup id.
     */
    override fun getAllMessageFromChat(chatId: String): Flow<List<ChatMessage>> {
        return callbackFlow {
            val listenerRegistration = db.collection(CONVERSATION_COLL)
                .document(chatId)
                .collection(MSG_COLL)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        cancel(
                            message = "Error fetching messages",
                            cause = firebaseFirestoreException
                        )
                        return@addSnapshotListener
                    }

                    val messages = querySnapshot?.documents
                        ?.mapNotNull { it.toChatMessage() }

                    if (messages != null) {
                        trySend(messages)
                    }
                }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }


    /**
     * Get all messages from a group.
     *
     * @param chatId id of the group, the same as the meetup id.
     */
    override suspend fun fetchMessages(chatId: String): List<ChatMessage>? {
        return try {
            db.collection(CONVERSATION_COLL)
                .document(chatId)
                .collection(MSG_COLL)
                .get()
                .await()
                ?.documents
                ?.mapNotNull { it.toChatMessage() }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Post a message to a group, if the group does not exist, it creates it.
     *
     * @param chatId id of the group, the same as the meetup id.
     * @param message message to post.
     *
     * @return The msgId or null if something wrong happened.
     */
    override suspend fun postMessage(chatId: String, message: ChatMessage): String? {
        return try {
            db.collection(CONVERSATION_COLL)
                .document(chatId)
                .collection(MSG_COLL)
                .add(message.toFirebaseDocument()).await().id
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Edit a message inside a group.
     *
     * @param groupId Id of the group.
     * @param msgId Id of the message to edit.
     * @param newContent New content of the message.
     *
     * @return The msgId or null if something wrong happened.
     */
    override suspend fun editMessage(groupId: String, msgId: String, newContent: String): String? {
        return try {
            db.collection(CONVERSATION_COLL)
                .document(groupId)
                .collection(MSG_COLL)
                .document(msgId)
                .update(
                    mapOf(
                        "content" to newContent,
                        "isEdited" to true
                    )
                ).await()
            msgId
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        const val CONVERSATION_COLL = "conversations"
        const val MSG_COLL = "messages"
    }
}