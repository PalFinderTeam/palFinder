package com.github.palFinderTeam.palfinder.meetups.meetupView

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.MeetUpRepository
import com.github.palFinderTeam.palfinder.profile.services.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tags.Category
import com.github.palFinderTeam.palfinder.tags.TagsRepository
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * viewModel attached to the MeetupView, mainly to fetch the meetUp and fill the fields of the layout
 * @param meetUpRepository the database for the meetups from which we fetch
 * @param profileService the database for the profiles mainly to get the currentLoggedUser
 * @param timeService database to retrieve the current time so we don't display outdated meetups
 */
@HiltViewModel
class MeetUpViewViewModel @Inject constructor(
    private val meetUpRepository: MeetUpRepository,
    private val profileService: ProfileService,
    private val timeService: TimeService
) : ViewModel() {
    private var _meetUp: MutableLiveData<MeetUp> = MutableLiveData<MeetUp>()
    val meetUp: LiveData<MeetUp> = _meetUp
    private var _askPermissionToJoin: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val askPermissionToJoin: LiveData<Boolean> = _askPermissionToJoin
    var loggedInUser: MutableLiveData<ProfileUser?> = MutableLiveData<ProfileUser?>()

    /**
     * Fetch given meetup and update corresponding livedata.
     *
     * @param meetUpId Id of the meetup to be fetched.
     */
    fun loadMeetUp(meetUpId: String) {
        viewModelScope.launch {
            val id = profileService.getLoggedInUserID()
            loggedInUser.value = if (id != null){
                profileService.fetch(id)
            }
            else{
                null
            }

            val fetchedMeetUp = meetUpRepository.fetch(meetUpId)
            // TODO do something on error
            fetchedMeetUp?.let { _meetUp.value = it }
        }
    }

    fun getMeetupID(): String{
        return meetUp.value!!.uuid
    }

    /**
     * get username of a user profile and process a function on it
     * @param uuid user unique identifier
     * @param callback function to process on the user username
     */
    fun getUsernameOf(uuid: String, callback: (String?)->Unit){
        viewModelScope.launch {
            val user = profileService.fetch(uuid)?.username
            callback(user)
        }
    }

    /**
     * helper functions for the chat/edit/join/openNavigation buttons
     */
    fun hasJoin(): Boolean {
        val uuid = profileService.getLoggedInUserID()
        return if (uuid != null && meetUp.value != null){
            meetUp.value!!.isParticipating(uuid)
        }
        else{
            false
        }
    }
    fun isCreator(): Boolean {
        val uuid = profileService.getLoggedInUserID()
        return if (uuid != null && meetUp.value != null){
            meetUp.value!!.creatorId == uuid
        }
        else{
            false
        }
    }
    fun canMute(): Boolean{
        return if (loggedInUser.value != null && meetUp.value != null){
            loggedInUser.value!!.canMuteMeetup(meetUp.value!!.uuid)
        } else{
            false
        }
    }
    fun canUnMute(): Boolean{
        return if (loggedInUser.value != null && meetUp.value != null){
            loggedInUser.value!!.canUnMuteMeetup(meetUp.value!!.uuid)
        } else{
            false
        }
    }

    /**
     * test if the user is participating in the meetup and propagate the join/leave to the database
     */
    fun joinOrLeave(context: Context, ignoreWarning: Boolean = false){
        val uuid = profileService.getLoggedInUserID()
        if (uuid != null) {
            viewModelScope.launch {
                if (hasJoin()) {
                    when(val ret = meetUpRepository.leaveMeetUp(meetUp.value!!.uuid, uuid)){
                        is Response.Failure -> Toast.makeText(context, ret.errorMessage, Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(context, R.string.meetup_view_left, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val meetUp = meetUp.value!!
                    val loggedUser = profileService.fetch(uuid)!!
                    val blockedUser = profileService.fetch(uuid)?.blockedUsers
                    val blockedByCreator = profileService.fetch(meetUp.creatorId)?.blockedUsers
                    if (blockedUser?.contains(meetUp.creatorId) == true) {
                        Toast.makeText(context, R.string.meetup_with_blocked, Toast.LENGTH_SHORT).show()
                    }
                    else if (blockedByCreator?.contains(uuid) == true) {
                        Toast.makeText(context, R.string.meetup_where_you_are_blocked, Toast.LENGTH_SHORT).show()
                    }
                    else if (!ignoreWarning && blockedUser?.intersect(meetUp.participantsId.toSet())?.isEmpty() == false) {
                        _askPermissionToJoin.value = true
                    }
                    else {
                        when(val ret = meetUpRepository.joinMeetUp(meetUp.uuid, uuid, timeService.now(),
                            loggedUser)){
                            is Response.Failure -> Toast.makeText(context, ret.errorMessage, Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(context, R.string.meetup_view_joined, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                loadMeetUp(meetUp.value!!.uuid)
            }
        }
    }

    /**
     * test if the meetup is muted and propagate the mute/unmute to the database
     */
    fun muteOrUnMute(context: Context){
        val uuid = profileService.getLoggedInUserID()
        if (uuid != null) {
            viewModelScope.launch {
                if (canMute()) {
                    when(val ret = profileService.muteMeetup(loggedInUser.value!!, meetUp.value!!.uuid)){
                        is Response.Failure -> Toast.makeText(context, ret.errorMessage, Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(context, R.string.meetup_view_muted, Toast.LENGTH_SHORT).show()
                    }
                } else if (canUnMute()) {
                    when(val ret = profileService.unMuteMeetup(loggedInUser.value!!, meetUp.value!!.uuid)){
                        is Response.Failure -> Toast.makeText(context, ret.errorMessage, Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(context, R.string.meetup_view_unmuted, Toast.LENGTH_SHORT).show()
                    }
                }
                loadMeetUp(meetUp.value!!.uuid)
            }
        }
    }

    /**
     * Describe how tags should be transferred from this viewModel to the tag viewModel.
     */
    val tagRepository = object : TagsRepository<Category> {
        override val tags: Set<Category>
            get() = meetUp.value?.tags ?: setOf()

        override val isEditable = false
        override val allTags = Category.values().toSet()

        override fun removeTag(tag: Category): Boolean = false

        override fun addTag(tag: Category): Boolean = false
    }
}