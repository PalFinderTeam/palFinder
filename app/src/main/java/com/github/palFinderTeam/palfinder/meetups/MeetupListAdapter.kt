package com.github.palFinderTeam.palfinder.meetups

import android.content.Context
import android.view.View.VISIBLE
import android.widget.Filter
import android.widget.Filterable
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.SearchedFilter

/**
 * extends the MeetupListRootAdapter by adding the distance to the current location and the filter
 */
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



