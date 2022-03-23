package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.profile.ProfileListFragment
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagSelectorFragment
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
        setContentView(R.layout.activity_meet_up_view)

        val meetupId = intent.getSerializableExtra(MEETUP_SHOWN) as String
        viewModel.loadMeetUp(meetupId)

        val button = findViewById<Button>(R.id.show_profile_list_button)
        button.setOnClickListener { showProfileList() }

        viewModel.meetUp.observe(this) { meetUp ->
            fillFields(meetUp)
        }

        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)
        if (savedInstanceState == null) {
            addTagsToFragmentManager(supportFragmentManager, R.id.fc_tags)
        }
    }

    private fun showProfileList() {
        ProfileListFragment(viewModel.meetUp.value?.participantsId!!).show(supportFragmentManager, "profile list")
    }

    private fun fillFields(meetup: MeetUp) {
        tagsViewModel.refreshTags()
    }

    fun onEdit(v: View) {
        val intent = Intent(this, MeetUpCreation::class.java).apply {
            putExtra(MEETUP_EDIT, viewModel.meetUp.value?.uuid)
        }
        startActivity(intent)
    }
}