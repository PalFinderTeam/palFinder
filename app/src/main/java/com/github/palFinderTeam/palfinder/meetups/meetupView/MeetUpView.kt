package com.github.palFinderTeam.palfinder.meetups.meetupView

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.github.palFinderTeam.palfinder.PalFinderBaseActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.chat.CHAT
import com.github.palFinderTeam.palfinder.chat.ChatActivity
import com.github.palFinderTeam.palfinder.profile.profileList.ProfileListFragment
import com.github.palFinderTeam.palfinder.profile.services.ProfileService
import com.github.palFinderTeam.palfinder.tags.Category
import com.github.palFinderTeam.palfinder.tags.TagsViewModel
import com.github.palFinderTeam.palfinder.tags.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.addTagsToFragmentManager
import com.github.palFinderTeam.palfinder.utils.createNoAccountPopUp
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
/**
 * view of a meetup letting the user see the different information and join it, if it is joined, he can access the chat or the navigation.
 * The owner can access the meetup editor
 */
class MeetUpView : PalFinderBaseActivity() {
    private val viewModel: MeetUpViewViewModel by viewModels()
    private lateinit var tagsViewModel: TagsViewModel<Category>

    @Inject
    lateinit var profileService: ProfileService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_view_new)

        val meetupId = intent.getSerializableExtra(MEETUP_SHOWN) as String

        bindButton()

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

    private fun bindButton() {
        findViewById<View>(R.id.show_qr_button).setOnClickListener { showProfileList() }
        findViewById<View>(R.id.bt_MuteMeetup).setOnClickListener { onMuteOrUnmute() }
        findViewById<View>(R.id.button_join_meetup).setOnClickListener { onJoinOrLeave() }
        findViewById<View>(R.id.qr_code_button).setOnClickListener { showQR() }
        findViewById<View>(R.id.bt_ChatMeetup).setOnClickListener { onChat() }
        findViewById<View>(R.id.bt_EditMeetup).setOnClickListener { onEdit() }
        findViewById<View>(R.id.bt_OpenNavigation).setOnClickListener { onOpenNavigation(it) }
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
            if (viewModel.canUnMute()) {
                this.setImageResource(R.drawable.ic_baseline_notifications_off_24)
            } else {
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
    private fun onEdit() {
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
    private fun onOpenNavigation(v: View) {
        if (viewModel.hasJoin()) {
            val location = viewModel.meetUp.value?.location
            if (location != null) {
                val navigationIntentUri = Uri.parse(
                    v.context.getString(R.string.navigationUri) + location.latitude.toString() + "," + location.longitude.toString()
                )
                val mapIntent = Intent(Intent.ACTION_VIEW, navigationIntentUri)
                mapIntent.setPackage(v.context.getString(R.string.mapPackage))
                startActivity(mapIntent)

            }
        }
    }

    /**
     * start the chatActivity of this meetUp (identified by the meetupId)
     */
    private fun onChat() {
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
    private fun showQR() {
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
    private fun onJoinOrLeave() {
        if (profileService.getLoggedInUserID() == null) {
            loginPopUp()
        } else {
            viewModel.joinOrLeave(this)
        }
    }

    /**
     * Mute or Unmute the meetup
     */
    private fun onMuteOrUnmute() {
        viewModel.muteOrUnMute(this)
    }

    private fun onJoinWithBlockedUsers() {
        createPopUp(
            this, R.string.meetup_with_blocked_warning
        ) {
            viewModel.joinOrLeave(this, ignoreWarning = true)
        }
    }

    private fun loginPopUp() {
        createNoAccountPopUp(this, R.string.no_account_join)
    }
}