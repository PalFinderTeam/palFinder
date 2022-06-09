package com.github.palFinderTeam.palfinder.tags

import androidx.lifecycle.MutableLiveData

/**
 * Provide the way data should be transfer to the tag viewModel.
 *
 * @property tags tags to be displayed.
 * @property isEditable true if the tag list should be editable.
 * @property allTags set of all possible tags in this context.
 */
interface TagsRepository<T: Tag> {
    val tags: Set<T>
    val isEditable: Boolean
    val allTags: Set<T>

    /**
     * Remove a tag from the set.
     *
     * @param tag tag to remove.
     */
    fun removeTag(tag: T): Boolean

    /**
     * Add a tag to the set.
     *
     * @param tag tag to add.
     */
    fun addTag(tag: T): Boolean
}

class TagFilterRepository<T: Tag>(private val tagsLiveData: MutableLiveData<Set<T>>, override val allTags: Set<T>) : TagsRepository<T> {
    override val tags: Set<T>
        get() = tagsLiveData.value ?: setOf()

    override val isEditable = true

    override fun removeTag(tag: T): Boolean {
        val tags = tagsLiveData.value
        return if (tags == null || !tags.contains(tag)) {
            false
        } else {
            tagsLiveData.value = tags.minus(tag)
            true
        }
    }

    override fun addTag(tag: T): Boolean {
        val tags = tagsLiveData.value
        return if (tags == null || tags.contains(tag)) {
            false
        } else {
            tagsLiveData.value = tags.plus(tag)
            true
        }
    }
}