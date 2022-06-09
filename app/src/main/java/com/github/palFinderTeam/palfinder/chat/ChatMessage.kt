package com.github.palFinderTeam.palfinder.chat

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import android.icu.util.Calendar

/**
 * Represent a message in a conversation.
 *
 * @property sentAt Time of sending.
 * @property sentAt Id of the sender
 * @property content The message itself.
 */
data class ChatMessage(val sentAt: Calendar, val sentBy: String, val content: String, val isEdited: Boolean = false) {
    companion object {
        /**
         *  Tries to convert a Firestore result in chat message.
         */
        fun DocumentSnapshot.toChatMessage(): ChatMessage? {
            return try {
                val sentAt = getDate("sentAt")!!
                val sentBy = getString("sentBy")!!
                val content = getString("content")!!
                val isEdited = getBoolean("isEdited")!!
                val sentAtCal = Calendar.getInstance().apply { time = sentAt }

                ChatMessage(sentAtCal, sentBy, content, isEdited)
            } catch (e: Exception) {
                Log.e("ChatMessage", "Error deserializing chat message", e)
                null
            }
        }
    }

    /**
     * Convert a message in Firestore friendly format.
     */
    fun toFirebaseDocument(): HashMap<String, Any> {
        return hashMapOf(
            "sentAt" to sentAt.time,
            "sentBy" to sentBy,
            "content" to content,
            "isEdited" to isEdited
        )
    }
}
