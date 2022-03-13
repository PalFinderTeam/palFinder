package com.github.palFinderTeam.palfinder.meetups

import com.github.palFinderTeam.palfinder.utils.Location

/**
 * Describes a service that can fetch, create and edit MeetUps.
 *
 * It is implemented by our concrete database service and can be easily mocked in tests.
 */
interface MeetUpRepository {
    /**
     * Return meetup data from database.
     *
     * @param meetUpId Id of the meetup in the db.
     */
    suspend fun getMeetUpData(meetUpId: String): MeetUp?

    /**
     * Create a new meetUp in db.
     *
     * @param newMeetUp MeetUp to create.
     */
    suspend fun createMeetUp(newMeetUp: MeetUp): String?

    /**
     * Edit an existing meetUp in db.
     *
     * @param meetUpId id of MeetUp.
     * @param field String key of field to update.
     * @param value new value.
     */
    suspend fun editMeetUp(meetUpId: String, field: String, value: Any): String?
    /**
     * Edit an existing meetUp in db.
     *
     * @param meetUpId id of MeetUp.
     * @param meetUp meetUp that will overwrite the existing one.
     */
    suspend fun editMeetUp(meetUpId: String, meetUp: MeetUp): String?

    /**
     * Return a list of meetups around a certain location.
     *
     * It will use geoquery of firestore.
     *
     * @param location Location around which to search.
     */
    suspend fun getMeetUpsAroundLocation(location: Location, radiusInM: Double): List<MeetUp>?
}