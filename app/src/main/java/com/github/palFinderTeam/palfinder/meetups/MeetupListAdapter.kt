package com.github.palFinderTeam.palfinder.meetups

import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R


class MeetupListAdapter<T : MeetUp>(private val dataSet : List<MeetUp>): RecyclerView.Adapter<MeetupListAdapter.ViewHolder>(), Filterable {
    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val meetup_title: TextView = view.findViewById(R.id.meetup_title)
        val meetup_date: TextView = view.findViewById(R.id.date)
        val meetup_description: TextView = view.findViewById(R.id.meetup_description)
        val meetup_number_participants: TextView = view.findViewById(R.id.number_participants)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getFilter(): Filter {
        TODO("Not yet implemented")
    }
}



