package com.github.palFinderTeam.palfinder.profile


import android.icu.util.Calendar
import android.util.Log
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.notification.NotificationHandler
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.ACHIEVEMENTS_OBTAINED
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWED_BY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.FOLLOWING_PROFILES
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.toProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.generics.FirestoreRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

open class FirebaseProfileService @Inject constructor(
    private val db: FirebaseFirestore
) : ProfileService {

    private val wrapper = FirestoreRepository(db, PROFILE_COLL, null) { it.toProfileUser() }

    override suspend fun fetch(uuid: String): ProfileUser? = wrapper.fetch(uuid)

    override fun fetchFlow(uuid: String): Flow<Response<ProfileUser>> = wrapper.fetchFlow(uuid)

    override suspend fun fetch(uuids: List<String>): List<ProfileUser> = wrapper.fetch(uuids)

    override suspend fun edit(uuid: String, field: String, value: Any): String? =
        wrapper.edit(uuid, field, value)

    override suspend fun edit(uuid: String, obj: ProfileUser): String? = wrapper.edit(uuid, obj)

    override suspend fun create(obj: ProfileUser): String? = wrapper.create(obj)

    override fun fetchAll(currentDate: Calendar?): Flow<List<ProfileUser>> =
        wrapper.fetchAll(currentDate)

    override suspend fun exists(uuid: String): Boolean = wrapper.exists(uuid)


    override suspend fun followUser(user: ProfileUser, targetId: String): Response<Unit> {
        return try {
            if (!user.canFollow(targetId)) {
                return Response.Failure("Cannot follow this user.")
            }
            val targetProfile = fetch(targetId)!!
            val batch = db.batch()
            batch.update(
                db.collection(PROFILE_COLL).document(user.uuid),
                FOLLOWING_PROFILES, FieldValue.arrayUnion(targetId)
            )
            if (updateAchievementsFollower(user).isNotEmpty()) {
                batch.update(
                    db.collection(PROFILE_COLL).document(user.uuid),
                    ACHIEVEMENTS_OBTAINED, FieldValue.arrayUnion(updateAchievementsFollower(user)[0])
                )
            }
            batch.update(
                db.collection(PROFILE_COLL).document(targetId),
                FOLLOWED_BY, FieldValue.arrayUnion(user.uuid)
            )
            if (updateAchievementsFollowed(targetProfile).isNotEmpty()) {
                batch.update(
                    db.collection(PROFILE_COLL).document(targetId),
                    ACHIEVEMENTS_OBTAINED, FieldValue.arrayUnion(updateAchievementsFollowed(targetProfile)[0])
                )
            }
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




    companion object {
        const val PROFILE_COLL = "users"
    }
}