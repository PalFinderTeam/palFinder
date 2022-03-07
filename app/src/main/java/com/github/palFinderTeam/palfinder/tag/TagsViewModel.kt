package com.github.palFinderTeam.palfinder.tag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel of the tag system.
 *
 * @property tagsRepository the repository containing the original tags (from a user, activity, ...)
 */
class TagsViewModel<T>(private val tagsRepository: TagsRepository<T>) : ViewModel()
    where T : Enum<T>,
          T : Tag {
    private val _tagContainer = MutableLiveData(tagsRepository.tags)
    // Encapsulate the liveData and only expose it as immutable LiveData
    val tagContainer: LiveData<Set<T>> = _tagContainer
    val isEditable = tagsRepository.isEditable
    val allTags = tagsRepository.allTags

    fun addTag(tag: T) {
        val changed = tagsRepository.addTag(tag)
        if (changed) {
            _tagContainer.value = tagsRepository.tags
        }
    }

    fun removeTag(tag: T) {
        val changed = tagsRepository.removeTag(tag)
        if (changed) {
            _tagContainer.value = tagsRepository.tags
        }
    }
}