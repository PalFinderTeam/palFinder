package com.github.palFinderTeam.palfinder.chat

import android.icu.util.Calendar
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private var chatService: ChatService,
    private val profileService: ProfileService
    ): ViewModel() {

    val listOfMessage: MutableLiveData<List<ChatMessage>> = MutableLiveData()
    val profilesData = HashMap<String, ProfileUser>()

    private lateinit var chatID: String

    fun sendMessage(content: String){
        val user = Firebase.auth.currentUser
        if (user != null) {
            val userID = user.uid
            val msg = ChatMessage(Calendar.getInstance(), userID, content, false)

            viewModelScope.launch {
                chatService.postMessage(chatID, msg)
            }
        }
    }

    fun fetchMessages(chatID: String){
        this.chatID = chatID
        listOfMessage.value = mutableListOf()
        viewModelScope.launch {
            chatService.getAllMessageFromChat(chatID).collect {
                listOfMessage.postValue(it.sortedBy { it.sentAt.time })
            }
        }
    }

    suspend fun loadProfileData(userId: String, pictureBox: ImageView, nameBox: TextView){
        if (!profilesData.containsKey(userId)){
            viewModelScope.launch {
                profileService.fetchProfileFlow(userId).collect {
                    if (it is Response.Success){
                        profilesData[userId] = it.data
                        nameBox.text = it.data.username
                        it.data.pfp.loadImageInto(pictureBox)
                    }
                }
            }
        }
        else{
            nameBox.text = profilesData[userId]!!.username
            profilesData[userId]!!.pfp.loadImageInto(pictureBox)
        }
    }
}