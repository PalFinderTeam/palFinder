package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.toProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseProfileService : ProfileService {

    private const val PROFILE_COLL = "users"

    override suspend fun fetchUserProfile(userId: String): ProfileUser? {
        val db = FirebaseFirestore.getInstance()

        return try {
            db.collection(PROFILE_COLL)
                .document(userId).get().await().toProfileUser()
        } catch (e: Exception) {
            null
        }
    }

    override fun fetchProfileFlow(userId: String): Flow<Response<ProfileUser>> {
       return flow {
           emit(Response.Loading())

           val profile = fetchUserProfile(userId)
           emit(Response.Success(profile!!))
       }.catch { error ->
           error.message?.let {
               emit(Response.Failure(it))
           }
       }
    }

    override suspend fun fetchUsersProfile(userIds: List<String>): List<ProfileUser>? =
        withContext(Dispatchers.IO) {
            try {
                userIds.map {
                    async {
                        fetchUserProfile(it)
                    }
                }.awaitAll().map { it!! }.toList()
            } catch (e: Exception) {
                null
            }
        }

    override suspend fun editUserProfile(userId: String, field: String, value: Any): String? {
        val db = FirebaseFirestore.getInstance()

        return try {
            db.collection(PROFILE_COLL).document(userId).update(field, value).await()
            userId
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun editUserProfile(userId: String, userProfile: ProfileUser): String? {
        val db = FirebaseFirestore.getInstance()

        return try {
            db.collection(PROFILE_COLL).document(userId).update(userProfile.toFirestoreData()).await()
            userId
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createProfile(newUserProfile: ProfileUser): String? {
        val db = FirebaseFirestore.getInstance()

        return try {
            db.collection(PROFILE_COLL).add(newUserProfile.toFirestoreData()).await().id
        } catch (e: Exception) {
            null
        }
    }
}