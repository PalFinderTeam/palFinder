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
    // Encapsulate the liveData and only expose it as immutable LiveData
    val tagContainer: LiveData<Set<T>> = _tagContainer
    val isEditable
        get() = tagsRepository.isEditable
    val allTags = tagsRepository.allTags

    fun addTag(tag: T) {
        if (!isEditable) {
            return
        }
        val changed = tagsRepository.addTag(tag)
        if (changed) {
            _tagContainer.value = tagsRepository.tags
        }
    }

    fun removeTag(tag: T) {
        if (!isEditable) {
            return
        }
        val changed = tagsRepository.removeTag(tag)
        if (changed) {
            _tagContainer.value = tagsRepository.tags
        }
    }
}