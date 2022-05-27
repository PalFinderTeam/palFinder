package com.github.palFinderTeam.palfinder.chat

import kotlinx.coroutines.flow.Flow

/**
 * Interface for chat service
 */
interface ChatService {

    /**
     * Get all messages from a group.
     *
     * @param chatId id of the group, the same as the meetup id.
     */
    fun getAllMessageFromChat(chatId: String): Flow<List<ChatMessage>>

    /**
     * Get all messages from a group.
     *
     * @param chatId id of the group, the same as the meetup id.
     */
    suspend fun fetchMessages(chatId: String): List<ChatMessage>?

    /**
     * Post a message to a group, if the group does not exist, it creates it.
     *
     * @param chatId id of the group, the same as the meetup id.
     * @param message message to post.
     *
     * @return The msgId or null if something wrong happened.
     */
    suspend fun postMessage(chatId: String, message: ChatMessage): String?

    /**
     * Edit a message inside a group.
     *
     * @param groupId Id of the group.
     * @param msgId Id of the message to edit.
     * @param newContent New content of the message.
     *
     * @return The msgId or null if something wrong happened.
     */
    suspend fun editMessage(groupId: String, msgId: String, newContent: String): String?
}