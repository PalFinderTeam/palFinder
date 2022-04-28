package com.github.palFinderTeam.palfinder.chat

import com.github.palFinderTeam.palfinder.di.ChatModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ChatModule::class]
)
/**
 * Provide a mock chat service for every UI tests.
 */
object UIMockChatServiceModule {

    private val chatService = UIMockChatService()

    @Singleton
    @Provides
    fun provideFirebaseChatService(): ChatService {
        return chatService
    }

    class UIMockChatService : ChatService {
        private val db = HashMap<String, MutableList<ChatMessage>>()

        override fun getAllMessageFromChat(chatId: String): Flow<List<ChatMessage>> {
            return flow {
                db[chatId]?.let { emit(it) }
            }
        }

        override suspend fun fetchMessages(chatId: String): List<ChatMessage>? {
            return db[chatId]
        }

        override suspend fun editMessage(groupId: String, msgId: String, newContent: String): String? {
            val msg = db[groupId]?.get(msgId.toInt())
            return if (msg != null) {
                db[groupId]!![msgId.toInt()] = msg.copy(content = newContent, isEdited = true)
                msgId
            } else {
                null
            }
        }

        override suspend fun postMessage(chatId: String, message: ChatMessage): String? {
            db[chatId] = (db[chatId]?.plus(message) ?: mutableListOf(message)) as MutableList<ChatMessage>
            return db[chatId]?.indexOf(message).toString()

        }

        fun clear(){
            db.clear()
        }
    }
}