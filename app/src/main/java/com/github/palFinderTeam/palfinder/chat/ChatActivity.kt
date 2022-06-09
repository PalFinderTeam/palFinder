package com.github.palFinderTeam.palfinder.chat

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.PalFinderBaseActivity
import com.github.palFinderTeam.palfinder.R
import dagger.hilt.android.AndroidEntryPoint

const val CHAT = "com.github.palFinderTeam.palFinder.meetup_view.CHAT"

/**
 * chat activity, that allows the user to speak with other people
 */
@AndroidEntryPoint
class ChatActivity : PalFinderBaseActivity() {
    private lateinit var chatList: RecyclerView
    lateinit var adapter: ChatMessageListAdapter
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var chatBox: TextView

    /**
     * load the chat to view
     */
    private fun loadChat() {
        if (intent.hasExtra(CHAT)) {
            val meetupId = intent.getStringExtra(CHAT)
            viewModel.connectToChat(meetupId!!)
            currentlyViewChat = meetupId
        }
    }

    /**
     * on pause, set current chat to null
     */
    override fun onPause() {
        super.onPause()
        currentlyViewChat = null
    }

    /**
     * on resume, load the chat
     */
    override fun onResume() {
        super.onResume()
        loadChat()
    }

    /**
     * on destroy, set current chat to null
     */
    override fun onDestroy() {
        super.onDestroy()
        currentlyViewChat = null
    }

    /**
     * on create, load the chat and create a fragment listing all messages
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        loadChat()

        chatList = findViewById(R.id.chat_list)
        chatList.layoutManager = LinearLayoutManager(this)

        viewModel.listOfMessage.observe(this) { messages ->
            adapter = ChatMessageListAdapter(viewModel, messages)
            chatList.adapter = adapter
            chatList.scrollToPosition(messages.size - 1)
        }

        chatBox = findViewById(R.id.et_ChatMessageEdit)

        findViewById<View>(R.id.bt_SendMessage).setOnClickListener{ onMessageSend() }
    }

    /**
     * send the content of the chatbox as a new message and remove the text from the chatbox
     */
    private fun onMessageSend() {
        if (chatBox.text.toString() != "") {
            viewModel.sendMessage(chatBox.text.toString())
            chatBox.text = ""
        }
    }

    companion object {
        var currentlyViewChat: String? = ""
    }
}