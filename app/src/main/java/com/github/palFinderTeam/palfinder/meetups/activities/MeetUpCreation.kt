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
import com.github.palFinderTeam.palfinder.PalFinderApplication
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.easter_egg.ManeameaFragment
import com.github.palFinderTeam.palfinder.meetups.fragments.CriterionsFragment
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.*
import com.github.palFinderTeam.palfinder.utils.LiveDataExtension.observeOnce
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.image.pickProfileImage
import com.github.palFinderTeam.palfinder.utils.time.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
@AndroidEntryPoint
/**
 * MeetupCreation activity, that allows the user to create a brand new activity
 */
class MeetUpCreation : Fragment(R.layout.activity_meet_up_creation_new), IconDialog.Callback {

    //viewModel to store the current entered data
    val viewModel: MeetUpCreationViewModel by activityViewModels()

    //set up the tagViewModel
    private lateinit var tagsViewModel: TagsViewModel<Category>

    //meetupId argument, to be able to edit a already existing meetup
    private val args: MeetUpCreationArgs by navArgs()

    //stores the parent view
    private lateinit var rootView: View

    private var dateFormat = SimpleDateFormat()

    //all parameters of a meetUp
    private lateinit var hasLimitCheckBox: CheckBox
    private lateinit var limitEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var changeIconButton: LinearLayout // Button but changed for design purposes
    private lateinit var changeMarkerButton: LinearLayout // Button but changed for design purposes
    private lateinit var icon: ImageView
    private lateinit var iconDialog: IconDialog
    private lateinit var startDateField: TextView
    private lateinit var endDateField: TextView
    private lateinit var selectLocationButton: LinearLayout
    private lateinit var doneButton: Button
    private lateinit var criteriaSelectButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView = view

        //pretty date display
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
        tagsViewModel = createTagFragmentModel(this, TagsViewModelFactory(viewModel.tagRepository))

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

        // Allows user to set a custom image for his meetup
        iconDialog = childFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
            ?: IconDialog.newInstance(IconDialogSettings())
    }

    /**
     * find in view all the editable meetups parameters
     */
    private fun initiateFieldRefs() {
        hasLimitCheckBox = rootView.findViewById(R.id.hasCapacityButton)
        limitEditText = rootView.findViewById(R.id.et_Capacity)
        nameEditText = rootView.findViewById(R.id.et_EventName)
        descriptionEditText = rootView.findViewById(R.id.et_Description)
        changeIconButton = rootView.findViewById(R.id.bt_SelectIcon)
        changeMarkerButton = rootView.findViewById(R.id.linearLayout_marker)
        icon = rootView.findViewById(R.id.iv_Icon)
        startDateField = rootView.findViewById(R.id.tv_StartDate)
        endDateField = rootView.findViewById(R.id.tv_EndDate)
        selectLocationButton = rootView.findViewById(R.id.bt_locationSelect)
        doneButton = rootView.findViewById(R.id.bt_Done)
        criteriaSelectButton = rootView.findViewById(R.id.criterionsSelectButton)
    }

    //bind the UI elements to the viewModel, in both ways
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
                //no max capacity if the string provided do not resolved to an Int
                viewModel.setCapacity(1)
                hasLimitCheckBox.isChecked = false
            }
        }
        limitEditText.isEnabled = hasLimitCheckBox.isChecked

        changeIconButton.setOnClickListener {
            pickProfileImage(
                requireActivity(),
                registerForImagePickerResult::launch
            )
        }

        changeMarkerButton.setOnClickListener {
            iconDialog.show(childFragmentManager, ICON_DIALOG_TAG)
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

        criteriaSelectButton.setOnClickListener {
            CriterionsFragment(viewModel).show(childFragmentManager, getString(R.string.criteria))
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
        viewModel.location.observeOnce(this) {
            // We convert for visual consistency.
            setTextView(R.id.tv_location, it.toLatLng().toString())
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
                    ImageInstance(it).loadImageInto(
                        rootView.findViewById(R.id.iv_Icon),
                        requireContext()
                    )
                } else {
                    rootView.findViewById<ImageView>(R.id.iv_Icon)
                        .setImageDrawable(getDrawable(requireContext(), R.drawable.icon_group))
                }
            }
        }
    }

    /**
     * user cannot edit the capcityField if the meetup has no max capacity
     * @param isEditable value of the hasLimit Checkbox
     */
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

    //button to select the start Date of meetup
    private fun onStartTimeSelectButton() {
        askTime(
            childFragmentManager,
            viewModel.startDate.value?.toSimpleDate(),
            viewModel.startDate.value?.toSimpleTime(),
            Calendar.getInstance(),
            maxStartDate
        ).thenAccept {
            viewModel.setStartDate(it)
        }
    }

    //button to select the end Date of meetup
    private fun onEndTimeSelectButton() {
        askTime(
            childFragmentManager,
            viewModel.endDate.value?.toSimpleDate(),
            viewModel.endDate.value?.toSimpleTime(),
            viewModel.startDate.value,
            maxEndDate
        ).thenAccept {
            viewModel.setEndDate(it)
        }
    }


    /**
     * Check Name and Description are present
     */
    private fun checkFieldValid(name: String, description: String, location: Location?): Boolean {
        if (name == "" || description == "" || location == null) {
            showMessage(
                R.string.meetup_creation_missing_name_desc,
                R.string.meetup_creation_missing_name_desc_title
            )
            return false
        }
        return true
    }

    /**
     * button to validate the created meetup, send it to the viewModel and then to the database,
     * and change the view to the new created meetup
     */
    private fun onDone(v: View) {
        // Check field validity
        val name = nameEditText.text.toString()
        val description = descriptionEditText.text.toString()
        //if (easterEggChecked(name, description)) {
        if (easterEggChecked(name, description)) {
            ManeameaFragment().show(childFragmentManager, "")
        } else {
            val location = viewModel.location.value
            if (!checkFieldValid(name, description, location)) return

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
    }

    /**
     * checks if the current arguments match the easter_egg constants
     */
    private fun easterEggChecked(name: String, description: String): Boolean {
        return name == EASTER_NAME && description == EASTER_DESC
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

    /**
     * select the location of the new meetup
     * opens the map selection
     */
    private fun selectLocation() {
        val action = MeetUpCreationDirections.actionCreationPickLocation()
        action.startSelection = viewModel.location.value
        findNavController().navigate(action, navOptions {
            this.restoreState = true
        })
    }

    //prints the selected location in LatLng format
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


    override val iconDialogIconPack: IconPack?
        get() = (requireActivity().application as PalFinderApplication).iconPack

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        if (icons.isNotEmpty()) {
            viewModel.setMarker(icons.first().id)
        }
    }

    companion object {
        private const val ICON_DIALOG_TAG = "icon-dialog"
        private const val EASTER_NAME = "TARTINE"
        private const val EASTER_DESC = "CATOGATOS THE MASTER OF DOGGOS"
    }
}
