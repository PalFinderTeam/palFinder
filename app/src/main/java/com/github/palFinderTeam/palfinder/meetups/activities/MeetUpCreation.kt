package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.fragments.CriterionsFragment
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.*
import com.github.palFinderTeam.palfinder.utils.LiveDataExtension.observeOnce
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.image.pickProfileImage
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
@AndroidEntryPoint
class MeetUpCreation : Fragment(R.layout.activity_meet_up_creation) {

    val viewModel: MeetUpCreationViewModel by activityViewModels()
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>
    private val args: MeetUpCreationArgs by navArgs()

    private lateinit var rootView: View

    private var dateFormat = SimpleDateFormat()

    private lateinit var hasLimitCheckBox: CheckBox
    private lateinit var limitEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var changeIconButton: Button
    private lateinit var icon: ImageView
    private lateinit var startDateField: TextView
    private lateinit var endDateField: TextView
    private lateinit var selectLocationButton: Button
    private lateinit var doneButton: Button
    private lateinit var criterionsSelectButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView = view

        dateFormat = SimpleDateFormat(getString(R.string.date_long_format))

        initiateFieldRefs()
        bindUI()

        if (savedInstanceState == null) {
            if (args.meetUpId != null) {
                viewModel.loadMeetUp(args.meetUpId!!)
            } else {
                viewModel.fillWithDefaultValues()
            }
        }

        // Create tag fragment
        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)

        if (savedInstanceState == null) {
            addTagsToFragmentManager(childFragmentManager, R.id.fc_tags)
        }
        // Make sure tags are refreshed once when fetching from DB
        viewModel.tags.observe(viewLifecycleOwner) {
            tagsViewModel.refreshTags()
        }

        // Observe map result
        getNavigationResultLiveData<Location>(LOCATION_RESULT)?.observe(viewLifecycleOwner) { result ->
            onLocationSelected(result.toLatLng())
            // Make sure to consume the value
            removeNavigationResult<Location>(LOCATION_RESULT)
        }
    }

    private fun initiateFieldRefs() {
        hasLimitCheckBox = rootView.findViewById(R.id.hasCapacityButton)
        limitEditText = rootView.findViewById(R.id.et_Capacity)
        nameEditText = rootView.findViewById(R.id.et_EventName)
        descriptionEditText = rootView.findViewById(R.id.et_Description)
        changeIconButton = rootView.findViewById(R.id.bt_SelectIcon)
        icon = rootView.findViewById(R.id.iv_Icon)
        startDateField = rootView.findViewById(R.id.tv_StartDate)
        endDateField = rootView.findViewById(R.id.tv_EndDate)
        selectLocationButton = rootView.findViewById(R.id.bt_locationSelect)
        doneButton = rootView.findViewById(R.id.bt_Done)
        criterionsSelectButton = rootView.findViewById(R.id.criterionsSelectButton)
    }

    private fun bindUI() {
        forwardBind()
        backwardBind()
    }

    // Bind fields to viewModel and button to actions
    private fun forwardBind() {
        nameEditText.doAfterTextChanged { text ->
            viewModel.setName(text.toString())
        }
        descriptionEditText.doAfterTextChanged { text ->
            viewModel.setDescription(text.toString())
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

        changeIconButton.setOnClickListener {
            pickProfileImage(
                requireActivity(),
                registerForImagePickerResult::launch
            )
        }
        icon.setOnClickListener {
            pickProfileImage(requireActivity(), registerForImagePickerResult::launch)
        }
        selectLocationButton.setOnClickListener {
            selectLocation()
        }
        startDateField.setOnClickListener {
            onStartTimeSelectButton()
        }
        endDateField.setOnClickListener {
            onEndTimeSelectButton()
        }
        doneButton.setOnClickListener {
            onDone(it)
        }

        criterionsSelectButton.setOnClickListener {
            CriterionsFragment(viewModel).show(childFragmentManager, "criterions")
        }
    }

    // Observe viewModel for changes
    private fun backwardBind() {
        viewModel.description.observeOnce(this) {
            setTextView(R.id.et_Description, it)
        }
        viewModel.startDate.observe(viewLifecycleOwner) { newDate ->
            setTextView(R.id.tv_StartDate, dateFormat.format(newDate))
        }
        viewModel.endDate.observe(viewLifecycleOwner) { newDate ->
            setTextView(R.id.tv_EndDate, dateFormat.format(newDate))
        }

        // Observe once when fetching existing meetup to avoid infinite loop
        viewModel.name.observeOnce(this) {
            setTextView(R.id.et_EventName, it)
        }
        viewModel.hasMaxCapacity.observeOnce(this) { hasMaxCapacity ->
            hasLimitCheckBox.isChecked = hasMaxCapacity
            limitEditText.isEnabled = hasMaxCapacity
        }
        viewModel.capacity.observeOnce(this) {
            setTextView(R.id.et_Capacity, it.toString())
        }
        viewModel.iconUrl.observeOnce(this) {
            lifecycleScope.launch {
                if (it == null) {
                    rootView.findViewById<ImageView>(R.id.iv_Icon)
                        .setImageDrawable(getDrawable(requireContext(), R.drawable.icon_group))
                } else {
                    ImageInstance(it).loadImageInto(icon, requireContext())
                }
            }
        }

        viewModel.canEditStartDate.observe(viewLifecycleOwner) {
            startDateField.isClickable = it
        }
        viewModel.canEditEndDate.observe(viewLifecycleOwner) {
            endDateField.isClickable = it
        }
        viewModel.icon.observe(viewLifecycleOwner) {
            viewModel.viewModelScope.launch {
                if (it != null) {
                    ImageInstance(it).loadImageInto(rootView.findViewById(R.id.iv_Icon), requireContext())
                } else {
                    rootView.findViewById<ImageView>(R.id.iv_Icon)
                        .setImageDrawable(getDrawable(requireContext(), R.drawable.icon_group))
                }
            }
        }
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
        rootView.findViewById<TextView>(id).apply { this.text = value }
    }

    private fun onStartTimeSelectButton() {
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

    private fun onEndTimeSelectButton() {
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

    private fun onDone(v: View) {
        // Check field validity
        val name = nameEditText.text.toString()
        val description = descriptionEditText.text.toString()
        if (!checkFieldValid(name, description)) return

        // Listen on DB response to move forward.
        viewModel.sendSuccess.observe(viewLifecycleOwner) { isSuccessFull ->
            if (isSuccessFull) {
                val intent = Intent(requireContext(), MeetUpView::class.java).apply {
                    putExtra(MEETUP_SHOWN, viewModel.getMeetUpId())
                }
                startActivity(intent)
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
        val dlgAlert = AlertDialog.Builder(requireContext())
        dlgAlert.setMessage(message)
        dlgAlert.setTitle(title)
        dlgAlert.setPositiveButton(R.string.ok, null)
        dlgAlert.setCancelable(true)
        dlgAlert.create().show()
    }

    private fun selectLocation() {
        val action = MeetUpCreationDirections.actionCreationPickLocation()
        action.startSelection = viewModel.location.value
        findNavController().navigate(action, navOptions {
            this.restoreState = true
        })
    }

    private fun onLocationSelected(p0: LatLng) {
        viewModel.setLatLng(p0)
        setTextView(R.id.tv_location, p0.toString())
    }

    // Runs when image picker returns result
    private val registerForImagePickerResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    viewModel.setIcon(fileUri)
                    icon.setImageURI(fileUri)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
}