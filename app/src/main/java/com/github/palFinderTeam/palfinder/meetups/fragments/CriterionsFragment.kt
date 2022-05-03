package com.github.palFinderTeam.palfinder.meetups.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreationViewModel
import com.github.palFinderTeam.palfinder.utils.CriterionGender
import com.google.android.material.slider.RangeSlider
import org.florescu.android.rangeseekbar.RangeSeekBar

const val MIN_AGE_DIST = 1.0f

class CriterionsFragment(val viewModel: MeetUpCreationViewModel) : DialogFragment() {

    private lateinit var radiusSlider: RangeSlider
    private lateinit var textMin: TextView
    private lateinit var textMax: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout with recycler view
        val v: View = inflater.inflate(R.layout.fragment_criterions, container, false)

        // Setup the new range seek bar
        //val rangeSeekBar: RangeSeekBar<Int> = RangeSeekBar(v.context)
        textMin = v.findViewById<TextView>(R.id.minValueAge)
        textMax = v.findViewById<TextView>(R.id.maxValueAge)

        // Set the new range
        radiusSlider = v.findViewById(R.id.rangeAgeSelector)
        // radiusSlider.value = max(radiusSlider.valueFrom, min(radiusSlider.valueTo, viewModel.searchRadius.value!!.toFloat()))
//        radiusSlider
//
//        radiusSlider.addOnChangeListener { _, value, _ ->
//            viewModel.setSearchParamAndFetch(radiusInKm = value.toDouble())
//        }
//        viewModel.setSearchParamAndFetch(radiusInKm = radiusSlider.value.toDouble())

        radiusSlider.setMinSeparationValue(MIN_AGE_DIST)
        radiusSlider.setValues(
            viewModel.criterionAge.value!!.first.toFloat(),
            if (viewModel.criterionAge.value!!.second == Int.MAX_VALUE) {
                getString(R.string.criterions_age_max).toFloat()
            } else {
                viewModel.criterionAge.value!!.second.toFloat()
            }
        )


        // Set the range
//        rangeSeekBar.setRangeValues(13, 66)
//        rangeSeekBar.selectedMinValue = viewModel.criterionAge.value!!.first
//        rangeSeekBar.selectedMaxValue = viewModel.criterionAge.value!!.second

        // Set initial values
        updateAgeText(radiusSlider)
//        textMin.text = rangeSeekBar.selectedMinValue.toString()
//        if (rangeSeekBar.selectedMaxValue == rangeSeekBar.absoluteMaxValue) {
//            textMax.text = getString(R.string.criterions_age_max_plus)
//        } else {
//            textMax.text = rangeSeekBar.selectedMaxValue.toString()
//        }

        // Bind listener on Slider
        radiusSlider.addOnChangeListener { rs, _, _ ->
            updateAgeText(rs)
        }

//        rangeSeekBar.setOnRangeSeekBarChangeListener { _, minValue, maxValue ->
//            textMin.text = minValue.toString()
//            if (maxValue == rangeSeekBar.absoluteMaxValue) {
//                textMax.text = getString(R.string.criterions_age_max)
//            } else {
//                textMax.text = maxValue.toString()
//            }}

        val sexGroup: RadioGroup = v.findViewById(R.id.radioSex)
        when (viewModel.criterionGender.value) {
            CriterionGender.MALE -> sexGroup.check(R.id.radioMale)
            CriterionGender.FEMALE -> sexGroup.check(R.id.radioFemale)
            CriterionGender.ALL -> sexGroup.check(R.id.radioMaleAndFemale)
        }

//        // Add to layout
//        val layout = v.findViewById(R.id.ageSeekBar) as FrameLayout
//        layout.addView(rangeSeekBar)

        val button: Button = v.findViewById(R.id.criterionButtonDone)

        // Set gender, or as Antoine would call it, sex.
        button.setOnClickListener {
            val selectedOptionId = sexGroup.checkedRadioButtonId
            val sex = v.findViewById<RadioButton>(selectedOptionId).text

            when (sex) {
                getString(R.string.radio_female) -> viewModel.setCriterionGender(CriterionGender.FEMALE)
                getString(R.string.radio_male) -> viewModel.setCriterionGender(CriterionGender.MALE)
                getString(R.string.radio_male_and_female) -> viewModel.setCriterionGender(CriterionGender.ALL)
            }

            // 66+ means anything above
            if (radiusSlider.values[1] == radiusSlider.valueTo) {
                viewModel.setCriterionAge(Pair(radiusSlider.values[0].toInt(), Int.MAX_VALUE))
            } else {
                viewModel.setCriterionAge(Pair(radiusSlider.values[0].toInt(), radiusSlider.values[1].toInt()))
            }
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
     * Update the age values on either side of the slide bar
     *
     * @param rs RangeSlider
     */
    private fun updateAgeText(rs: RangeSlider) {
        textMin.text = (rs.values[0].toInt()).toString()
        if (rs.values[1] == rs.valueTo) {
            textMax.text = getString(R.string.criterions_age_max_plus)
        } else {
            textMax.text = (rs.values[1].toInt()).toString()
        }
    }
}