package com.github.palFinderTeam.palfinder.profile

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileAdapter(private val dataSet: List<ProfileUser>, private val loggedUser: ProfileUser, private val profileService: ProfileService, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>(),
    Filterable {
    val currentDataSet = dataSet.toMutableList()

    class ViewHolder(view: View, private val follow:(position: Int) -> Unit,private val unfollow:(position: Int) -> Unit, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val name: TextView = view.findViewById(R.id.profile_name)
        val fullName: TextView = view.findViewById(R.id.fullName)
        val pic: ImageView = view.findViewById(R.id.userPic)
        private val followButton: Button = view.findViewById(R.id.followButton)
        private val unfollowButton: Button = view.findViewById(R.id.unfollowButton)
        init {
            view.setOnClickListener(this)
            followButton.setOnClickListener {
                follow(adapterPosition)
                Log.d("agent", adapterPosition.toString())
            }
            unfollowButton.setOnClickListener {
                unfollow(adapterPosition)
            }
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            onItemClicked(position)
        }


    }

    private fun follow(position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            profileService.followUser(loggedUser, currentDataSet[position].uuid)
        }
    }

    private fun unfollow(position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            profileService.unfollowUser(loggedUser, currentDataSet[position].uuid)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.profile_row_item, parent, false), {
            val item = currentDataSet[it]
            val originalItemPos = dataSet.indexOf(item)
            follow(originalItemPos)
        }, {
            val item = currentDataSet[it]
            val originalItemPos = dataSet.indexOf(item)
            unfollow(originalItemPos)
        })
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