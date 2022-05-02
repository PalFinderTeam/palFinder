package com.github.palFinderTeam.palfinder.meetups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.PrettyDate
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MeetupListAdapter(private val dataSet: List<MeetUp>, override val currentDataSet: MutableList<MeetUp>,
                        private var filter: SearchedFilter<MeetUp>, private var currentLocation: Location,
                        private val context: Context,
                        private val onItemClicked: (position: Int) -> Unit) :
    MeetupListRootAdapter(dataSet, currentDataSet, context, onItemClicked), Filterable {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        // Also add the distance to the Meetup View list element
        val meetupDistance = holder.meetupDistance
        meetupDistance.text = currentDataSet[position].location
            .prettyDistanceTo(holder.parContext, currentLocation)

        meetupDistance.visibility = VISIBLE
    }

    override fun getFilter(): Filter = filter
}



