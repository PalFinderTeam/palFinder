package com.github.palFinderTeam.palfinder.meetups.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.meetups.activities.ShowParam
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MeetupFilterFragment(val viewModel: MapListViewModel) : DialogFragment() {

    private lateinit var filterGroup: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout with recycler view
        val v: View = inflater.inflate(R.layout.fragment_meetup_filter, container, false)

        // RadioGroup to choose between the different filter options
        filterGroup = v.findViewById(R.id.follower_options_group)

        // Check currently selected option
        when (viewModel.showParam) {
            ShowParam.ALL -> filterGroup.check(R.id.button_all)
            ShowParam.PAL_PARTICIPATING -> filterGroup.check(R.id.participate_button)
            ShowParam.PAL_CREATOR -> filterGroup.check(R.id.created_button)
            ShowParam.ONLY_JOINED -> filterGroup.check(R.id.joinedButton)
        }

        // Change view model
        filterGroup.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = v.findViewById(checkedId)
            when (filterGroup.indexOfChild(radio)) {
                0 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.ALL)
                1 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.PAL_PARTICIPATING)
                2 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.PAL_CREATOR)
                3 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.ONLY_JOINED)
            }
        }


        return v
    }
}