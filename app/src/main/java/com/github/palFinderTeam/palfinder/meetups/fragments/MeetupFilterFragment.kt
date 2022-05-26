package com.github.palFinderTeam.palfinder.meetups.fragments

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.meetups.activities.ShowParam
import com.github.palFinderTeam.palfinder.utils.time.askTime
import com.github.palFinderTeam.palfinder.utils.time.toSimpleDate
import com.github.palFinderTeam.palfinder.utils.time.toSimpleTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MeetupFilterFragment(val viewModel: MapListViewModel) : DialogFragment() {

    private lateinit var filterGroup: RadioGroup
    private lateinit var selectStartTime: TextView
    private lateinit var selectEndTime: TextView

    private var dateFormat = SimpleDateFormat()

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
        when (viewModel.showParam.value!!) {
            ShowParam.ALL -> filterGroup.check(R.id.button_all)
            ShowParam.PAL_PARTICIPATING -> filterGroup.check(R.id.participate_button)
            ShowParam.PAL_CREATOR -> filterGroup.check(R.id.created_button)
            ShowParam.ONLY_JOINED -> filterGroup.check(R.id.joinedButton)
            ShowParam.TRENDS -> filterGroup.check(R.id.trendButton)
        }

        // Change view model
        filterGroup.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = v.findViewById(checkedId)
            when (filterGroup.indexOfChild(radio)) {
                0 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.ALL)
                1 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.PAL_PARTICIPATING)
                2 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.PAL_CREATOR)
                3 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.ONLY_JOINED)
                4 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.TRENDS)
            }
        }

        // Dates, set to currently selected options
        viewModel.startTime = MutableLiveData(Calendar.getInstance())
        viewModel.endTime = MutableLiveData(Calendar.getInstance())

        context?.resources?.let { viewModel.endTime.value?.add(Calendar.DAY_OF_MONTH, it.getInteger(R.integer.base_day_interval)) }

        selectStartTime = v.findViewById(R.id.tv_StartDate)
        selectEndTime = v.findViewById(R.id.tv_EndDate)

        selectStartTime.setOnClickListener {
            onStartTimeSelect()
        }

        selectEndTime.setOnClickListener {
            onEndTimeSelect()
        }

        viewModel.startTime.observe(viewLifecycleOwner) { newDate ->
            v.findViewById<TextView>(R.id.tv_StartDate).apply { this.text = dateFormat.format(newDate) }
        }
        viewModel.endTime.observe(viewLifecycleOwner) { newDate ->
            v.findViewById<TextView>(R.id.tv_EndDate).apply { this.text = dateFormat.format(newDate) }
        }

        // Close dialog
        v.findViewById<Button>(R.id.filtersButtonDone).setOnClickListener{
            dialog?.dismiss()
        }

        return v
    }

    override fun onStart() {
        super.onStart()
        // Force the dialog to take whole width
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    /**
     * Button to select the start Date of meetup
     */
    private fun onStartTimeSelect() {
        askTime(
            childFragmentManager,
            viewModel.startTime.value?.toSimpleDate(),
            viewModel.startTime.value?.toSimpleTime(),
            minDate = Calendar.getInstance()
        ).thenAccept {
            val interval = viewModel.startTime.value?.timeInMillis?.let { it1 ->
                viewModel.endTime.value?.timeInMillis?.minus(
                    it1
                )
            }
            viewModel.startTime.value = it
            viewModel.endTime.value?.timeInMillis = it.timeInMillis + interval!!
            viewModel.endTime.value = viewModel.endTime.value
        }
    }

    /**
     * Button to select the end Date of meetup
     */
    private fun onEndTimeSelect() {
        askTime(
            childFragmentManager,
            viewModel.endTime.value?.toSimpleDate(),
            viewModel.endTime.value?.toSimpleTime(),
            minDate = viewModel.startTime.value
        ).thenAccept {
            viewModel.endTime.value = it
        }
    }
}