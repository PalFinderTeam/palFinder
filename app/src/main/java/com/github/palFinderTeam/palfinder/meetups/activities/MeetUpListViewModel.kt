package com.github.palFinderTeam.palfinder.meetups.activities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@HiltViewModel
class MeetUpListViewModel @Inject constructor(
    private val meetUpRepository: MeetUpRepository
) : ViewModel() {
    val listOfMeetUp: LiveData<List<MeetUp>> = meetUpRepository.getAllMeetUps().asLiveData()
    private val _tags: MutableLiveData<Set<Category>> = MutableLiveData()
    val tags: LiveData<Set<Category>> = _tags

    /**
     *  Provides the tagContainer with the necessary tags and allows it to edit them.
     */
    val tagRepository = object : TagsRepository<Category> {
        override val tags: Set<Category>
            get() = _tags.value ?: setOf()

        override val isEditable = true
        override val allTags = Category.values().toSet()

        override fun removeTag(tag: Category): Boolean {
            val tags = _tags.value
            return if (tags == null || !tags.contains(tag)) {
                false
            } else {
                _tags.value = tags.minus(tag)
                true
            }
        }

        override fun addTag(tag: Category): Boolean {
            val tags = _tags.value
            return if (tags == null || tags.contains(tag)) {
                false
            } else {
                _tags.value = tags.plus(tag)
                true
            }
        }
    }
}