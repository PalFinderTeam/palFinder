package com.github.palFinderTeam.palfinder.chat

import com.github.palFinderTeam.palfinder.chat.ChatMessage.Companion.toChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseChatService @Inject constructor(
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

    override fun postMessage(chatId: String, message: ChatMessage) {
        try {
            db.collection(CONVERSATION_COLL)
                .document(chatId)
                .collection(MSG_COLL)
                .add(message.toFirebaseDocument())
        } catch (e: Exception) {

        }
    }

    companion object {
        private const val CONVERSATION_COLL = "conversations"
        private const val MSG_COLL = "messages"
    }
}