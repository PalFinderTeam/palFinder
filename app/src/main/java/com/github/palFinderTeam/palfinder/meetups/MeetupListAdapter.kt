package com.github.palFinderTeam.palfinder.meetups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.PrettyDate
import com.github.palFinderTeam.palfinder.utils.SearchedFilter


class MeetupListAdapter(private val dataSet: List<MeetUp>, private val onItemClicked: (position: Int) -> Unit) :
    RecyclerView.Adapter<MeetupListAdapter.ViewHolder>(), Filterable {
    val currentDataSet = dataSet.toMutableList()

    class ViewHolder(view: View, private val onItemClicked: (position: Int) -> Unit) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        //TODO - add some remaining fields to display
        val meetupTitle: TextView = view.findViewById(R.id.meetup_title)
        val meetupDate: TextView = view.findViewById(R.id.date)
        val meetupDescription: TextView = view.findViewById(R.id.meetup_description)
        val meetupNumberParticipants: TextView = view.findViewById(R.id.number_participants)

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
            LayoutInflater.from(parent.context).inflate(R.layout.meetup_listview, parent, false)
        ) {
            val item = currentDataSet[it]
            val originalItemPos = dataSet.indexOf(item)

            onItemClicked(originalItemPos)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //update displayed elements with the content of the current dataset
        val meetupTitle = holder.meetupTitle
        meetupTitle.text = currentDataSet[position].name
        val meetupDate = holder.meetupDate
        val prettyDate = PrettyDate()

        meetupDate.text = prettyDate.timeDiff(currentDataSet[position].startDate)
        val meetupDescription = holder.meetupDescription
        meetupDescription.text = currentDataSet[position].description
        val meetupNumberParticipants = holder.meetupNumberParticipants
        meetupNumberParticipants.text = currentDataSet[position].capacity.toString()
    }

    override fun getItemCount(): Int = currentDataSet.size

    override fun getFilter(): Filter =
        SearchedFilter(dataSet, currentDataSet, { notifyDataSetChanged() })
}



