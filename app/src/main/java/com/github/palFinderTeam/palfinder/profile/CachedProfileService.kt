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

    override suspend fun fetch(uuid: String): ProfileUser? {
        return if (cache.contains(uuid)){
            cache.get(uuid)
        }else{
            super.fetch(uuid)
        }
    }

    override fun fetchFlow(uuid: String): Flow<Response<ProfileUser>> {
        return if (cache.contains(uuid)){
            flow {
                emit(Response.Success(cache.get(uuid)))
            }
        }else{
            super.fetchFlow(uuid)
        }
    }

    override suspend fun edit(uuid: String, field: String, value: Any): String? {
        val id = super.edit(uuid, field, value)
        return if (id != null){
            cache.store(uuid, super.fetch(uuid)!!)
            id
        }
        else{
            null
        }
    }

    override suspend fun edit(uuid: String, obj: ProfileUser): String? {
        val id = super.edit(uuid, obj)
        return if(id != null){
            cache.store(uuid, obj)
            id
        }
        else{
            null
        }
    }
}