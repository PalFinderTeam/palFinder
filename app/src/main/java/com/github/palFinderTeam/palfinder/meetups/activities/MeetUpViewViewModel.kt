package com.github.palFinderTeam.palfinder.meetups.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MeetUpViewViewModel @Inject constructor(
    private val meetUpRepository: MeetUpRepository
) : ViewModel() {
    private var _meetUp: MutableLiveData<MeetUp> = MutableLiveData<MeetUp>()
    val meetUp: LiveData<MeetUp> = _meetUp

    /**
     * Fetch given meetup and update corresponding livedata.
     *
     * @param meetUpId Id of the meetup to be fetched.
     */
    fun loadMeetUp(meetUpId: String) {
        viewModelScope.launch {
            val fetchedMeetUp = meetUpRepository.getMeetUpData(meetUpId)
            // TODO do something on error
            fetchedMeetUp?.let { _meetUp.value = it }
        }
    }

    fun getMeetupID(): String{
        return meetUp.value!!.uuid
    }

    /**
     * Describe how tags should be transferred from this viewModel to the tag viewModel.
     */
    val tagRepository = object : TagsRepository<Category> {
        override val tags: Set<Category>
            get() = meetUp.value?.tags ?: setOf()

        override val isEditable = false
        override val allTags = Category.values().toSet()

        override fun removeTag(tag: Category): Boolean = false

        override fun addTag(tag: Category): Boolean = false
    }
}