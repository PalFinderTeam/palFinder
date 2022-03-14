package com.github.palFinderTeam.palfinder.meetups

import android.annotation.SuppressLint
import android.util.Log
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


class MeetupListAdapter<T : MeetUp>(private val dataSet : List<T>): RecyclerView.Adapter<MeetupListAdapter.ViewHolder>(), Filterable {
    val currentDataSet = dataSet.toMutableList()

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        //TODO - add some remaining fields to display
        val meetup_title: TextView = view.findViewById(R.id.meetup_title)
        val meetup_date: TextView = view.findViewById(R.id.date)
        val meetup_description: TextView = view.findViewById(R.id.meetup_description)
        val meetup_number_participants: TextView = view.findViewById(R.id.number_participants)
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        //create a new view for each meetup
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.meetup_listview, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //update displayed elements with the content of the current dataset
        val meetup_title = holder.meetup_title
        meetup_title.text = currentDataSet[position].name
        val meetup_date = holder.meetup_date
        val prettyDate = PrettyDate()
        meetup_date.text = prettyDate.timeDiff(currentDataSet[position].startDate)
        val meetup_description = holder.meetup_description
        meetup_description.text = currentDataSet[position].description
        val meetup_number_participants = holder.meetup_number_participants
        meetup_number_participants.text = currentDataSet[position].capacity.toString()

    }

    override fun getItemCount(): Int = currentDataSet.size

    override fun getFilter(): Filter = SearchedFilter(dataSet, currentDataSet, { notifyDataSetChanged() })
}



