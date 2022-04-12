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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.CONTEXT
import com.github.palFinderTeam.palfinder.map.LOCATION_SELECT
import com.github.palFinderTeam.palfinder.map.LOCATION_SELECTED
import com.github.palFinderTeam.palfinder.map.MapsActivity
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

const val MEETUP_EDIT = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_EDIT"
const val defaultTimeDelta = 1000 * 60 * 60

@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
@AndroidEntryPoint
class MeetUpCreation : AppCompatActivity() {

    private val viewModel: MeetUpCreationViewModel by viewModels()
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>


    private var dateFormat = SimpleDateFormat()

    private lateinit var hasLimitCheckBox: CheckBox
    private lateinit var limitEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var changeIconButton: Button
    private lateinit var icon: ImageView
    private lateinit var startDateField: TextView
    private lateinit var endDateField: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_creation)

        dateFormat = SimpleDateFormat(getString(R.string.date_long_format))

        initiateFieldRefs()
        bindUI()

        // Load meetup or start from scratch
        if (intent.hasExtra(MEETUP_EDIT)) {
            val meetupId = intent.getStringExtra(MEETUP_EDIT)
            if (meetupId != null) {
                viewModel.loadMeetUp(meetupId)
            }
        } else {
            viewModel.fillWithDefaultValues()
        }

        // Create tag fragment
        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)

        if (savedInstanceState == null) {
            addTagsToFragmentManager(supportFragmentManager, R.id.fc_tags)
        }
        // Make sure tags are refreshed once when fetching from DB
        viewModel.tags.observe(this) {
            tagsViewModel.refreshTags()
        }
    }

    private fun initiateFieldRefs() {
        hasLimitCheckBox = findViewById(R.id.hasCapacityButton)
        limitEditText = findViewById(R.id.et_Capacity)
        nameEditText = findViewById(R.id.et_EventName)
        descriptionEditText = findViewById(R.id.et_Description)
        changeIconButton = findViewById(R.id.bt_SelectIcon)
        icon = findViewById(R.id.iv_Icon)
        startDateField = findViewById(R.id.tv_StartDate)
        endDateField = findViewById(R.id.tv_EndDate)
    }

    private fun bindUI() {
        forwardBind()
        backwardBind()
    }

    // Bind fields to viewModel
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
                this,
                registerForImagePickerResult::launch
            )
        }
        icon.setOnClickListener {
            pickProfileImage(this, registerForImagePickerResult::launch)
        }
    }

    // Observe viewModel for changes
    private fun backwardBind() {
        viewModel.description.observeOnce(this) {
            setTextView(R.id.et_Description, it)
        }
        viewModel.startDate.observe(this) { newDate ->
            setTextView(R.id.tv_StartDate, dateFormat.format(newDate))
        }
        viewModel.endDate.observe(this) { newDate ->
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
                    findViewById<ImageView>(R.id.iv_Icon).setImageDrawable(getDrawable(R.drawable.icon_group))
                } else {
                    ImageInstance(it).loadImageInto(icon)
                }
            }
        }

        viewModel.canEditStartDate.observe(this) {
            startDateField.isClickable = it
        }
        viewModel.canEditEndDate.observe(this) {
            endDateField.isClickable = it
        }
        viewModel.icon.observe(this) {
            viewModel.viewModelScope.launch {
                if (it != null) {
                    ImageInstance(it).loadImageInto(findViewById(R.id.iv_Icon))
                } else {
                    findViewById<ImageView>(R.id.iv_Icon).setImageDrawable(getDrawable(R.drawable.icon_group))
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
        findViewById<TextView>(id).apply { this.text = value }
    }

    fun onStartTimeSelectButton(v: View) {
        askTime(
            supportFragmentManager,
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
            supportFragmentManager,
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
        viewModel.sendSuccess.observe(this) { isSuccessFull ->
            if (isSuccessFull) {
                val intent = Intent(this, MeetUpView::class.java).apply {
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
        val dlgAlert = AlertDialog.Builder(this)
        dlgAlert.setMessage(message)
        dlgAlert.setTitle(title)
        dlgAlert.setPositiveButton(R.string.ok, null)
        dlgAlert.setCancelable(true)
        dlgAlert.create().show()
    }

    fun onSelectLocation(v: View) {
        val intent = Intent(this, MapsActivity::class.java)
        val extras = Bundle().apply {
            putSerializable(CONTEXT, MapsActivity.Companion.SELECT_LOCATION)
            putParcelable(LOCATION_SELECT, LatLng(0.0, 0.0))
        }
        intent.putExtras(extras)
        registerForLocationResult.launch(intent)
    }

    private val registerForLocationResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    onLocationSelected(data.getParcelableExtra(LOCATION_SELECTED)!!)
                }
            }
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
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
            }
        }
}