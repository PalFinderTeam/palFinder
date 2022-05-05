package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.cache.FileCache
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import com.github.palFinderTeam.palfinder.utils.evictAfterXMinutes
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CachedMeetUpService @Inject constructor(
    private val db: FirebaseFirestore,
    private val time: TimeService,
    private val contextProvider: ContextService
): FirebaseMeetUpService(db) {
    private var cache = DictionaryCache("meetup", MeetUp::class.java, false, contextProvider.get(), evictAfterXMinutes(10, time))
    private var cacheJoined = FileCache("meetup_joined", JoinedMeetupListWrapper::class.java, true, contextProvider.get())

    private fun addJoinedMeetupToCache(meetUpId: String){
        val jml = if (cacheJoined.exist()){
            cacheJoined.get()
        } else {
            JoinedMeetupListWrapper(mutableListOf())
        }
        if (!jml.lst.contains(meetUpId)) {
            jml.lst.add(meetUpId)
            cacheJoined.store(jml)
        }
    }
    private fun removeJoinedMeetupFromCache(meetUpId: String){
        val jml = cacheJoined.get()
        while (jml.lst.contains(meetUpId)) {
            jml.lst.remove(meetUpId)
        }
        cacheJoined.store(jml)
    }
    private fun clearJoinedMeetupToCache(meetUpId: String){
        cacheJoined.store(JoinedMeetupListWrapper(mutableListOf()))
    }

    override suspend fun create(obj: MeetUp): String? {
        val ret = super.create(obj)
        if (ret != null){
            addJoinedMeetupToCache(ret)
            fetch(ret) // Cache it
        }
        return ret
    }

    /**
     * Return List of all joined Meetup ID
     */
    fun getAllJoinedMeetupID(): List<String>{
        return if (cacheJoined.exist()){
            cacheJoined.get().lst
        } else {
            mutableListOf()
        }
    }

    override suspend fun fetch(uuid: String): MeetUp? {
        return if (cache.contains(uuid)){
            cache.get(uuid)
        }
        else{
            super.fetch(uuid)
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

    override suspend fun edit(uuid: String, obj: MeetUp): String? {
        val id = super.edit(uuid, obj)
        return if(id != null){
            cache.store(uuid, obj)
            id
        }
        else{
            null
        }
    }

    override suspend fun joinMeetUp(meetUpId: String, userId: String, now: Calendar, profile: ProfileUser): Response<Unit> {
        return when(val ret = super.joinMeetUp(meetUpId, userId, now, profile)){
            is Response.Success -> {
                addJoinedMeetupToCache(meetUpId)
                cache.delete(meetUpId)
                ret
            }
            else -> ret
        }
    }

    override suspend fun leaveMeetUp(meetUpId: String, userId: String): Response<Unit> {
        return when(val ret = super.leaveMeetUp(meetUpId, userId)){
            is Response.Success -> {
                removeJoinedMeetupFromCache(meetUpId)
                cache.delete(meetUpId)
                ret
            }
            else -> ret
        }
    }

    override fun fetchAll(currentDate: Calendar?): Flow<List<MeetUp>> {
        return super.fetchAll(currentDate).map {
            it.ifEmpty {
                cache.getAll()
            }
        }
    }

    private data class JoinedMeetupListWrapper(val lst: MutableList<String>)
}