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
import com.github.palFinderTeam.palfinder.profile.ProfileListFragment
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.addTagsToFragmentManager
import com.github.palFinderTeam.palfinder.utils.createTagFragmentModel
import dagger.hilt.android.AndroidEntryPoint


const val MEETUP_SHOWN = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_SHOWN"

@SuppressLint("SimpleDateFormat")
@AndroidEntryPoint
class MeetUpView : AppCompatActivity() {
    private val viewModel: MeetUpViewViewModel by viewModels()
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_view_new)

        val meetupId = intent.getSerializableExtra(MEETUP_SHOWN) as String
        viewModel.loadMeetUp(meetupId)
        val button = findViewById<Button>(R.id.show_profile_list_button)
        button.setOnClickListener { showProfileList() }

        viewModel.meetUp.observe(this) { meetUp ->
            fillFields()
            handleButton()
        }

        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)
        if (savedInstanceState == null) {
            addTagsToFragmentManager(supportFragmentManager, R.id.fc_tags)
        }
    }

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

    private fun showProfileList() {
        ProfileListFragment(viewModel.meetUp.value?.participantsId!!).show(supportFragmentManager, "profile list")
    }

    private fun fillFields() {
        tagsViewModel.refreshTags()
    }

    fun onEdit(v: View) {
        if (viewModel.isCreator()) {
            val intent = Intent(this, MeetUpEditCompat::class.java).apply {
                putExtra(MEETUP_EDIT, viewModel.getMeetupID())
            }
            startActivity(intent)
        }
    }

    fun onChat(v: View) {
        if (viewModel.hasJoin()) {
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra(CHAT, viewModel.getMeetupID())
            }
            startActivity(intent)
        }
    }

    fun onJoinOrLeave(v: View){
        viewModel.joinOrLeave(this)
    }


}