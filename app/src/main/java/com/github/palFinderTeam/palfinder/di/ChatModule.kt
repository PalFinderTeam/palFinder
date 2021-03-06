package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.chat.ChatService
import com.github.palFinderTeam.palfinder.chat.FirebaseChatService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/**
 * Inject chat when needed, it makes certain class more testable.
 */
abstract class ChatModule {

    @Singleton
    @Binds
    abstract fun bindChatService(
        firebaseChatService: FirebaseChatService
    ): ChatService
}