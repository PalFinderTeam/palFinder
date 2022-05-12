package com.github.palFinderTeam.palfinder.chat

import android.content.Context
import android.content.SharedPreferences
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
        val sharedPref = getSharedPreferences("theme", Context.MODE_PRIVATE) ?: return
        val theme = sharedPref.getInt("theme", R.style.palFinder_default_theme)
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        var sharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "theme") {
                    recreate()
                }
            }
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    private fun loadChat(){
        if (intent.hasExtra(CHAT)) {
            val meetupId = intent.getStringExtra(CHAT)
            viewModel.connectToChat(meetupId!!)
            currentlyViewChat = meetupId
        }
    }

    override fun onPause() {
        super.onPause()
        currentlyViewChat = null
    }

    override fun onResume() {
        super.onResume()
        loadChat()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentlyViewChat = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        loadChat()

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

    companion object{
        var currentlyViewChat: String? = ""
    }
}