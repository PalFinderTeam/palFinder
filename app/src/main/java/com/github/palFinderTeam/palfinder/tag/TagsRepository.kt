package com.github.palFinderTeam.palfinder.tag

/**
 * Provide the way data should be transfer to the tag viewModel.
 *
 * @property tags tags to be displayed.
 * @property isEditable true if the tag list should be editable.
 * @property allTags set of all possible tags in this context.
 */
interface TagsRepository<T>
    where T : Enum<T>,
          T : Tag {
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

/**
 * Simple implementation if the tags should be readonly.
 */
class NonEditableTags<T>(override val tags: Set<T>, override val allTags: Set<T>) : TagsRepository<T>
    where T : Enum<T>,
          T : Tag {
    override val isEditable = false

    override fun removeTag(tag: T): Boolean {
        // Immutable, cannot remove
        return false
    }

    override fun addTag(tag: T): Boolean {
        // Immutable, cannot add
        return false
    }
}

/**
 * Simple implementation if the tags should be editable.
 */
class EditableTags<T>(private val _tags: MutableSet<T>, override val allTags: Set<T>) : TagsRepository<T>
    where T : Enum<T>,
          T : Tag {
    override val isEditable = true
    override val tags: Set<T> = _tags

    override fun removeTag(tag: T): Boolean {
        return _tags.remove(tag)
    }

    override fun addTag(tag: T): Boolean {
        return _tags.add(tag)
    }
}
