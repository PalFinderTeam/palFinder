package com.github.palFinderTeam.palfinder.user.profileSettings

import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.palFinderTeam.palfinder.profile.services.ProfileService
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.PrivacySettings
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.github.palFinderTeam.palfinder.utils.image.ImageUploader
import com.github.palFinderTeam.palfinder.utils.image.UrlFormat
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class UserProfileSettingsViewModel @Inject constructor(
    private val profileService: ProfileService,
    private val imageUploader: ImageUploader,
    private val timeService: TimeService
) : ViewModel() {

    // Define length constraints
    companion object {
        const val FIELD_USERNAME = "username"
        const val FIELD_NAME = "name"
        const val FIELD_SURNAME = "surname"
        const val FIELD_BIO = "profile description"

        val USERNAME_REGEX = """[a-zA-Z0-9._]*""".toRegex()

        // Define each field's allowed MIN/MAX length
        val FIELDS_LENGTH: Map<String, Pair<Int, Int>> = hashMapOf(
            FIELD_USERNAME to Pair(1, 32),
            FIELD_NAME to Pair(1, 32),
            FIELD_SURNAME to Pair(0, 32),
            FIELD_BIO to Pair(0, 180)
        )

        const val MSG_NO_MSG = ""
        const val MSG_FIELD_TOO_LONG = "Your %s exceeds the max %d characters allowed (has %d)"
        const val MSG_FIELD_TOO_SHORT = "Your %s is too short (or cannot be empty)!"
        const val MSG_USERNAME_BAD =
            "Username \"%s\" is invalid (can contain letters/numbers/. or _)"

        // Values for updates that indicate the status of the update process
        const val UPDATE_IDLE = 0
        const val UPDATE_RUNNING = 1
        const val UPDATE_ERROR = 2
        const val UPDATE_SUCCESS = 3
        const val CREATE_SUCCESS = 4
    }

    var loggedUID: String = profileService.getLoggedInUserID().orEmpty()

    private lateinit var _joinDate: Calendar

    private var _isNewUser: Boolean = false

    private val _username: MutableLiveData<String> = MutableLiveData()
    private val _name: MutableLiveData<String> = MutableLiveData()
    private val _surname: MutableLiveData<String> = MutableLiveData()
    private val _birthday: MutableLiveData<Calendar?> = MutableLiveData()
    private val _userBio: MutableLiveData<String> = MutableLiveData()
    private val _gender: MutableLiveData<Gender?> = MutableLiveData()
    private val _privacySettings: MutableLiveData<PrivacySettings?> = MutableLiveData()
    private val _pfp: MutableLiveData<String> = MutableLiveData()

    // The difference with the _pfp is that here it points to a local file and will be set only if the user choose a new icon.
    private val _pfpUri: MutableLiveData<Uri> = MutableLiveData()

    val username: LiveData<String> = _username
    val name: LiveData<String> = _name
    val surname: LiveData<String> = _surname
    val birthday: LiveData<Calendar?> = _birthday
    val userBio: LiveData<String> = _userBio
    val gender: LiveData<Gender?> = _gender
    val privacySettings: LiveData<PrivacySettings?> = _privacySettings
    val pfp: LiveData<String> = _pfp
    val pfpUri: LiveData<Uri> = _pfpUri

    // To indicate success of database fetch
    private val _updateStatus: MutableLiveData<Int> = MutableLiveData(UPDATE_IDLE)
    val updateStatus: LiveData<Int> = _updateStatus


    /**
     * Reset all fields to default values
     */
    fun resetFieldsWithDefaults() {
        _username.value = ""
        _name.value = ""
        _surname.value = ""
        _birthday.value = null
        _userBio.value = ""
        _gender.value = Gender.OTHER
        _privacySettings.value = PrivacySettings.PUBLIC
        _pfp.value =
            "" //TODO: TEMP VALUE until image upload works (everyone turns into a cat for now)
    }

    /**
     * Set all fields with values of the ProfileUse
     *
     * @param user ProfileUser to use the data from
     */
    private fun setFieldsWithUserValues(user: ProfileUser) {
        _username.postValue(user.username)
        _name.postValue(user.name)
        _surname.postValue(user.surname)
        _username.postValue(user.username)
        _userBio.postValue(user.description)
        _gender.postValue(user.gender)
        _privacySettings.postValue(user.privacySettings)
        _birthday.postValue(user.birthday)
        _pfp.postValue(user.pfp.imgURL)
    }

    /**
     * Load user data into fields asynchronously
     *
     * If a UserProfile is passed, that means it's because
     * the user wants to create an account, some info will be
     * taken from their Google account
     *
     * @param preFillUser if wants to transfer pre-fill data
     */
    fun loadUserInfo(preFillUser: ProfileUser? = null) {
        // Add prefill text, that means user creates an account
        _joinDate = timeService.now()
        if (preFillUser != null) {
            _isNewUser = true
            _joinDate = preFillUser.joinDate
            setFieldsWithUserValues(preFillUser)
        } else {
            viewModelScope.launch {
                // User exists in DB
                if (profileService.exists(loggedUID)) {
                    val user = profileService.fetch(loggedUID)!!
                    setFieldsWithUserValues(user)
                } else {
                    // No intent and user not found, reset everything
                    _isNewUser = true
                    resetFieldsWithDefaults()
                }
            }
        }

    }

    /**
     * Functions to set private data values of fields
     */
    fun setUsername(username: String) {
        _username.value = username
    }

    fun setName(name: String) {
        _name.value = name
    }

    fun setSurname(surname: String) {
        _surname.value = surname
    }

    fun setBirthday(birthday: Calendar?) {
        _birthday.value = birthday
    }

    fun setBio(bio: String) {
        _userBio.value = bio
    }

    fun setPfp(uri: Uri) {
        _pfpUri.value = uri
    }

    fun setGender(gender: Gender) {
        _gender.value = gender
    }

    fun setPrivacySetting(privacySettings: PrivacySettings){
        _privacySettings.value = privacySettings
    }

    /**
     * Checks if a specific field value meets the
     * defined requirements. If the requirements of
     * the fields are undefined, no check is made
     *
     * @param fieldName Name of the field
     * @param fieldValue Value of the field
     *
     * @return a string message to display in case of an error,
     * if no error EMPTY_STRING is returned
     */
    private fun checkField(fieldName: String, fieldValue: String): String {
        // Anything that was not defined with constraints are accepted
        if (!FIELDS_LENGTH.containsKey(fieldName)) return ""

        FIELDS_LENGTH[fieldName].let {
            val (min, max) = it!!
            if (fieldValue.length < min) {
                return String.format(MSG_FIELD_TOO_SHORT, fieldName)
            }
            if (fieldValue.length > max) {
                return String.format(MSG_FIELD_TOO_LONG, fieldName, max, fieldValue.length)
            }
            // Regex check for username
            if (fieldName == FIELD_USERNAME && !USERNAME_REGEX.matches(fieldValue)) {
                return String.format(MSG_USERNAME_BAD, fieldValue)
            }
            return MSG_NO_MSG
        }
    }

    /**
     * Save all user values into database
     */
    fun saveValuesIntoDatabase() {
        viewModelScope.launch {
            _updateStatus.postValue(UPDATE_RUNNING)
            pfpUri.value?.let {
                val newPath = imageUploader.uploadImage(it)
                if (newPath != null) {
                    // Also remove the previous icon from DB to avoid garbage.
                    pfp.value?.let { url ->
                        if (url.isNotEmpty() && UrlFormat.getUrlType(url) == UrlFormat.URL_IS_FIREBASE) {
                            try {
                                imageUploader.removeImage(url)
                            } catch (e : Exception) {
                                Log.e("UserSettings", e.message.orEmpty())
                            }
                        }
                    }
                    // We choose the new image only if not null
                    _pfp.value = newPath!!
                }
            }
            val oldUser = profileService.fetch(loggedUID)
            if (_isNewUser || oldUser == null) {
                val newUser = ProfileUser(
                    uuid = loggedUID,
                    username = username.value!!,
                    name = name.value!!,
                    surname = surname.value!!,
                    joinDate = _joinDate,
                    pfp = ImageInstance(pfp.value!!),
                    description = userBio.value!!,
                    birthday = birthday.value,
                    gender = gender.value,
                    privacySettings = privacySettings.value,
                )

                // Notify result
                if (profileService.create(newUser) != null) {
                    _updateStatus.postValue(CREATE_SUCCESS)
                } else {
                    _updateStatus.postValue(UPDATE_ERROR)
                }
            } else {

                val editedUser  = oldUser.copy(
                    username = username.value!!,
                    name = name.value!!,
                    surname = surname.value!!,
                    pfp = ImageInstance(pfp.value!!),
                    description = userBio.value!!,
                    birthday = birthday.value,
                    gender = gender.value,
                    privacySettings = privacySettings.value,
                )
                // Notify result
                if (profileService.edit(loggedUID, editedUser) != null) {
                    _updateStatus.postValue(UPDATE_SUCCESS)
                } else {
                    _updateStatus.postValue(UPDATE_ERROR)
                }
            }

        }
    }

    /**
     * Checks all text fields for upload
     * before uploading them to DB (called by View)
     * Note: this only applies to fields that are strings
     *
     * @return empty MSG if all fields passed, error message
     * otherwise
     */
    fun checkAllFields(): String {
        val fieldsToDataCheck: Map<String, LiveData<String>> = hashMapOf(
            FIELD_USERNAME to username,
            FIELD_NAME to name,
            FIELD_SURNAME to surname,
            FIELD_BIO to userBio
        )

        // Perform checks on all fields
        var resp = ""
        // Use of .all allows to easily iterate on each field and
        // stops if one value is not valid
        fieldsToDataCheck.all {
            val (name, data) = it
            resp = checkField(name, data.value.orEmpty())
            (resp == MSG_NO_MSG)
        }

        return resp
    }

}