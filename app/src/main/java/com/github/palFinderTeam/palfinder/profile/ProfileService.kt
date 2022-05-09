package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.Response
import kotlinx.coroutines.flow.Flow

interface ProfileService {
    /**
     * Fetch a profile from database.
     *
     * @param userId Id of the user to fetch.
     *
     * @return the user profile or null if something wrong occurs.
     */
    suspend fun fetchUserProfile(userId: String): ProfileUser?

    /**
     * Fetch multiple profile concurrently from database.
     *
     * @param userIds Ids of every users to fetch.
     *
     * @return the user profiles or null if something wrong occurs.
     */
    suspend fun fetchUsersProfile(userIds: List<String>): List<ProfileUser>?

    /**
     * Edit one field of a profile in database.
     *
     * @param userId id of the profile to update.
     * @param field name of the field to update.
     * @param value new value to apply.
     *
     * @return the userId or null if something wrong occurs.
     */
    suspend fun editUserProfile(userId: String, field: String, value: Any): String?

    /**
     * Edit one a profile with a whole new profile.
     *
     * @param userId id of the profile to update.
     * @param userProfile new profile to apply.
     *
     * @return the userId or null if something wrong occurs.
     */
    suspend fun editUserProfile(userId: String, userProfile: ProfileUser): String?

    /**
     * Create a profile in DB.
     *
     * @param newUserProfile profile of the new user.
     *
     * @return the user id or null if something wrong occurs.
     */
    suspend fun createProfile(newUserProfile: ProfileUser): String?

    /**
     * Fetch a profile from database and exposes it as a flow.
     *
     * @param userId Id of the user to fetch.
     *
     * @return a flow emitting Response regarding the state of the request.
     */
    fun fetchProfileFlow(userId: String): Flow<Response<ProfileUser>>

    /**
     * @return the userId of the logged in user or null if not
     */
    fun getLoggedInUserID(): String?

    /**
     * Checks if user exists in database
     *
     * @param userId Id of the user
     *
     * @return boolean
     */
    suspend fun doesUserIDExist(userId: String): Boolean

    /**
     * Try to follow a user and update the follower achievements if necessary
     *
     * @param user profile of user that ought to follow
     * @param targetId Id of user to follow
     */
    suspend fun followUser(user: ProfileUser, targetId: String): Response<Unit>

    /**
     * Try to unfollow a user
     *
     * @param user profile of user that follows
     * @param targetId Id of user to unfollow
     */
    suspend fun unfollowUser(user: ProfileUser, targetId: String): Response<Unit>

    /**
     * check if the follower deserves an achievement by checking his number of
     */
    fun updateAchievementsFollower(follower: ProfileUser): String? {
        return when (follower.following.size) {
            4 -> Achievement.PAL_FINDER.string
            9 -> Achievement.PAL_MINER.string
            29 -> Achievement.PAL_TRACKER.string
            99 -> Achievement.PALDEX_COMPLETED.string
            else -> null
        }
    }

    fun updateAchievementsFollowed(followed: ProfileUser): String? {
        return when (followed.followed.size) {
            4 -> Achievement.BEAUTY_AND_THE_PAL.string
            9 -> Achievement.CRYPTOPAL.string
            29 -> Achievement.MASTER_OF_CATS.string
            99 -> Achievement.VERIFIED.string
            else -> null
        }
    }
}