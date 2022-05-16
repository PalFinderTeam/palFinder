package com.github.palFinderTeam.palfinder.meetups.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.ProfileActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpViewViewModel
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.USER_ID
import com.github.palFinderTeam.palfinder.utils.Response
import kotlinx.coroutines.launch


class MeetupViewFragment : Fragment() {
    private val viewModel: MeetUpViewViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val meetupObs = Observer<MeetUp> { meetup ->
            fillFields(meetup)
        }

        viewModel.meetUp.observe(viewLifecycleOwner, meetupObs)

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

        injectImage(meetUp)

        viewModel.getUsernameOf(meetUp.creatorId){
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
        setTextView(R.id.tv_ViewEndDate, endDate)

        setTextView(R.id.tv_ViewParticipants, getParticipation(meetUp))
        setTextView(R.id.tv_ViewAge, getAgeRange(meetUp))
        setTextView(R.id.tv_ViewGender, meetUp.criterionGender?.genderName.orEmpty())

        view?.findViewById<TextView>(R.id.tv_ViewEventCreator)?.setOnClickListener { openProfile() }
    }

    /**
     * Inject image or remove view if empty image
     * @param meetUp
     */
    private fun injectImage(meetUp: MeetUp) {
        viewModel.viewModelScope.launch {
            if (meetUp.iconImage == null) {
                requireView().findViewById<ImageView>(R.id.iv_MeetupImage).visibility = View.GONE
            } else {
                meetUp.iconImage.loadImageInto(
                    requireView().findViewById(R.id.iv_MeetupImage),
                    requireContext()
                )
            }
        }
    }

    /**
     * Creates the format for the age range
     * @param meetUp
     */
    private fun getAgeRange(meetUp: MeetUp): String {
        var marginStart = meetUp.criterionAge?.first.toString()
        var marginEnd = meetUp.criterionAge?.second.toString()
        if (meetUp.criterionAge?.second == Int.MAX_VALUE) {
            // Display 66+ if second is MAX_INT
            marginEnd = getString(R.string.meetup_view_age_max_plus)
        }

        return String.format(
            getString(R.string.meetup_view_age),
            marginStart,
            marginEnd
        )
    }

    /**
     * Creates the format for the participates number
     * @param meetUp
     */
    private fun getParticipation(meetUp: MeetUp): String {
        return if (meetUp.hasMaxCapacity) {
            // Limited participants
            String.format(
                getString(R.string.meetup_view_participation_limited),
                meetUp.numberOfParticipants(),
                meetUp.capacity
            )
        } else {
            // Unlimited participants
            String.format(
                getString(R.string.meetup_view_participation_unlimited),
                meetUp.numberOfParticipants()
            )
        }
    }

    /**
     * Onclick open profile user safely
     */
    private fun openProfile() {
        val intent = Intent(requireContext(), ProfileActivity::class.java).apply {
            putExtra(USER_ID, viewModel.meetUp.value!!.creatorId)
        }
        startActivity(intent)
    }
}