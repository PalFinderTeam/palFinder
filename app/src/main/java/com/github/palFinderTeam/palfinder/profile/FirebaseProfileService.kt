package com.github.palFinderTeam.palfinder.profile

import android.util.Log
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWED_BY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWING_PROFILES
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.toProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class FirebaseProfileService @Inject constructor(
    private val db: FirebaseFirestore
) : ProfileService {

    override suspend fun fetchUserProfile(userId: String): ProfileUser? {
        return try {
            db.collection(PROFILE_COLL)
                .document(userId).get().await().toProfileUser()
        } catch (e: Exception) {
            Log.d("db user", "failed safely")
            null
        }
    }

    override fun fetchProfileFlow(userId: String): Flow<Response<ProfileUser>> {
        return flow {
            emit(Response.Loading())

            val profile = fetchUserProfile(userId)
            if (profile != null) {
                emit(Response.Success(profile))
            } else {
                emit(Response.Failure("Could not find user."))
            }

        }.catch { error ->
            emit(Response.Failure(error.message.orEmpty()))
        }
    }

    override suspend fun fetchUsersProfile(userIds: List<String>): List<ProfileUser> {
        // Firebase don't support more than 10 ids in query.
        val chunked = userIds.chunked(10)
        val queries = chunked.map {
            db.collection(PROFILE_COLL).whereIn(FieldPath.documentId(), it).get()
        }
        val result = Tasks.whenAllSuccess<QuerySnapshot>(queries).await()
        return result.flatMap { it.documents.mapNotNull { it.toProfileUser() } }
    }

    override suspend fun editUserProfile(userId: String, field: String, value: Any): String? {
        return try {
            db.collection(PROFILE_COLL).document(userId).update(field, value).await()
            userId
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun editUserProfile(userId: String, userProfile: ProfileUser): String? {
        return try {
            db.collection(PROFILE_COLL).document(userId).update(userProfile.toFirestoreData())
                .await()
            userId
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createProfile(newUserProfile: ProfileUser): String? {
        return try {
            db.collection(PROFILE_COLL).document(newUserProfile.uuid)
                .set(newUserProfile.toFirestoreData()).await()
            newUserProfile.uuid
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun followUser(user: ProfileUser, targetId: String): Response<Unit> {
        return try {
            if (!user.canFollow(targetId)) {
                return Response.Failure("Cannot follow this user.")
            }
            val batch = db.batch()
            batch.update(
                db.collection(PROFILE_COLL).document(user.uuid),
                FOLLOWING_PROFILES, FieldValue.arrayUnion(targetId)
            )
            batch.update(
                db.collection(PROFILE_COLL).document(targetId),
                FOLLOWED_BY, FieldValue.arrayUnion(user.uuid)
            )
            batch.commit().await()
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Failure(e.message.orEmpty())
        }
    }

    override suspend fun unfollowUser(user: ProfileUser, targetId: String): Response<Unit> {
        return try {
            if (!user.canUnFollow(targetId)) {
                return Response.Failure("Cannot unfollow this user.")
            }
            val batch = db.batch()
            batch.update(
                db.collection(PROFILE_COLL).document(user.uuid),
                FOLLOWING_PROFILES, FieldValue.arrayRemove(targetId)
            )
            batch.update(
                db.collection(PROFILE_COLL).document(targetId),
                FOLLOWED_BY, FieldValue.arrayRemove(user.uuid)
            )
            batch.commit().await()
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Failure(e.message.orEmpty())
        }
    }

    override fun getLoggedInUserID(): String? = Firebase.auth.currentUser?.uid

    override suspend fun doesUserIDExist(userId: String): Boolean {
        return db.collection(PROFILE_COLL).document(userId).get().await().exists()
    }

    companion object {
        const val PROFILE_COLL = "users"
    }
}