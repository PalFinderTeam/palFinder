package com.github.palFinderTeam.palfinder.profile.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.meetupList.MeetupListRootAdapter
import com.github.palFinderTeam.palfinder.profile.services.ProfileService
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

    private val _loggedProfile: MutableLiveData<Response<ProfileUser>> = MutableLiveData()
    val loggedProfile: LiveData<Response<ProfileUser>> = _loggedProfile

    /**
     * Fetch user profile and post its value
     * @param userId
     */
    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            fetchProfileInto(userId, _profile)
        }
    }

    fun fetchLoggedProfile() {
        viewModelScope.launch {
            val logged = profileService.getLoggedInUserID()
            if (logged != null) {
                fetchProfileInto(logged, _loggedProfile)
            }
        }
    }

    private suspend fun fetchProfileInto(userId: String, dest: MutableLiveData<Response<ProfileUser>>) {
        profileService.fetchFlow(userId).collect {
            dest.postValue(it)
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

    private suspend fun refresh(userId: String) {
        val logged = profileService.getLoggedInUserID()
        if (logged != null) {
            fetchProfileInto(logged, _loggedProfile)
        }
        fetchProfileInto(userId, _profile)
    }

    /**
     * We handle the follow/unfollow logic here to avoid handling coroutines in
     * the adapter. Notice that we need to fetch again the user who wants to follow,
     * that's because we need to have the more up to date info.
     */
    fun followUnFollow(userId: String, otherId: String, follow: Boolean) {
        viewModelScope.launch {
            profileService.fetch(userId)?.let {
                if (follow) {
                    profileService.followUser(it, otherId)
                } else {
                    profileService.unfollowUser(it, otherId)
                }
            }
            refresh(otherId)
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
            refresh(otherId)
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
            refresh(otherId)
        }
    }
}