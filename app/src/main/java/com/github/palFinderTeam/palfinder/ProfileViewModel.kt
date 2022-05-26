package com.github.palFinderTeam.palfinder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.MeetupListRootAdapter
import com.github.palFinderTeam.palfinder.meetups.*
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel.Companion.TEXT_FILTER
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.BLOCKED_USERS
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.transformer.ListTransformer
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
    val filterer = ListTransformer<ProfileUser>()
    private val _profilesList: MutableLiveData<List<ProfileUser>> =
        MutableLiveData<List<ProfileUser>>(listOf())
    val profilesList: MutableLiveData<List<ProfileUser>> = filterer.transform(_profilesList)

    // For the User's meetup list
    private var _adapter: MutableLiveData<MeetupListRootAdapter> = MutableLiveData()
    var adapter: LiveData<MeetupListRootAdapter> = _adapter

    // Meetups data
    private var _meetupDataSet: MutableLiveData<Response<List<MeetUp>>> = MutableLiveData()
    var meetupDataSet: LiveData<Response<List<MeetUp>>> = _meetupDataSet

    private val _logged_profile: MutableLiveData<Response<ProfileUser>> = MutableLiveData()
    val logged_profile: LiveData<Response<ProfileUser>> = _logged_profile

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

    fun fetchLoggedProfile() {
        viewModelScope.launch {
            if (profileService.getLoggedInUserID() != null) {
                profileService.fetchFlow(profileService.getLoggedInUserID()!!).collect {
                    _logged_profile.postValue(it)
                }
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

    /**
     * We handle the block/unblock logic here to avoid handling coroutines in
     * the adapter. Notice that we need to fetch again the user who wants to follow,
     * that's because we need to have the more up to date info.
     */
    fun block(userId: String, otherId: String) {
        viewModelScope.launch {
            profileService.fetch(userId)?.let { user ->
                // Sanity check
                if (userId != otherId && !user.blockedUsers.contains(otherId)) {
                    val newBlockList = user.blockedUsers.plus(otherId)
                    profileService.unfollowUser(user, otherId)
                    profileService.edit(user.uuid, BLOCKED_USERS, newBlockList)
                    // Leave all meetups organize by blocked user
                    for (meetupId in user.joinedMeetUps) {
                        meetUpService.fetch(meetupId)?.let { meetUp ->
                            if (meetUp.creatorId == otherId) {
                                meetUpService.leaveMeetUp(meetUp.uuid, user.uuid)
                            }
                        }
                    }
                }
            }
        }
    }
    fun unBlock(userId: String, otherId: String) {
        viewModelScope.launch {
            profileService.fetch(userId)?.let {
                // Sanity check
                if (userId != otherId && it.blockedUsers.contains(otherId)) {
                    profileService.edit(it.uuid, BLOCKED_USERS, it.blockedUsers.minus(otherId))
                }
            }
        }
    }
}