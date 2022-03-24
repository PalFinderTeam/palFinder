package com.github.palFinderTeam.palfinder.chat

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import dagger.hilt.android.AndroidEntryPoint

const val CHAT = "com.github.palFinderTeam.palFinder.meetup_view.CHAT"

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    private lateinit var chatList: RecyclerView
    lateinit var adapter: ChatMessageListAdapter
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var chatBox: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        if (intent.hasExtra(CHAT)) {
            val meetupId = intent.getStringExtra(CHAT)
            viewModel.connectToChat(meetupId!!)
        }
        chatList = findViewById(R.id.chat_list)
        chatList.layoutManager = LinearLayoutManager(this)

        viewModel.listOfMessage.observe(this) { messages ->
            adapter = ChatMessageListAdapter(viewModel, messages) { onListItemClick(it) }
            chatList.adapter = adapter
            chatList.scrollToPosition(messages.size - 1)
        }

        chatBox = findViewById(R.id.et_ChatMessageEdit)
    }

    private fun onListItemClick(position: Int) {

    }

    fun onMessageSend(v: View){
        if (chatBox.text.toString() != "") {
            viewModel.sendMessage(chatBox.text.toString())
            chatBox.text = ""
        }
    }
}