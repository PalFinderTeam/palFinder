package com.github.palFinderTeam.palfinder.profile.services

import com.github.palFinderTeam.palfinder.profile.Achievement
import com.github.palFinderTeam.palfinder.profile.AchievementCategory
import com.github.palFinderTeam.palfinder.profile.ProfileUser
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
     * Try to mute a meetup
     *
     * @param user profile of user
     * @param meetup Id of meetup to mute
     */
    suspend fun muteMeetup(user: ProfileUser, meetup: String): Response<Unit>

    /**
     * Try to unmute a meetup
     *
     * @param user profile of user
     * @param meetup Id of meetup to mute
     */
    suspend fun unMuteMeetup(user: ProfileUser, meetup: String): Response<Unit>

    /**
     * check if the follower deserves an achievement by checking the number of pals he follows
     */
    fun updateAchievementsFollower(follower: ProfileUser): List<String> {
        return Achievement.values().filter { it.cat == AchievementCategory.FOLLOWER &&
                it.milestone == follower.following.size}.map{it.aName}
    }

    /**
     * check if the followed deserves an achievement by checking the number of pals follow him
     */
    fun updateAchievementsFollowed(followed: ProfileUser): List<String> {
        return Achievement.values().filter { it.cat == AchievementCategory.FOLLOWED &&
                it.milestone == followed.followed.size}.map{it.aName}
    }
}