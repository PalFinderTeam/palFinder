package com.github.palFinderTeam.palfinder.meetups.fragments

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpViewViewModel
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import kotlinx.coroutines.launch

private const val ARG_MEETUP = "meetup"

class MeetupViewFragment : Fragment() {
    private val model: MeetUpViewViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val meetupObs = Observer<MeetUp> { meetup ->
            fillFields(meetup)
        }

        model.meetUp.observe(viewLifecycleOwner, meetupObs)

        return inflater.inflate(R.layout.fragment_meetup_view_new, container, false)
    }

    private fun setTextView(id: Int, value: String){
        view?.findViewById<TextView>(id)?.apply { this.text = value }
    }

    @SuppressLint("SimpleDateFormat")
    private fun fillFields(meetUp: MeetUp){
        val format = SimpleDateFormat(getString(R.string.date_long_format))
        val startDate = format.format(meetUp.startDate.time)
        val endDate = format.format(meetUp.endDate.time)

        setTextView(R.id.tv_ViewEventName,meetUp.name)
        setTextView(R.id.tv_ViewEventDescritpion,meetUp.description)

        model.viewModelScope.launch {
            meetUp.iconImage?.let {
                it.loadImageInto(requireView().findViewById(R.id.iv_MeetupImage), requireContext())
            }
        }
        model.getUsernameOf(meetUp.creatorId){
            if (it != null){
                setTextView(R.id.tv_ViewEventCreator,
                    getString(R.string.meetup_view_creator, it))
            }
            else{
                setTextView(R.id.tv_ViewEventCreator,
                    getString(R.string.meetup_view_creator,
                        getString(R.string.invalid_username)))
            }
        }

        setTextView(R.id.tv_ViewStartDate, startDate)
        setTextView(R.id.tv_ViewEndDate,endDate)
    }
}