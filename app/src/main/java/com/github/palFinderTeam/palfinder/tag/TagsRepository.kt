package com.github.palFinderTeam.palfinder.tag

/**
 * Provide the way data should be transfer to the tag viewModel.
 */
interface TagsRepository<T>
    where T : Enum<T>,
          T : Tag {
    val tags: Set<T>
    val isEditable: Boolean

    fun removeTag(tag: T): Boolean
    fun addTag(tag: T): Boolean
}

/**
 * Simple implementation if the tags should be readonly.
 */
class NonEditableTags<T>(override val tags: Set<T>) : TagsRepository<T>
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
class EditableTags<T>(private val _tags: MutableSet<T>) : TagsRepository<T>
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
