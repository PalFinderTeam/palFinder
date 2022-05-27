package com.github.palFinderTeam.palfinder.utils.generics

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.utils.Response
import kotlinx.coroutines.flow.Flow

/**
 * Represents a generic interface for a data source.
 */
interface Repository<T> {
    suspend fun exists(uuid: String): Boolean

    /**
     * Fetches every object from DB.
     *
     * @param currentDate If not null, will fetch only object that are available.
     */
    fun fetchAll(currentDate: Calendar?): Flow<List<T>>

    /**
     * Create a new object in db.
     *
     * @param obj MeetUp to create.
     */
    suspend fun create(obj: T): String?

    /**
     * Edit an existing object in db.
     *
     * @param uuid of object.
     * @param obj T that will overwrite the existing one.
     */
    suspend fun edit(uuid: String, obj: T): String?

    /**
     * Edit an existing object in db. If the [field] does
     * not exists, will create it with the new [value].
     *
     * @param uuid of object.
     * @param field String key of field to update.
     * @param value new value.
     */
    suspend fun edit(uuid: String, field: String, value: Any): String?

    fun fetchFlow(uuid: String): Flow<Response<T>>

    /**
     * Return object data from database.
     *
     * @param uuid Id of the meetup in the db.
     */
    suspend fun fetch(uuid: String): T?

    /**
     * Get all object from the list of ids.
     */
    suspend fun fetch(uuids: List<String>): List<T>?
}