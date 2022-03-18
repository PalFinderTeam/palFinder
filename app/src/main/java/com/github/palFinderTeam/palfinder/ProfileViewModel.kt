package com.github.palFinderTeam.palfinder

import android.widget.Toast
import androidx.lifecycle.*
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileService: ProfileService
) : ViewModel() {

    private val _profile: MutableLiveData<ProfileUser> = MutableLiveData()
    val profile: LiveData<ProfileUser> = _profile


    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            val res = profileService.fetchUserProfile(userId)?.let {
                _profile.postValue(it)
            }
            if (res == null) {
                // TODO show error message
            }
        }
    }
}