package com.github.palFinderTeam.palfinder.meetups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


/**
 * A general class for to bind Meetup data to a RecyclerView
 * containing a list of meetups. Doesn't contain location and filtering
 */
open class MeetupListRootAdapter(
    private val dataSet: List<MeetUp>,
    private val currentDataSet: MutableList<MeetUp>,
    private val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<MeetupListRootAdapter.ViewHolder>() {

    companion object {
        const val PARTICIPANTS_RATIO: String = "%d / %d"
        const val PARTICIPANTS_NO_LIMIT: String = "%d"
    }

    /**
     * ViewHolder for a single meetup element in the list
     */
    class ViewHolder(view: View, private val onItemClicked: (position: Int) -> Unit) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        val meetupTitle: TextView = view.findViewById(R.id.meetup_title)
        val meetupDate: TextView = view.findViewById(R.id.meetup_date)
        val meetupDescription: TextView = view.findViewById(R.id.meetup_description)
        val meetupNumberParticipants: TextView = view.findViewById(R.id.meetup_participant)
        val meetupDistance: TextView = view.findViewById(R.id.meetup_dist)
        val meetupImage: ImageView = view.findViewById(R.id.meetup_pic)
        val parContext: Context = view.context

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            onItemClicked(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetupListRootAdapter.ViewHolder {
        // Create a new view for each meetup
        return MeetupListRootAdapter.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.meetup_listview_new, parent, false)
        ) {
            val item = currentDataSet[it]
            val originalItemPos = dataSet.indexOf(item)
            onItemClicked(originalItemPos)
        }
    }

    override fun onBindViewHolder(holder: MeetupListRootAdapter.ViewHolder, position: Int) {
        // Update displayed elements with the content of the current dataset
        val meetupTitle = holder.meetupTitle
        meetupTitle.text = currentDataSet[position].name

        val meetupDate = holder.meetupDate
        meetupDate.text = PrettyDate().timeDiff(currentDataSet[position].startDate)

        val meetupDescription = holder.meetupDescription
        meetupDescription.text = currentDataSet[position].description

        // Distance KM element is GONE by default

        val meetupNumberParticipants = holder.meetupNumberParticipants
        meetupNumberParticipants.text = getParticipantsFormatted(position)

        // Insert correct image
        val meetupPicture = holder.meetupImage
        if (currentDataSet[position].iconImage != null) {
            CoroutineScope(Dispatchers.Main).launch {
                currentDataSet[position].iconImage?.let {
                    it.loadImageInto(meetupPicture)
                }
            }
        } else {
            meetupPicture.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = currentDataSet.size

    /**
     * Generates a formatted version of the participants number
     * given the position of the element in the list
     *
     * @param position of the element in `currentDataSet`
     */
    private fun getParticipantsFormatted(position: Int) : String {
        return if (currentDataSet[position].hasMaxCapacity) {
            String.format(
                PARTICIPANTS_RATIO,
                currentDataSet[position].numberOfParticipants(),
                currentDataSet[position].capacity
            )
        } else {
            String.format(
                PARTICIPANTS_NO_LIMIT,
                currentDataSet[position].numberOfParticipants(),
            )
        }
    }

}