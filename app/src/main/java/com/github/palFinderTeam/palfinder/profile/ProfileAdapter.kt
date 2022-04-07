package com.github.palFinderTeam.palfinder.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileAdapter(private val dataSet: List<ProfileUser>, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>(),
    Filterable {
    val currentDataSet = dataSet.toMutableList()

    class ViewHolder(view: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val name: TextView = view.findViewById(R.id.profile_name)
        val fullName: TextView = view.findViewById(R.id.fullName)
        val pic: ImageView = view.findViewById(R.id.userPic)
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
        val fullName = holder.fullName
        fullName.text = currentDataSet[position].fullName()
        name.text = currentDataSet[position].username
        CoroutineScope(Dispatchers.IO).launch { currentDataSet[position].pfp.loadImageInto(holder.pic) }
    }

    override fun getItemCount(): Int = currentDataSet.size
    override fun getFilter(): Filter = SearchedFilter(dataSet, currentDataSet,null) { notifyDataSetChanged() }

}