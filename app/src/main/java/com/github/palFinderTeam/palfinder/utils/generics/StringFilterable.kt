package com.github.palFinderTeam.palfinder.utils.generics

/**
 * Represents an object that can be filtered by text.
 */
interface StringFilterable {
    /**
     * Return a string containing all the text contains in the object
     */
    fun getAllText(): String

    /**
     * Check if the text contains the given string words
     */
    fun containsString(query: String): Boolean{
        val text = getAllText()
        val words = query.lowercase().split(" ")
        return words.all {
            text.lowercase().contains(it)
        }
    }
}
