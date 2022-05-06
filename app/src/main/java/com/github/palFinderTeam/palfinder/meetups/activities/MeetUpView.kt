package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.chat.CHAT
import com.github.palFinderTeam.palfinder.chat.ChatActivity
import com.github.palFinderTeam.palfinder.databinding.NoAccountWarningBinding
import com.github.palFinderTeam.palfinder.profile.ProfileListFragment
import com.github.palFinderTeam.palfinder.profile.ProfileService
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.ui.login.LoginActivity
import com.github.palFinderTeam.palfinder.utils.addTagsToFragmentManager
import com.github.palFinderTeam.palfinder.utils.createPopUp
import com.github.palFinderTeam.palfinder.utils.createTagFragmentModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

//ids for putExtra function, to pass Meetups between views
const val MEETUP_SHOWN = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_SHOWN"
const val MEETUP_EDIT = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_EDIT"


@SuppressLint("SimpleDateFormat")
@AndroidEntryPoint
class MeetUpView : AppCompatActivity() {
    private val viewModel: MeetUpViewViewModel by viewModels()
    private lateinit var tagsViewModel: TagsViewModel<Category>

    @Inject
    lateinit var profileService: ProfileService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_view_new)

        val meetupId = intent.getSerializableExtra(MEETUP_SHOWN) as String
        viewModel.loadMeetUp(meetupId)
        //button to show the list of users participating in this meetUp
        val button = findViewById<Button>(R.id.show_profile_list_button)
        button.setOnClickListener { showProfileList() }

        //fill fields when the meetup is loaded from database
        viewModel.meetUp.observe(this) { meetUp ->
            fillFields()
            handleButton()
        }

        tagsViewModel = createTagFragmentModel(this, TagsViewModelFactory(viewModel.tagRepository))
        if (savedInstanceState == null) {
            addTagsToFragmentManager(supportFragmentManager, R.id.fc_tags)
        }
    }

    /**
     * chatButton, available only to participants
     * editButton, available only to meetUp creator
     * joinButton, available to all but the meetUp creator (cannot leave your own meetUp)
     */
    private fun handleButton(){
        val hasJoined = viewModel.hasJoin()
        val isCreator = viewModel.isCreator()
        findViewById<View>(R.id.bt_ChatMeetup).apply {
            this.isEnabled = hasJoined
            this.isVisible = hasJoined
            this.isClickable = hasJoined
        }
        findViewById<View>(R.id.bt_EditMeetup).apply {
            this.isEnabled = isCreator
            this.isVisible = isCreator
            this.isClickable = isCreator
        }
        findViewById<Button>(R.id.bt_JoinMeetup).apply {
            this.isEnabled = !isCreator
            this.isClickable = !isCreator
            this.text = if (hasJoined) getString(R.string.meetup_view_leave) else getString(R.string.meetup_view_join)
        }
    }

    /**
     * launches the ProfileListFragment, a dialogFragment, from the participantsId of the meetup loaded
     */
    private fun showProfileList() {
        ProfileListFragment(viewModel.meetUp.value?.participantsId!!).show(supportFragmentManager, "profile list")
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
     * cannot join/leave a meetUp if you are not logged in
     */
    fun onJoinOrLeave(v: View){
        if(profileService.getLoggedInUserID() == null){
            createPopUp(this,
                { startActivity(Intent(this, LoginActivity::class.java)) },
                textId = R.string.no_account_join,
                continueButtonTextId = R.string.login
            )

        }else {
            viewModel.joinOrLeave(this)
        }
    }




}