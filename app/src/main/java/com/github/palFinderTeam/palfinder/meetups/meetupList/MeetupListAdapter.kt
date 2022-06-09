package com.github.palFinderTeam.palfinder.meetups.meetupList

import android.content.Context
import android.view.View.VISIBLE
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.utils.Location

/**
 * extends the MeetupListRootAdapter by adding the distance to the current location and the filter
 */
class MeetupListAdapter(
    dataSet: List<MeetUp>,
    override val currentDataSet: MutableList<MeetUp>,
    private var currentLocation: Location,
    context: Context,
    onItemClicked: (position: Int) -> Unit) :
    MeetupListRootAdapter(dataSet, currentDataSet, context, onItemClicked) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        // Also add the distance to the Meetup View list element
        val meetupDistance = holder.meetupDistance
        meetupDistance.text = currentDataSet[position].location
            .prettyDistanceTo(holder.parContext, currentLocation)

        meetupDistance.visibility = VISIBLE
    }
}



