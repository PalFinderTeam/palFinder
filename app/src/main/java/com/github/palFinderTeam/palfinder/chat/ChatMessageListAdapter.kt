package com.github.palFinderTeam.palfinder.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.profile.ProfileActivity
import com.github.palFinderTeam.palfinder.profile.USER_ID
import com.github.palFinderTeam.palfinder.utils.time.PrettyDate
import com.github.palFinderTeam.palfinder.utils.time.ShortDate
import kotlinx.coroutines.launch


class ChatMessageListAdapter(
    private val viewModel: ChatViewModel,
    dataSet: List<ChatMessage>
) :
    RecyclerView.Adapter<ChatMessageListAdapter.ViewHolder>() {

    private val currentDataSet = dataSet.toMutableList()

    class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

//        val messageInLayout: ConstraintLayout = view.findViewById(R.id.msg_in_layout)

        val messageInContent: TextView = view.findViewById(R.id.msg_in_text)
        val messageInDate: TextView = view.findViewById(R.id.msg_in_date)
        val messageInSenderName: TextView = view.findViewById(R.id.msg_in_sender_name)
        val messageInSenderPic: ImageView = view.findViewById(R.id.msg_send_picture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        //create a new view for each message
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.message_listview, parent, false)
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prettyDate = PrettyDate()
        val msg = currentDataSet[position]
        val context = holder.messageInContent.context
        val previous = currentDataSet.getOrNull(position - 1)

        // Show image only on others and avoid always showing image on chain of messages
        val showPicture =
            !(msg.sentBy == viewModel.profileService.getLoggedInUserID() || previous?.sentBy == msg.sentBy)

        val showName = previous?.sentBy != msg.sentBy

//        if (msg.sentBy == viewModel.profileService.getLoggedInUserID()) {
//            holder.messageInLayout.background = context.getDrawable(R.drawable.out_going_message)
//        } else {
//            holder.messageInLayout.background = context.getDrawable(R.drawable.in_coming_message)
//        }
        holder.messageInContent.text = msg.content
        holder.messageInDate.text = ShortDate.format(context, msg.sentAt, Calendar.getInstance())

        holder.messageInSenderPic.setOnClickListener {
            val intent = Intent(it.context, ProfileActivity::class.java).apply {
                putExtra(USER_ID, msg.sentBy)
            }
            ContextCompat.startActivity(it.context, intent, null)
        }
        viewModel.viewModelScope.launch {
            viewModel.loadProfileData(
                msg.sentBy,
                holder.messageInSenderPic,
                holder.messageInSenderName,
                showPicture,
                showName
            )
        }
    }

    override fun getItemCount(): Int = currentDataSet.size
}