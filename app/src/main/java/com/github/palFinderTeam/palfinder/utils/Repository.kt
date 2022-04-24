package com.github.palFinderTeam.palfinder.utils

import com.github.palFinderTeam.palfinder.profile.ProfileUser
import kotlinx.coroutines.flow.Flow

interface Repository<T> {
    /**
     * Fetch an object from repository.
     *
     * @param id Id of the object to fetch.
     *
     * @return the object or null if something wrong occurs.
     */
    suspend fun repositoryGet(id: String): T?

    /**
     * Edit one field of an object in repository.
     *
     * @param id id of the object to update.
     * @param field name of the field to update.
     * @param value new value to apply.
     *
     * @return the id or null if something wrong occurs.
     */
    suspend fun repositoryEdit(id: String, field: String, value: Any): String?

    /**
     * Edit one object with a whole new object.
     *
     * @param id id of the object to update.
     * @param obj new profile to apply.
     *
     * @return the id or null if something wrong occurs.
     */
    suspend fun repositoryEdit(id: String, obj: ProfileUser): String?

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
     * Checks if object exists in repository
     *
     * @param id Id of the object
     *
     * @return boolean
     */
    suspend fun exists(id: String): Boolean
}