package com.github.palFinderTeam.palfinder.utils

/**
 * Represent a response for an asynchronous process, like DB queries.
 * It is great as an output for a flow. They are really similar to a Result,
 * but with an intermediary state Loading, useful to display loading widget while doing the task.
 */
sealed class Response<out T> {
    class Loading<out T>: Response<T>()

    /**
     * Successful response.
     * @param data the data of the response
     */
    data class Success<out T>(
        val data: T
    ): Response<T>()

    /**
     * Error response.
     */
    data class Failure<out T>(
        val errorMessage: String
    ): Response<T>()
}