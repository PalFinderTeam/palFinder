package com.github.palFinderTeam.palfinder.meetups.activities

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.palFinderTeam.palfinder.meetups.MeetUp

class MeetUpViewViewModel: ViewModel() {
    val meetUp: MutableLiveData<MeetUp> by lazy {
        MutableLiveData<MeetUp>()
    }

    /**
     * Update change meetup for [newMeetUp]
     */
    fun changeMeetup(newMeetUp: MeetUp){
        this.meetUp.value = newMeetUp
    }
}