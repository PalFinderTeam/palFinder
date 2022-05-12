package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import android.util.Log
import com.github.palFinderTeam.palfinder.meetups.activities.ShowParam
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.generics.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Describes a service that can fetch, create and edit MeetUps.
 *
 * It is implemented by our concrete database service and can be easily mocked in tests.
 */
interface MeetUpRepository : Repository<MeetUp> {
    /**
     * Return a list of meetups around a certain location.
     *
     * It will use geoquery of firestore.
     *
     * @param location Location around which to search.
     * @param radiusInKm Radius in Km of the search.
     * @param currentDate If not null, will fetch only meetups that are available at this date.
     */
    fun getMeetUpsAroundLocation(
        location: Location,
        radiusInKm: Double,
        currentDate: Calendar? = null,
        showParam: ShowParam? = ShowParam.ALL,
        profile: ProfileUser? = null
    ): Flow<Response<List<MeetUp>>>

    /**
     * Try to join a meetup.
     *
     * @param meetUpId Id of the meetup to join.
     * @param userId Id of user that joins.
     */
    suspend fun joinMeetUp(
        meetUpId: String,
        userId: String,
        now: Calendar,
        profile: ProfileUser
    ): Response<Unit>

    /**
     * Try to leave a meetup.
     *
     * @param meetUpId Id of the meetup to leave.
     * @param userId Id of user that leaves.
     */
    suspend fun leaveMeetUp(meetUpId: String, userId: String): Response<Unit>


    /**
     * Get all meetup the user, with id [userId], is taking part of.
     * @param currentDate If not null, will fetch only meetups that are available.
     *
     * @return A flow of the form Fetching -> MeetUps or Fetching -> Failure.
     */
    fun getUserMeetups(userId: String, currentDate: Calendar? = null): Flow<Response<List<MeetUp>>>


    /**
     * provide additional filter for the getMeetupAroundLocation function, depending on the showParam
     * you either check if the profileUser is following a meetup participant, or the meetup creator
     * @param profile the profile of the logged user
     * @param meetUp the meetUp we need to filter
     * @param showParam the way we must filter it
     */
    fun additionalFilter(profile: ProfileUser?, meetUp: MeetUp, showParam: ShowParam?): Boolean {
        return when (showParam) {
            ShowParam.PAL_PARTCIPATING -> profile!!.following.any { meetUp.isParticipating(it) }
            ShowParam.PAL_CREATOR -> profile!!.following.any{meetUp.creatorId == it}
            else -> true
        }
    }
}