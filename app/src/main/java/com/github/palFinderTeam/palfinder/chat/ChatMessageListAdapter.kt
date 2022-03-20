package com.github.palFinderTeam.palfinder.chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.ProfileActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.USER_ID
import com.github.palFinderTeam.palfinder.utils.PrettyDate
import com.github.palFinderTeam.palfinder.utils.SearchedFilter

class ChatMessageListAdapter(private val viewModel: ChatViewModel, private val dataSet: List<ChatMessage>, private val onItemClicked: (position: Int) -> Unit) :
    RecyclerView.Adapter<ChatMessageListAdapter.ViewHolder>(), Filterable {
    private val currentDataSet = dataSet.toMutableList()

    class ViewHolder(view: View, private val onItemClicked: (position: Int) -> Unit) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        val messageContent: TextView = view.findViewById(R.id.msg_text)
        val messageDate: TextView = view.findViewById(R.id.msg_date)
        val messageSenderName: TextView = view.findViewById(R.id.msg_sender_name)
        val messageSenderPic: ImageView = view.findViewById(R.id.msg_picture)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            onItemClicked(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        //create a new view for each meetup
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.message_recieved_listview, parent, false)
        ) {
            val item = currentDataSet[it]
            val originalItemPos = dataSet.indexOf(item)

            onItemClicked(originalItemPos)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prettyDate = PrettyDate()
        holder.messageContent.text = currentDataSet[position].content
        holder.messageDate.text = prettyDate.timeDiff(currentDataSet[position].sentAt)
        holder.messageSenderName.text = currentDataSet[position].sentBy
        holder.messageSenderPic.setOnClickListener{
            val intent = Intent(it.context, ProfileActivity::class.java).apply {
                putExtra(USER_ID, "Ze3Wyf0qgVaR1xb9BmOqPmDJsYd2")
            }
            startActivity(it.context, intent, null)
        }
        //holder.messageContent.text = currentDataSet[position].content
    }

    override fun getItemCount(): Int = currentDataSet.size

    override fun getFilter(): Filter =
        SearchedFilter(dataSet, currentDataSet, { notifyDataSetChanged() })
}