package com.github.palFinderTeam.palfinder.tag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel of the tag system.
 *
 * @property tagsRepository the repository containing the original tags (from a user, activity, ...)
 */
class TagsViewModel(private val tagsRepository: TagsRepository) : ViewModel() {
    private val _tagContainer = MutableLiveData(tagsRepository.tags)
    // Encapsulate the liveData and only expose it as immutable LiveData
    val tagContainer: LiveData<Set<Tag>> = _tagContainer
    val isEditable = tagsRepository.isEditable

    fun addTag(tag: Tag) {
        val changed = tagsRepository.addTag(tag)
        if (changed) {
            _tagContainer.value = tagsRepository.tags
        }
    }

    fun removeTag(tag: Tag) {
        val changed = tagsRepository.removeTag(tag)
        if (changed) {
            _tagContainer.value = tagsRepository.tags
        }
    }
}