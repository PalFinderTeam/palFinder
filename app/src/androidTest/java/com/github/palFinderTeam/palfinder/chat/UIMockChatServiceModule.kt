package com.github.palFinderTeam.palfinder.chat

import com.github.palFinderTeam.palfinder.di.MeetUpModule
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
    replaces = [MeetUpModule::class]
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
        private val db = HashMap<String, List<ChatMessage>>()

        override fun getAllMessageFromChat(chatId: String): Flow<List<ChatMessage>> {
            return flow {
                db[chatId]?.let { emit(it) }
            }
        }

        override fun postMessage(chatId: String, message: ChatMessage) {
            db[chatId] = db[chatId]?.plus(message) ?: listOf(message)
        }
    }
}