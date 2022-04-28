package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import com.github.palFinderTeam.palfinder.utils.evictAfterXMinutes
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CachedProfileService @Inject constructor(
    private val db: FirebaseFirestore,
    private val time: TimeService,
    private val contextProvider: ContextService
) : FirebaseProfileService(db) {
    private var cache = DictionaryCache("profile", ProfileUser::class.java, false, contextProvider.get(), evictAfterXMinutes(10, time))

    override suspend fun fetchUserProfile(userId: String): ProfileUser? {
        return if (cache.contains(userId)){
            cache.get(userId)
        }else{
            super.fetchUserProfile(userId)
        }
    }

    override fun fetchProfileFlow(userId: String): Flow<Response<ProfileUser>> {
        return if (cache.contains(userId)){
            flow {
                emit(Response.Success(cache.get(userId)))
            }
        }else{
            super.fetchProfileFlow(userId)
        }
    }

    override suspend fun editUserProfile(userId: String, field: String, value: Any): String? {
        val id = super.editUserProfile(userId, field, value)
        return if (id != null){
            cache.store(userId, super.fetchUserProfile(userId)!!)
            id
        }
        else{
            null
        }
    }

    override suspend fun editUserProfile(userId: String, userProfile: ProfileUser): String? {
        val id = super.editUserProfile(userId, userProfile)
        return if(id != null){
            cache.store(userId, userProfile)
            id
        }
        else{
            null
        }
    }
}