package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.generics.Repository

interface ProfileService: Repository<ProfileUser> {
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
   // suspend fun doesUserIDExist(userId: String): Boolean

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