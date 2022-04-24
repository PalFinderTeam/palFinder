package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.PalFinderApplication
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CachedProfileService @Inject constructor(
    private val db: FirebaseFirestore
) : FirebaseProfileService(db) {
    private var cache = DictionaryCache("profile", ProfileUser::class.java, false, PalFinderApplication.instance)

    override suspend fun fetchUserProfile(userId: String): ProfileUser? {
        val ret = super.fetchUserProfile(userId)
        return if (ret == null && cache.contains(userId)){
            cache.get(userId)
        }
        else{
            ret
        }
    }

    override fun fetchProfileFlow(userId: String): Flow<Response<ProfileUser>> {
        return super.fetchProfileFlow(userId).map {
            when(it){
                is Response.Success -> it
                is Response.Failure -> {
                    if (cache.contains(userId)){
                        Response.Success(cache.get(userId))
                    }
                    else{
                        it
                    }
                }
                else -> it
            }
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

    override suspend fun createProfile(newUserProfile: ProfileUser): String? {
        createProfile()
    }
}