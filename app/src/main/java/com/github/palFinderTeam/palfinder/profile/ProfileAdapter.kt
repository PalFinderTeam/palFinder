package com.github.palFinderTeam.palfinder.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import com.google.android.material.chip.Chip

class ProfileAdapter<T: ProfileUser>(private val dataSet: List<T>, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>(),
    Filterable {
    val currentDataSet = dataSet.toMutableList()

    class ViewHolder(view: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val name: TextView = view.findViewById(R.id.profile_name)
        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            onItemClicked(position)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.profile_row_item, parent, false))
            {
                val item = currentDataSet[it]
                val originalItemPos = dataSet.indexOf(item)
                onItemClicked(originalItemPos)
            }
    }

    override fun onBindViewHolder(holder: ProfileAdapter.ViewHolder, position: Int) {
        val name = holder.name
        name.text = currentDataSet[position].username
    }

    override fun getItemCount(): Int = currentDataSet.size
    override fun getFilter(): Filter = SearchedFilter(dataSet, currentDataSet,null) { notifyDataSetChanged() }

}