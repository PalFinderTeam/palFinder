package com.github.palFinderTeam.palfinder.meetups.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MeetUpListViewModel @Inject constructor(
    private val meetUpRepository: MeetUpRepository
) : ViewModel() {

    val listOfMeetUp: LiveData<List<MeetUp>> = meetUpRepository.getAllMeetUps().asLiveData()
}