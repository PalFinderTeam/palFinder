package com.github.palFinderTeam.palfinder

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.MeetupListAdapter
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
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
    private val profileService: ProfileService,
    //private val meetUpService: MeetUpRepository
) : ViewModel() {


    private val _profile: MutableLiveData<Response<ProfileUser>> = MutableLiveData()
    val profile: LiveData<Response<ProfileUser>> = _profile
    private val _profilesList: MutableLiveData<List<ProfileUser>> =
        MutableLiveData<List<ProfileUser>>(listOf())
    val profilesList: LiveData<List<ProfileUser>> = _profilesList

    // For the User's meetup list
    lateinit var adapter: MeetupListAdapter
    private val _listOfMeetUpResponse: MutableLiveData<Response<List<MeetUp>>> = MutableLiveData()
    val listOfMeetUpResponse: LiveData<Response<List<MeetUp>>> = _listOfMeetUpResponse

    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            profileService.fetchProfileFlow(userId).collect {
                _profile.postValue(it)
            }
        }
        profile.value
    }

    fun fetchUsersProfile(usersIds : List<String>) {
        viewModelScope.launch {
            _profilesList.postValue(profileService.fetchUsersProfile(usersIds))
        }
    }

//    /**
//     * Get the list of meetups of a user
//     * @param userId The ID of a user
//     */
//    fun fetchUserMeetups(userId: String){
//        viewModelScope.launch {
//
//            // TODO: change to user list when Zac implements it
//            meetUpService.getAllMeetUpsResponse().collect{
//
//                if (it is Response.Success) {
//                    val userMeetups = it.data
//
//                    adapter = MeetupListAdapter(
//                        userMeetups, userMeetups.toMutableList(),
//                        SearchedFilter(
//                            userMeetups, userMeetups.toMutableList(), ::filterTags
//                        ) {
//                            adapter.notifyDataSetChanged()
//                        },
//                        viewModel.searchLocation.value!!
//                    )
//
//                    _listOfMeetUpResponse.postValue(it)
//                }
//
//            }
//        }
//    }
//
//    private fun filterTags(meetup: MeetUp): Boolean {
//        return true
//    }


}