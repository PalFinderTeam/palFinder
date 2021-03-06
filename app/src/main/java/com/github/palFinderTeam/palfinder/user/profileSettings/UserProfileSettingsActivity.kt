package com.github.palFinderTeam.palfinder.user.profileSettings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.palFinderTeam.palfinder.PalFinderBaseActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.profile.USER_ID
import com.github.palFinderTeam.palfinder.login.CREATE_ACCOUNT_PROFILE
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.LiveDataExtension.observeOnce
import com.github.palFinderTeam.palfinder.utils.PrivacySettings
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.image.pickProfileImage
import com.github.palFinderTeam.palfinder.utils.time.askDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@SuppressLint("SimpleDateFormat")
class UserProfileSettingsActivity : PalFinderBaseActivity() {

    val viewModel: UserProfileSettingsViewModel by viewModels()

    private lateinit var usernameField: EditText
    private lateinit var nameField: EditText
    private lateinit var surnameField: EditText
    private lateinit var bioField: EditText
    private lateinit var birthdayField: EditText
    private lateinit var imageField: ImageView
    private lateinit var removeBirthdayButton: ImageView
    private lateinit var genderRadio: RadioGroup
    private lateinit var privacySettingsRadio: RadioGroup
    private lateinit var submitButton: Button

    private var dateFormat = SimpleDateFormat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        dateFormat = SimpleDateFormat(getString(R.string.userSettingsBdayPattern))

        submitButton = findViewById(R.id.SettingsSubmitButton)

        // Force user id
        if (intent.hasExtra(USER_ID)) {
            viewModel.loggedUID = intent.getStringExtra(USER_ID).toString()
        }

        // If user comes here because they to create an account, the login page
        // might have already have some data, use it to prefill fields
        var preProfile: ProfileUser? = null
        if (intent.hasExtra(CREATE_ACCOUNT_PROFILE)) {
            preProfile = intent.getSerializableExtra(CREATE_ACCOUNT_PROFILE) as ProfileUser
            viewModel.loggedUID = preProfile.uuid
            submitButton.text =
                getString(R.string.userSettingsButtonCreate)
        }

