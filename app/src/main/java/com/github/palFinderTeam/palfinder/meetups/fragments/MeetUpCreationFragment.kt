package com.github.palFinderTeam.palfinder.meetups.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.LOCATION_SELECTED
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpCreationViewModel
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.*
import com.github.palFinderTeam.palfinder.utils.LiveDataExtension.observeOnce
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


const val MEETUP_EDIT = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_EDIT"
const val defaultTimeDelta = 1000 * 60 * 60

@AndroidEntryPoint
class MeetUpCreationFragment : Fragment() {
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    private val viewModel: MeetUpCreationViewModel by viewModels()
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>

    private val args: MeetUpCreationFragmentArgs by navArgs()

    private var dateFormat = SimpleDateFormat()

    private lateinit var hasLimitCheckBox: CheckBox
    private lateinit var limitEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var startDateField: TextView
    private lateinit var endDateField: TextView
    private lateinit var locationButton: Button
    private lateinit var doneButton: Button

    private lateinit var binding: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflate(R.layout.fragment_meetup_creation, container, false)
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateFormat = SimpleDateFormat(getString(R.string.date_long_format))

        hasLimitCheckBox = view.findViewById(R.id.hasCapacityButton)
        limitEditText = view.findViewById(R.id.et_Capacity)
        nameEditText = view.findViewById(R.id.et_EventName)
        descriptionEditText = view.findViewById(R.id.et_Description)
        startDateField = view.findViewById(R.id.tv_StartDate)
        endDateField = view.findViewById(R.id.tv_EndDate)
        locationButton = view.findViewById(R.id.bt_locationSelect)
        doneButton = view.findViewById(R.id.bt_Done)

        bindUI()

        // Create tag fragment
        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)

        if (savedInstanceState == null) {
            addTagsToFragmentManager(childFragmentManager, R.id.fc_tags)
        }

        // Load meetup or start from scratch
        val meetUpId = args.meetupId
        if (meetUpId != null) {
            viewModel.loadMeetUp(meetUpId)
        } else {
            viewModel.fillWithDefaultValues()
        }

        // Make sure tags are refreshed once when fetching from DB
        viewModel.tags.observe(viewLifecycleOwner) {
            tagsViewModel.refreshTags()
        }

        registerActivityResult()

        // Register for map result
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<LatLng>("selectedLocation")
            ?.observe(viewLifecycleOwner) {
                Log.i("JPP", it.toString())
                onLocationSelected(it)
            }
    }

    private fun registerActivityResult() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        onLocationSelected(data.getParcelableExtra(LOCATION_SELECTED)!!)
                    }
                }
            }
    }

    private fun bindUI() {
        viewModel.startDate.observe(viewLifecycleOwner) { newDate ->
            setTextView(R.id.tv_StartDate, dateFormat.format(newDate))
        }
        viewModel.endDate.observe(viewLifecycleOwner) { newDate ->
            setTextView(R.id.tv_EndDate, dateFormat.format(newDate))
        }

        viewModel.name.observeOnce(viewLifecycleOwner) {
            setTextView(R.id.et_EventName, it)
        }
        nameEditText.doAfterTextChanged { text ->
            viewModel.setName(text.toString())
        }
        viewModel.description.observeOnce(viewLifecycleOwner) {
            setTextView(R.id.et_Description, it)
        }
        descriptionEditText.doAfterTextChanged { text ->
            viewModel.setDescription(text.toString())
        }
        viewModel.hasMaxCapacity.observeOnce(viewLifecycleOwner) { hasMaxCapacity ->
            hasLimitCheckBox.isChecked = hasMaxCapacity
            limitEditText.isEnabled = hasMaxCapacity
        }
        viewModel.capacity.observeOnce(viewLifecycleOwner) {
            setTextView(R.id.et_Capacity, it.toString())
        }
        hasLimitCheckBox.setOnCheckedChangeListener { _, isChecked ->
            setCapacityField(isChecked)
            viewModel.setHasMaxCapacity(isChecked)
        }
        limitEditText.doAfterTextChanged { text ->
            val parsed = text.toString().toIntOrNull()
            if (parsed != null) {
                viewModel.setCapacity(parsed)
            } else {
                // TODO find something meaningful to do
                viewModel.setCapacity(1)
            }
        }
        limitEditText.isEnabled = hasLimitCheckBox.isChecked

        viewModel.canEditStartDate.observe(viewLifecycleOwner) {
            startDateField.isClickable = it
        }
        viewModel.canEditEndDate.observe(viewLifecycleOwner) {
            endDateField.isClickable = it
        }

        locationButton.setOnClickListener {
            onSelectLocation(it)
        }

        startDateField.setOnClickListener { onStartTimeSelectButton(it) }
        endDateField.setOnClickListener { onEndTimeSelectButton(it) }
        doneButton.setOnClickListener { onDone(it) }
    }

    private fun setCapacityField(isEditable: Boolean) {
        if (isEditable) {
            limitEditText.isEnabled = true
        } else {
            limitEditText.isEnabled = false
            limitEditText.text.clear()
        }
    }

    private fun setTextView(id: Int, value: String) {
        binding.findViewById<TextView>(id).apply { this.text = value }
    }

    fun onStartTimeSelectButton(v: View) {
        askTime(
            childFragmentManager,
            viewModel.startDate.value?.toSimpleDate(),
            viewModel.startDate.value?.toSimpleTime(),
            Calendar.getInstance(),
            viewModel.maxStartDate
        ).thenAccept {
            viewModel.setStartDate(it)
        }
    }

    fun onEndTimeSelectButton(v: View) {
        askTime(
            childFragmentManager,
            viewModel.endDate.value?.toSimpleDate(),
            viewModel.endDate.value?.toSimpleTime(),
            viewModel.startDate.value,
            viewModel.maxEndDate
        ).thenAccept {
            viewModel.setEndDate(it)
        }
    }


    /**
     * Check Name and Description are present
     */
    private fun checkFieldValid(name: String, description: String): Boolean {
        if (name == "" || description == "") {
            showMessage(
                R.string.meetup_creation_missing_name_desc,
                R.string.meetup_creation_missing_name_desc_title
            )
            return false
        }
        return true
    }

    fun onDone(v: View) {

        // Check field validity
        val name = nameEditText.text.toString()
        val description = descriptionEditText.text.toString()
        if (!checkFieldValid(name, description)) return

        // Listen on DB response to move forward.
        viewModel.sendSuccess.observe(viewLifecycleOwner) { isSuccessFull ->
            if (isSuccessFull) {
                val action =
                    MeetUpCreationFragmentDirections.actionMeetupCreationFragmentToMeetupShowFragment(
                        viewModel.getMeetUpId()!!
                    )
                findNavController().navigate(action)
            } else {
                Snackbar.make(v, getString(R.string.DB_error_msg), 4).show()
            }
        }

        viewModel.sendMeetUp()
    }

    /**
     * Show [message] with [title] in an alert box
     */
    private fun showMessage(message: Int, title: Int) {
        val dlgAlert = AlertDialog.Builder(context)
        dlgAlert.setMessage(message)
        dlgAlert.setTitle(title)
        dlgAlert.setPositiveButton(R.string.ok, null)
        dlgAlert.setCancelable(true)
        dlgAlert.create().show()
    }

    private fun onSelectLocation(v: View) {
        val action =
            MeetUpCreationFragmentDirections.actionMeetupCreationFragmentSelectLocation(viewModel.getLatLng())
        findNavController().navigate(action)
    }

    private fun onLocationSelected(p0: LatLng) {
        viewModel.setLatLng(p0)
    }
}