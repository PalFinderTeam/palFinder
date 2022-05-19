package com.github.palFinderTeam.palfinder.profile

import com.github.palFinderTeam.palfinder.profile.AchievementMilestones.MILESTONE1
import com.github.palFinderTeam.palfinder.profile.AchievementMilestones.MILESTONE2
import com.github.palFinderTeam.palfinder.profile.AchievementMilestones.MILESTONE3
import com.github.palFinderTeam.palfinder.profile.AchievementMilestones.MILESTONE4
import com.github.palFinderTeam.palfinder.profile.AchievementMilestones.followCountAdapt
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
     * check if the follower deserves an achievement by checking the number of pals he follows
     */
    fun updateAchievementsFollower(follower: ProfileUser): String? {
        Achievement.values().filter { it.cat == AchievementCategory.FOLLOWING }
        return when (follower.following.size) {
            followCountAdapt(MILESTONE1) -> Achievement.PAL_FINDER.aName
            followCountAdapt(MILESTONE2) -> Achievement.PAL_MINER.aName
            followCountAdapt(MILESTONE3) -> Achievement.PAL_TRACKER.aName
            followCountAdapt(MILESTONE4) -> Achievement.PALDEX_COMPLETED.aName
            else -> null
        }
    }

    /**
     * check if the followed deserves an achievement by checking the number of pals follow him
     */
    fun updateAchievementsFollowed(followed: ProfileUser): String? {
        return when (followed.followed.size) {
            followCountAdapt(MILESTONE1) -> Achievement.BEAUTY_AND_THE_PAL.aName
            followCountAdapt(MILESTONE2) -> Achievement.CRYPTO_PAL.aName
            followCountAdapt(MILESTONE3) -> Achievement.MASTER_OF_CATS.aName
            followCountAdapt(MILESTONE4) -> Achievement.ULTIMATE_PAL.aName
            else -> null
        }
    }
}