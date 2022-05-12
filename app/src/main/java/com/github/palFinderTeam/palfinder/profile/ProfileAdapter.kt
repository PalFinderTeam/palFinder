package com.github.palFinderTeam.palfinder.profile

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileAdapter(
    private val dataSet: List<ProfileUser>,
    private var loggedUser: ProfileUser,
    private val context: Context,
    private val onItemClicked: (position: Int) -> Unit,
    private val onFollow: (uuid: String) -> Unit,
    private val onUnFollow: (uuid: String) -> Unit,
) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>(),
    Filterable {
    val currentDataSet = dataSet.toMutableList()

    class ViewHolder(val view: View, private val onItemClicked: (position: Int) -> Unit) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        val name: TextView = view.findViewById(R.id.profile_name)
        val fullName: TextView = view.findViewById(R.id.fullName)
        val pic: ImageView = view.findViewById(R.id.userPic)
        val followButton: Button = view.findViewById(R.id.followButton)
        val listAchievement: List<ImageView> = listOf(view.findViewById(R.id.AchPic1), view.findViewById(R.id.AchPic2), view.findViewById(R.id.AchPic3))

        init {
            view.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            val position = adapterPosition
            onItemClicked(position)
        }


    }

    private fun follow(position: Int, holder: ViewHolder) {
        onFollow(currentDataSet[position].uuid)
        holder.followButton.text = holder.view.resources.getString(R.string.unfollow)
    }

    private fun canFollow(position: Int): Boolean {
        return loggedUser.canFollow(currentDataSet[position].uuid)
    }

    private fun unfollow(position: Int, holder: ViewHolder) {
        onUnFollow(currentDataSet[position].uuid)
        holder.followButton.text = holder.view.resources.getString(R.string.follow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.profile_row_item, parent, false)
        )
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
        bindImages(holder, position)
        when {
            currentDataSet[position].uuid == loggedUser.uuid -> {
                holder.followButton.visibility = INVISIBLE
            }
            canFollow(position) -> {
                holder.followButton.text = holder.view.resources.getString(R.string.follow)
            }
            else -> {
                holder.followButton.text = holder.view.resources.getString(R.string.unfollow)
            }
        }
        holder.followButton.setOnClickListener {
            if (holder.followButton.text.equals(holder.view.resources.getString(R.string.follow))) {
                follow(position, holder)
            } else {
                unfollow(position, holder)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            currentDataSet[position].pfp.loadImageInto(
                holder.pic,
                context
            )
        }
    }

    /**
     * fill the 3 image placeholder with achievements pictures
     */
    private fun bindImages(holder: ProfileAdapter.ViewHolder, position: Int) {
        val achievements = currentDataSet[position].achievements()
        when (achievements.size) {
            0 -> holder.listAchievement.forEach { it.visibility = INVISIBLE }
            1 -> {
                holder.listAchievement[0].setImageResource(achievements[0].imageID)
                holder.listAchievement.drop(1).forEach{it.visibility = INVISIBLE }
            }
            2 -> {
                holder.listAchievement[0].setImageResource(achievements[0].imageID)
                holder.listAchievement[1].setImageResource(achievements[1].imageID)
                holder.listAchievement[2].visibility = INVISIBLE
            }
            else -> {
                holder.listAchievement[0].setImageResource(achievements[0].imageID)
                holder.listAchievement[1].setImageResource(achievements[1].imageID)
                holder.listAchievement[2].setImageResource(achievements[2].imageID)
            }

        }
    }

    override fun getItemCount(): Int = currentDataSet.size
    override fun getFilter(): Filter =
        SearchedFilter(dataSet, currentDataSet, null) { notifyDataSetChanged() }

}