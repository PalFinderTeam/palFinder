package com.github.palFinderTeam.palfinder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.MeetupListRootAdapter
import com.github.palFinderTeam.palfinder.meetups.*
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
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
            profileService.fetchFlow(userId).collect {
                _profile.postValue(it)
            }
        }
    }

    /**
     * Fetch user profiles and post the values in the list
     * @param usersIds list of IDs
     */
    fun fetchUsersProfile(usersIds: List<String>) {
        viewModelScope.launch {
            _profilesList.postValue(profileService.fetch(usersIds))
        }
    }

    /**
     * Get the list of meetups of a user expose it through [meetupDataSet]
     * @param userId The ID of a user
     */
    fun fetchUserMeetups(userId: String) {
        viewModelScope.launch {
            meetUpService.getUserMeetups(userId).collect { resp ->
                if (resp is Response.Success) {
                    _meetupDataSet.postValue(resp)
                }
            }
        }
    }

    /**
     * We handle the follow/unfollow logic here to avoid handling coroutines in
     * the adapter. Notice that we need to fetch again the user who wants to follow,
     * that's because we need to have the more up to date info.
     */
    fun follow(userId: String, otherId: String) {
        viewModelScope.launch {
            profileService.fetch(userId)?.let {
                profileService.followUser(it, otherId)
            }
        }
    }
    fun unFollow(userId: String, otherId: String) {
        viewModelScope.launch {
            profileService.fetch(userId)?.let {
                profileService.unfollowUser(it, otherId)
            }
        }
    }

}