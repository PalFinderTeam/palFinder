package com.github.palFinderTeam.palfinder.tag

/**
 * Provide the way data should be transfer to the tag viewModel.
 */
interface TagsRepository {
    val tags: Set<Tag>
    val isEditable: Boolean

    fun removeTag(tag: Tag): Boolean
    fun addTag(tag: Tag): Boolean
}

/**
 * Simple implementation if the tags should be readonly.
 */
class NonEditableTags(override val tags: Set<Tag>) : TagsRepository {
    override val isEditable = false

    override fun removeTag(tag: Tag): Boolean {
        // Immutable, cannot remove
        return false
    }

    override fun addTag(tag: Tag): Boolean {
        // Immutable, cannot add
        return false
    }
}

/**
 * Simple implementation if the tags should be editable.
 */
class EditableTags(private val _tags: MutableSet<Tag>) : TagsRepository {
    override val isEditable = true
    override val tags: Set<Tag> = _tags

    override fun removeTag(tag: Tag): Boolean {
        return _tags.remove(tag)
    }

    override fun addTag(tag: Tag): Boolean {
        return _tags.add(tag)
    }
}
