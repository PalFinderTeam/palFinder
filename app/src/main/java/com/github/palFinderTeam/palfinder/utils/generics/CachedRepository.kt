package com.github.palFinderTeam.palfinder.utils.generics

import Repository
import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import com.github.palFinderTeam.palfinder.utils.evictAfterXMinutes
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class CachedRepository<T: FirebaseObject>(
    private val name: String,
    private val clazz: Class<T>,
    private val rp: Repository<T>,
    private val time: TimeService,
    private val contextProvider: ContextService
) : Repository<T> {
    private var cache = DictionaryCache(name, clazz, false, contextProvider.get(), evictAfterXMinutes(10, time))

    override suspend fun fetch(uuid: String): T? {
        return if (cache.contains(uuid)){
            cache.get(uuid)
        }else{
            rp.fetch(uuid)
        }
    }

    override fun fetchFlow(uuid: String): Flow<Response<T>> {
        return if (cache.contains(uuid)){
            flow {
                emit(Response.Success(cache.get(uuid)))
            }
        }else{
            rp.fetchFlow(uuid)
        }
    }

    override suspend fun edit(uuid: String, field: String, value: Any): String? {
        val id = rp.edit(uuid, field, value)
        return if (id != null){
            cache.store(uuid, rp.fetch(uuid)!!)
            id
        }
        else{
            null
        }
    }

    override suspend fun edit(uuid: String, obj: T): String? {
        val id = rp.edit(uuid, obj)
        return if(id != null){
            cache.store(uuid, obj)
            id
        }
        else{
            null
        }
    }

    override suspend fun exists(uuid: String): Boolean {
        return cache.contains(uuid) || rp.exists(uuid)
    }

    override fun fetchAll(currentDate: Calendar?): Flow<List<T>> {
        return rp.fetchAll(currentDate).map { it.ifEmpty { cache.getAll() } }
    }

    override suspend fun create(obj: T): String? {
        return rp.create(obj)
    }

    override suspend fun fetch(uuids: List<String>): List<T> {
        return uuids.mapNotNull { fetch(it) }
    }
}