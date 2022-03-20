package com.github.palFinderTeam.palfinder.chat

import android.icu.util.Calendar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel: ViewModel() {
    val listOfMessage: MutableLiveData<MutableList<ChatMessage>> = MutableLiveData()

    fun sendMessage(content: String){
        // TODO Replace with logged user
        val user = "user"
        val msg = ChatMessage(Calendar.getInstance(), user, content, false)
        listOfMessage.value!!.add(msg)
        listOfMessage.postValue(listOfMessage.value!!)
    }
}