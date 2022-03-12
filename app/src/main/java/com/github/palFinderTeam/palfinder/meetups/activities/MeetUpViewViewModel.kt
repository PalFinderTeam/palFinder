package com.github.palFinderTeam.palfinder.meetups.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import kotlinx.coroutines.launch

class MeetUpViewViewModel: ViewModel() {
    private var _meetUp: MutableLiveData<MeetUp> = MutableLiveData<MeetUp>()
    val meetUp: LiveData<MeetUp> = _meetUp

    /**
     * Fetch given meetup and update corresponding livedata.
     *
     * @param meetUpId Id of the meetup to be fetched.
     */
    fun loadMeetUp(meetUpId: String) {
        viewModelScope.launch {
            val fetchedMeetUp = FirebaseMeetUpService.getMeetUpData(meetUpId)
            if (fetchedMeetUp != null) {
                _meetUp.value = fetchedMeetUp
            }
        }
    }
}