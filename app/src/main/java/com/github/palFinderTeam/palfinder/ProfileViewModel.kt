package com.github.palFinderTeam.palfinder

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.meetups.*
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val profileService: ProfileService,
    private val meetUpService: MeetUpRepository
) : ViewModel() {

    // For one user
    private val _profile: MutableLiveData<Response<ProfileUser>> = MutableLiveData()
    val profile: LiveData<Response<ProfileUser>> = _profile

    // For multiple users
    private val _profilesList: MutableLiveData<List<ProfileUser>> =
        MutableLiveData<List<ProfileUser>>(listOf())
    val profilesList: LiveData<List<ProfileUser>> = _profilesList

    // For the User's meetup list
    private var _adapter: MutableLiveData<MeetupListRootAdapter> = MutableLiveData()
    var adapter: LiveData<MeetupListRootAdapter> = _adapter

    // Meetups data
    private var _meetupDataSet: MutableLiveData<Response<List<MeetUp>>> = MutableLiveData()
    var meetupDataSet: LiveData<Response<List<MeetUp>>> = _meetupDataSet

    /**
     * Fetch user profile and post its value
     * @param userId
     */
    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            profileService.fetchProfileFlow(userId).collect {
                _profile.postValue(it)
            }
        }
        profile.value
    }

    /**
     * Fetch user profiles and post the values in the list
     * @param usersIds list of IDs
     */
    fun fetchUsersProfile(usersIds : List<String>) {
        viewModelScope.launch {
            _profilesList.postValue(profileService.fetchUsersProfile(usersIds))
        }
    }

    /**
     * Get the list of meetups of a user and put it into the adapter
     * @param userId The ID of a user
     */
    fun createAdapter(userId: String) {
        viewModelScope.launch {
            // TODO: change to user list when Zac implements it
            meetUpService.getUserMeetups(userId).collect{ resp ->
                if (resp is Response.Success) {
                    _meetupDataSet.postValue(resp)
                }
            }
        }
    }

}