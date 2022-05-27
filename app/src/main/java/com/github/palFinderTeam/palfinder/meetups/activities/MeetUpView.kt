package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.chat.CHAT
import com.github.palFinderTeam.palfinder.chat.ChatActivity
import com.github.palFinderTeam.palfinder.profile.ProfileListFragment
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.utils.addTagsToFragmentManager
import com.github.palFinderTeam.palfinder.utils.createPopUp
import com.github.palFinderTeam.palfinder.utils.createTagFragmentModel
import com.github.palFinderTeam.palfinder.utils.image.QRCode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


//ids for putExtra function, to pass Meetups between views
const val MEETUP_SHOWN = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_SHOWN"
const val MEETUP_EDIT = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_EDIT"


@AndroidEntryPoint
class MeetUpView : AppCompatActivity() {
    private val viewModel: MeetUpViewViewModel by viewModels()
    private lateinit var tagsViewModel: TagsViewModel<Category>

    @Inject
    lateinit var profileService: ProfileService

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("theme", Context.MODE_PRIVATE) ?: return
        val theme = sharedPref.getInt("theme", R.style.palFinder_default_theme)
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_view_new)
        var sharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "theme") {
                    recreate()
                }
            }
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        val meetupId = intent.getSerializableExtra(MEETUP_SHOWN) as String

        //button (image for design purposes) to show the list of users participating in this meetUp
        val button = findViewById<ImageView>(R.id.show_qr_button)
        button.setOnClickListener { showProfileList() }

        //fill fields when the meetup is loaded from database
        viewModel.meetUp.observe(this) {
            fillFields()
            handleButton()
        }

        viewModel.loadMeetUp(meetupId)

        tagsViewModel = createTagFragmentModel(this, TagsViewModelFactory(viewModel.tagRepository))
        if (savedInstanceState == null) {
            addTagsToFragmentManager(supportFragmentManager, R.id.fc_tags)
        }

        viewModel.askPermissionToJoin.observe(this) {
            if (it) {
                onJoinWithBlockedUsers()
            }
        }
    }

    /**
     * chatButton, available only to participants
     * editButton, available only to meetUp creator
     * joinButton, available to all but the meetUp creator (cannot leave your own meetUp)
     */
    private fun handleButton() {
        val hasJoined = viewModel.hasJoin()
        val isCreator = viewModel.isCreator()
        findViewById<View>(R.id.bt_ChatMeetup).apply {
            this.isEnabled = hasJoined
            this.isVisible = hasJoined
            this.isClickable = hasJoined
        }
        findViewById<View>(R.id.bt_OpenNavigation).apply {
            this.isEnabled = hasJoined
            this.isVisible = hasJoined
            this.isClickable = hasJoined
        }
        findViewById<View>(R.id.bt_EditMeetup).apply {
            this.isEnabled = isCreator
            this.isVisible = isCreator
            this.isClickable = isCreator
        }
        findViewById<Button>(R.id.button_join_meetup).apply {
            this.isEnabled = !isCreator
            this.isClickable = !isCreator
            this.text =
                if (hasJoined) getString(R.string.meetup_view_leave) else getString(R.string.meetup_view_join)
        }
        findViewById<FloatingActionButton>(R.id.bt_MuteMeetup).apply {
            this.isEnabled = hasJoined
            this.isClickable = hasJoined
            this.isVisible = hasJoined
            if (viewModel.canUnMute()){
                this.setImageResource(R.drawable.ic_baseline_notifications_off_24)
            }
            else{
                this.setImageResource(R.drawable.ic_baseline_notifications_active_24)
            }
        }
    }

    /**
     * launches the ProfileListFragment, a dialogFragment, from the participantsId of the meetup loaded
     */
    private fun showProfileList() {
        ProfileListFragment(viewModel.meetUp.value?.participantsId!!).show(
            supportFragmentManager,
            "profile list"
        )
    }

    private fun fillFields() {
        tagsViewModel.refreshTags()
    }


    /**
     * restart the meetUp creation activity to edit it
     */
    fun onEdit(v: View) {
        if (viewModel.isCreator()) {
            val intent = Intent(this, MeetUpEditCompat::class.java).apply {
                putExtra(MEETUP_EDIT, viewModel.getMeetupID())
            }
            startActivity(intent)
        }
    }

    /**
     * open Google Maps with navigation from currentLocation to meetUpLocation
     */
    fun onOpenNavigation(v: View){
        if(viewModel.hasJoin()){
            val location = viewModel.meetUp.value?.location
            if (location != null) {
                val navigationIntentUri = Uri.parse(
                    v.context.getString(R.string.navigationUri) + location.latitude.toString()+ "," + location.longitude.toString())
                val mapIntent = Intent(Intent.ACTION_VIEW, navigationIntentUri)
                mapIntent.setPackage(v.context.getString(R.string.mapPackage))
                startActivity(mapIntent)

            }
        }
    }

    /**
     * start the chatActivity of this meetUp (identified by the meetupId)
     */
    fun onChat(v: View) {
        if (viewModel.hasJoin()) {
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra(CHAT, viewModel.getMeetupID())
            }
            startActivity(intent)
        }
    }

    /**
     * Clicking on QR Code icon will show QR code
     */
    fun showQR(view: View?) {
        //Initiate the barcode encoder
        val barcodeEncoder = BarcodeEncoder()
        //Encode text in editText into QRCode image into the specified size using barcodeEncoder
        val bitmap = barcodeEncoder.encodeBitmap(
            MEETUP_SHOWN + intent.getSerializableExtra(MEETUP_SHOWN) as String,
            BarcodeFormat.QR_CODE,
            resources.getInteger(R.integer.QR_size),
            resources.getInteger(R.integer.QR_size)
        )

        QRCode.shareQRcode(bitmap, this)


    }

    /**
     * cannot join/leave a meetUp if you are not logged in
     */
    fun onJoinOrLeave(v: View) {
        if (profileService.getLoggedInUserID() == null) {
            loginPopUp()
        } else {
            viewModel.joinOrLeave(this)
        }
    }

    /**
     * Mute or Unmute the meetup
     */
    fun onMuteOrUnmute(v: View){
        viewModel.muteOrUnMute(this)
    }

    private fun onJoinWithBlockedUsers() {
        createPopUp(
            this, {
                viewModel.joinOrLeave(this, ignoreWarning = true)
            }, R.string.meetup_with_blocked_warning
        )
    }

    private fun loginPopUp(){
        createPopUp(this,
            { startActivity(Intent(this, LoginActivity::class.java)) },
            textId = R.string.no_account_join,
            continueButtonTextId = R.string.login
        )
    }
}