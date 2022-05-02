package com.github.palFinderTeam.palfinder.chat

import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CachedChatService @Inject constructor(
    private val db: FirebaseFirestore,
    private val contextProvider: ContextService
) : FirebaseChatService(db) {
    private var cache = DictionaryCache("chat", ChatCache::class.java, false, contextProvider.get())

    override fun getAllMessageFromChat(chatId: String): Flow<List<ChatMessage>> {
        val ret = super.getAllMessageFromChat(chatId)
        ret.map {
            if(it.isEmpty()){
                cache.get(chatId).msg
            }
            else{
                cache.store(chatId, ChatCache(it))
                it
            }
        }
        return ret
    }

    data class ChatCache(val msg: List<ChatMessage>)
}