package com.github.palFinderTeam.palfinder.user.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.palFinderTeam.palfinder.navigation.MainNavActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.USER_ID
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.ui.login.CREATE_ACCOUNT_PROFILE
import com.github.palFinderTeam.palfinder.utils.LiveDataExtension.observeOnce
import com.github.palFinderTeam.palfinder.utils.askDate
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.image.pickProfileImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@SuppressLint("SimpleDateFormat")
class UserSettingsActivity : AppCompatActivity() {

    val viewModel: UserSettingsViewModel by viewModels()

    private lateinit var usernameField: EditText
    private lateinit var nameField: EditText
    private lateinit var surnameField: EditText
    private lateinit var bioField: EditText
    private lateinit var birthdayField: EditText
    private lateinit var imageField: ImageView
    private lateinit var removeBirthdayButton: ImageView

    private var dateFormat = SimpleDateFormat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)
        dateFormat = SimpleDateFormat(getString(R.string.userSettingsBdayPattern))

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
            findViewById<Button>(R.id.SettingsSubmitButton).text =
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
                ImageInstance(it).loadImageInto(imageField)
            }
        }
        viewModel.birthday.observeOnce(this) {
            updateBDayField(it)
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
    }

    /**
     * Setup for success message display, as well as
     * fields activations/deactivations
     */
    private fun initiateSuccessIndicator() {
        viewModel.updateStatus.observe(this) { status ->
            // Set availability status
            if (status == UserSettingsViewModel.UPDATE_RUNNING) {
                toggleFieldAvailability(false)
            } else {
                toggleFieldAvailability(true)
            }

            // Show success message
            if (status == UserSettingsViewModel.UPDATE_SUCCESS) {
                displayToastMsg(getString(R.string.userSettingsSaveSuccess))
            }

            // Show success message
            if (status == UserSettingsViewModel.UPDATE_ERROR) {
                displayToastMsg(getString(R.string.userSettingsSaveFail))
            }

            // Show create account success + redirect
            // Go to main activity
            if (status == UserSettingsViewModel.CREATE_SUCCESS) {
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
        if (msg != UserSettingsViewModel.MSG_NO_MSG) {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Called when the save button is clicked
     */
    fun submitData(view: View?) {
        val respMsg = viewModel.checkAllFields()
        if (respMsg == UserSettingsViewModel.MSG_NO_MSG) {
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
        findViewById<Button>(R.id.SettingsSubmitButton).isEnabled = status
    }

    /**
     * Open date fragment by clicking on the birthday field
     */
    fun openDatePickerFragment(view: View?) {
        askDate(supportFragmentManager).thenAccept {
            viewModel.setBirthday(it)
            updateBDayField(it)
        }
    }

    /**
     * Click on the delete button on the right to clear birthday
     * field, if it hasn't been cleared yet
     */
    fun deletePickedBirthday(view: View?) {
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