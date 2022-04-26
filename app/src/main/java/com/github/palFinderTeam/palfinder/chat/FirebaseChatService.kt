package com.github.palFinderTeam.palfinder.chat

import com.github.palFinderTeam.palfinder.chat.ChatMessage.Companion.toChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

open class FirebaseChatService @Inject constructor(
    private val db: FirebaseFirestore
) : ChatService {

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