package com.github.palFinderTeam.palfinder.tag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel of the tag system.
 *
 * @property tagsRepository the repository containing the original tags (from a user, activity, ...)
 */
class TagsViewModel<T: Tag>(private val tagsRepository: TagsRepository<T>) : ViewModel() {
    private val _tagContainer = MutableLiveData(tagsRepository.tags)
    private val _isEditable = MutableLiveData<Boolean>(tagsRepository.isEditable)
    // Encapsulate the liveData and only expose it as immutable LiveData
    val tagContainer: LiveData<Set<T>> = _tagContainer
    val isEditable: LiveData<Boolean> = _isEditable
    val allTags = tagsRepository.allTags

    fun addTag(tag: T) {
        if (!isEditable.value!!) {
            return
        }
        val changed = tagsRepository.addTag(tag)
        if (changed) {
            refreshTags()
        }
    }

    fun removeTag(tag: T) {
        if (!isEditable.value!!) {
            return
        }
        val changed = tagsRepository.removeTag(tag)
        if (changed) {
            refreshTags()
        }
    }

    /**
     * Refresh tags value from repository, useful when repository values are acquired
     * asynchronously for instance.
     */
    fun refreshTags() {
        _tagContainer.value = tagsRepository.tags
    }

    /**
     * Set editable status, which is possible only if the repository allows it (is editable itself)
     * This allows the UI to have an edit button even if it is already editable.
     */
    fun setEditable(editable: Boolean) {
        if (tagsRepository.isEditable) {
            _isEditable.value = editable
        }
    }
}