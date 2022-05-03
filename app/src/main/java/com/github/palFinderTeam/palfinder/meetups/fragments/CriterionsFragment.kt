package com.github.palFinderTeam.palfinder.meetups.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreationViewModel
import com.github.palFinderTeam.palfinder.utils.CriterionGender
import com.github.palFinderTeam.palfinder.utils.PrettyDate
import org.florescu.android.rangeseekbar.RangeSeekBar
import org.w3c.dom.Text


class CriterionsFragment(val viewModel: MeetUpCreationViewModel) : DialogFragment() {

    companion object {
        const val MIN_AGE = 13
        const val MAX_AGE = 66
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //inflate layout with recycler view
        val v: View = inflater.inflate(R.layout.fragment_criterions, container, false)
        // Setup the new range seek bar
        val rangeSeekBar: RangeSeekBar<Int> = RangeSeekBar(v.context)
        val textMin= v.findViewById<TextView>(R.id.minValueAge)
        val textMax = v.findViewById<TextView>(R.id.maxValueAge)
        // Set the range
        rangeSeekBar.setRangeValues(MIN_AGE, MAX_AGE)
        rangeSeekBar.selectedMinValue = viewModel.criterionAge.value!!.first
        rangeSeekBar.selectedMaxValue = viewModel.criterionAge.value!!.second
        textMin.text = rangeSeekBar.selectedMinValue.toString()
        if (rangeSeekBar.selectedMaxValue == rangeSeekBar.absoluteMaxValue) {
            textMax.text = getString(R.string.criterions_age_max)
        } else {
            textMax.text = rangeSeekBar.selectedMaxValue.toString()
        }

        rangeSeekBar.setOnRangeSeekBarChangeListener { _, minValue, maxValue ->
            textMin.text = minValue.toString()
            if (maxValue == rangeSeekBar.absoluteMaxValue) {
                textMax.text = getString(R.string.criterions_age_max)
            } else {
                textMax.text = maxValue.toString()
            }}

        val sexGroup: RadioGroup = v.findViewById(R.id.radioSex)
        when (viewModel.criterionGender.value) {
            CriterionGender.MALE -> sexGroup.check(R.id.radioMale)
            CriterionGender.FEMALE -> sexGroup.check(R.id.radioFemale)
            CriterionGender.ALL -> sexGroup.check(R.id.radioMaleAndFemale)
        }



        // Add to layout
        val layout = v.findViewById(R.id.ageSeekBar) as FrameLayout
        layout.addView(rangeSeekBar)

        val button: Button = v.findViewById(R.id.criterionButtonDone)

        button.setOnClickListener {
            val selectedOptionId = sexGroup.checkedRadioButtonId
            val sex = v.findViewById<RadioButton>(selectedOptionId).text

            when (sex) {
                getString(R.string.radio_female) -> viewModel.setCriterionGender(CriterionGender.FEMALE)
                getString(R.string.radio_male) -> viewModel.setCriterionGender(CriterionGender.MALE)
                getString(R.string.radio_male_and_female) -> viewModel.setCriterionGender(CriterionGender.ALL)
            }

            if (rangeSeekBar.selectedMaxValue == rangeSeekBar.absoluteMaxValue) {
                viewModel.setCriterionAge(Pair(rangeSeekBar.selectedMinValue, Int.MAX_VALUE))
            } else {
                viewModel.setCriterionAge(Pair(rangeSeekBar.selectedMinValue, rangeSeekBar.selectedMaxValue))
            }
            dialog?.dismiss()
        }
        return v
    }

    override fun onStart() {
        super.onStart()

        // Force the dialog to take whole width
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
}