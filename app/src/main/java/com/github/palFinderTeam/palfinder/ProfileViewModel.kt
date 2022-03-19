package com.github.palFinderTeam.palfinder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileService: ProfileService
) : ViewModel() {

    private val _profile: MutableLiveData<Response<ProfileUser>> = MutableLiveData()
    val profile: LiveData<Response<ProfileUser>> = _profile


    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            profileService.fetchProfileFlow(userId).collect {
                _profile.postValue(it)
            }
        }
    }
}