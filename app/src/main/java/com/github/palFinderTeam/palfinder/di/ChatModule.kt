package com.github.palFinderTeam.palfinder.di

import com.github.palFinderTeam.palfinder.chat.ChatService
import com.github.palFinderTeam.palfinder.chat.FirebaseChatService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ChatModule {

    @Singleton
    @Provides
    fun provideFirebaseChatService(): ChatService {
        return FirebaseChatService
    }
}