package com.github.palFinderTeam.palfinder.chat

import kotlinx.coroutines.flow.Flow

interface ChatService {

    /**
     * Get all messages from a group.
     *
     * @param chatId id of the group, the same as the meetup id.
     */
    fun getAllMessageFromChat(chatId: String): Flow<List<ChatMessage>>

    /**
     * Post a message to a group, if the group does not exist, it creates it.
     *
     * @param chatId id of the group, the same as the meetup id.
     * @param message message to post.
     */
    fun postMessage(chatId: String, message: ChatMessage)
}