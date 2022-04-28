package com.github.palFinderTeam.palfinder.profile

import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.log

class ProfileAdapter(private val dataSet: List<ProfileUser>, private var loggedUser: ProfileUser, private val profileService: ProfileService, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>(),
    Filterable {
    val currentDataSet = dataSet.toMutableList()

    class ViewHolder(val view: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val name: TextView = view.findViewById(R.id.profile_name)
        val fullName: TextView = view.findViewById(R.id.fullName)
        val pic: ImageView = view.findViewById(R.id.userPic)
        val followButton: Button = view.findViewById(R.id.followButton)
        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            onItemClicked(position)
        }


    }

    private fun follow(position: Int, holder: ViewHolder) {
        CoroutineScope(Dispatchers.IO).launch {
            profileService.followUser(loggedUser, currentDataSet[position].uuid)
        }
        holder.followButton.text = holder.view.resources.getString(R.string.unfollow_button)
    }

    private fun canFollow(position: Int): Boolean {
        return loggedUser.canFollow(currentDataSet[position].uuid)
    }

    private fun unfollow(position: Int, holder: ViewHolder) {
        CoroutineScope(Dispatchers.IO).launch {
            profileService.unfollowUser(loggedUser, currentDataSet[position].uuid)
        }
        holder.followButton.text = holder.view.resources.getString(R.string.follow_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.profile_row_item, parent, false))
            {
                val item = currentDataSet[it]
                val originalItemPos = dataSet.indexOf(item)
                onItemClicked(originalItemPos)
            }
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = holder.name
        val fullName = holder.fullName
        fullName.text = currentDataSet[position].fullName()
        name.text = currentDataSet[position].username
        CoroutineScope(Dispatchers.IO).launch { currentDataSet[position].pfp.loadImageInto(holder.pic) }
        if (currentDataSet[position].uuid == loggedUser.uuid) {
            holder.followButton.visibility = INVISIBLE
        }
        else if (canFollow(position)) {
            holder.followButton.text = holder.view.resources.getString(R.string.follow_button)
        } else {
            holder.followButton.text = holder.view.resources.getString(R.string.unfollow_button)
        }
        holder.followButton.setOnClickListener {
            if (holder.followButton.text.equals(holder.view.resources.getString(R.string.follow_button))) {
                follow(position, holder)
            } else {
                unfollow(position, holder)
            }
            CoroutineScope(Dispatchers.IO).launch {
                loggedUser = profileService.fetchUserProfile(loggedUser.uuid)!!
            }


        }
    }

    override fun getItemCount(): Int = currentDataSet.size
    override fun getFilter(): Filter = SearchedFilter(dataSet, currentDataSet,null) { notifyDataSetChanged() }

}