        // Init
        viewModel.loadUserInfo(preProfile)
        initiateFieldRefs()
        bindFieldsToData()
        initiateSuccessIndicator()

    }

    /**
     * Setup for References for fields in view
     */
    private fun initiateFieldRefs() {
        usernameField = findViewById(R.id.SettingsUsernameText)
        nameField = findViewById(R.id.SettingsNameText)
        surnameField = findViewById(R.id.SettingsSurnameText)
        bioField = findViewById(R.id.SettingsBioText)
        imageField = findViewById(R.id.settingsPfp)
        birthdayField = findViewById(R.id.SettingsBirthdayText)
        removeBirthdayButton = findViewById(R.id.SettingsDeleteBDay)
        genderRadio = findViewById(R.id.radioSex)
        privacySettingsRadio = findViewById(R.id.radioPrivacy)
    }

    /**
     * Setup for Bindings between fields in view and view model
     */
    private fun bindFieldsToData() {
        forwardBind()
        backwardsBind()
    }

    /**
     * Bind fields to view model
     */
    private fun forwardBind() {
        // Fields change bind to view model (to trigger checks/saves)
        usernameField.doAfterTextChanged { text ->
            viewModel.setUsername(text.toString())
        }
        nameField.doAfterTextChanged { text ->
            viewModel.setName(text.toString())
        }
        surnameField.doAfterTextChanged { text ->
            viewModel.setSurname(text.toString())
        }
        bioField.doAfterTextChanged { text ->
            viewModel.setBio(text.toString())
        }
        imageField.setOnClickListener {
            pickProfileImage(this, onResultFromIntent::launch)
        }
        submitButton.setOnClickListener { submitData() }
        removeBirthdayButton.setOnClickListener { deletePickedBirthday() }
        genderRadio.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioMale -> viewModel.setGender(Gender.MALE)
                R.id.radioFemale -> viewModel.setGender(Gender.FEMALE)
                R.id.radioOther -> viewModel.setGender(Gender.OTHER)
            }
        }
        privacySettingsRadio.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioPublic -> viewModel.setPrivacySetting(PrivacySettings.PUBLIC)
                R.id.radioFriends -> viewModel.setPrivacySetting(PrivacySettings.FRIENDS)
                R.id.radioPrivate -> viewModel.setPrivacySetting(PrivacySettings.PRIVATE)
            }
        }

        // NOTE: We don't have an observer for the text changing because
        // it is triggered directly with other functions such as
        // openDatePickerFragment() or deletePickedBirthday()
    }

    private fun backwardsBind() {
        // View model changes bind to fields
        viewModel.username.observeOnce(this) {
            usernameField.setText(it)
        }
        viewModel.name.observeOnce(this) {
            nameField.setText(it)
        }
        viewModel.surname.observeOnce(this) {
            surnameField.setText(it)
        }
        viewModel.userBio.observeOnce(this) {
            bioField.setText(it)
        }
        viewModel.pfp.observeOnce(this) {
            lifecycleScope.launch {
                ImageInstance(it).loadImageInto(imageField, applicationContext)
            }
        }
        viewModel.birthday.observeOnce(this) {
            updateBDayField(it)
        }
        viewModel.gender.observeOnce(this) {
            when (it) {
                Gender.MALE -> genderRadio.check(R.id.radioMale)
                Gender.FEMALE -> genderRadio.check(R.id.radioFemale)
                Gender.OTHER -> genderRadio.check(R.id.radioOther)
                else -> genderRadio.check(R.id.radioOther)
            }
        }
        viewModel.privacySettings.observeOnce(this) {
            when (it) {
                PrivacySettings.PUBLIC -> privacySettingsRadio.check(R.id.radioPublic)
                PrivacySettings.FRIENDS -> privacySettingsRadio.check(R.id.radioFriends)
                PrivacySettings.PRIVATE -> privacySettingsRadio.check(R.id.radioPrivate)
                else -> privacySettingsRadio.check(R.id.radioPublic)
            }
        }
    }

    /**
     * Extracted function to update the b-day text field
     * Because changing the field graphically here won't trigger
     * the update of the viewModel values, this will have to
     * be called multiple times in different functions
     *
     * @param calendar b-day value to translate into text field
     */
    private fun updateBDayField(calendar: Calendar?) {
        if (calendar == null) {
            birthdayField.setText("")
        } else {
            birthdayField.setText(dateFormat.format(calendar))
        }
        birthdayField.setOnClickListener { openDatePickerFragment() }
    }

    /**
     * Setup for success message display, as well as
     * fields activations/deactivations
     */
    private fun initiateSuccessIndicator() {
        viewModel.updateStatus.observe(this) { status ->
            // Set availability status
            if (status == UserProfileSettingsViewModel.UPDATE_RUNNING) {
                toggleFieldAvailability(false)
            } else {
                toggleFieldAvailability(true)
            }

            // Show success message
            if (status == UserProfileSettingsViewModel.UPDATE_SUCCESS) {
                displayToastMsg(getString(R.string.userSettingsSaveSuccess))
            }

            // Show success message
            if (status == UserProfileSettingsViewModel.UPDATE_ERROR) {
                displayToastMsg(getString(R.string.userSettingsSaveFail))
            }

            // Show create account success + redirect
            // Go to main activity
            if (status == UserProfileSettingsViewModel.CREATE_SUCCESS) {
                displayToastMsg(getString(R.string.userSettingsSaveCreate))
                val intent = Intent(this, MainNavActivity::class.java)
                startActivity(intent)
            }
        }
    }

    /**
     * Displays a toast message if the message is not empty
     * Otherwise ignore it
     *
     * @param msg Message to display
     */
    private fun displayToastMsg(msg: String) {
        if (msg != UserProfileSettingsViewModel.MSG_NO_MSG) {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Called when the save button is clicked
     */
    private fun submitData() {
        val respMsg = viewModel.checkAllFields()
        if (respMsg == UserProfileSettingsViewModel.MSG_NO_MSG) {
            viewModel.saveValuesIntoDatabase()
        } else {
            displayToastMsg(respMsg)
        }
    }

    /**
     * Toggle fields availability while submitting data
     *
     * @param status true/false for the enabled attribute
     */
    private fun toggleFieldAvailability(status: Boolean) {
        usernameField.isEnabled = status
        nameField.isEnabled = status
        surnameField.isEnabled = status
        bioField.isEnabled = status
        imageField.isEnabled = status
        removeBirthdayButton.isEnabled = status
        submitButton.isEnabled = status
    }

    /**
     * Open date fragment by clicking on the birthday field
     */
    private fun openDatePickerFragment() {
        askDate(supportFragmentManager).thenAccept {
            viewModel.setBirthday(it)
            updateBDayField(it)
        }
    }

    /**
     * Click on the delete button on the right to clear birthday
     * field, if it hasn't been cleared yet
     */
    private fun deletePickedBirthday() {
        if (viewModel.birthday.value != null) {
            viewModel.setBirthday(null)
            updateBDayField(null)
        }
    }

    // Repeated code from MeetupCreation, but can't extract it, it will throw an illegal state exception.
    private val onResultFromIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    viewModel.setPfp(fileUri)
                    imageField.setImageURI(fileUri)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
            }
        }
}