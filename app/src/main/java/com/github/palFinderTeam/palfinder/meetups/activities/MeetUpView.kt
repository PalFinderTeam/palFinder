package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import dagger.hilt.android.AndroidEntryPoint
import com.github.palFinderTeam.palfinder.tag.*


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

        viewModel.meetUp.observe(this) { meetUp ->
            fillFields(meetUp)
        }

        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = ViewModelProvider(
            this,
            tagsViewModelFactory
        )[TagsViewModel::class.java] as TagsViewModel<Category>

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<TagsDisplayFragment<Category>>(R.id.fc_tags)
            }
        }
    }

    private fun setTextView(id: Int, value: String){
        findViewById<TextView>(id).apply { this.text = value }
    }

    private fun fillFields(meetup: MeetUp){
        val format = SimpleDateFormat(getString(R.string.date_long_format))
        val startDate = format.format(meetup.startDate.time)
        val endDate = format.format(meetup.endDate.time)

        setTextView(R.id.tv_ViewEventName,meetup.name)
        setTextView(R.id.tv_ViewEventDescritpion,meetup.description)
        setTextView(R.id.tv_ViewEventCreator, getString(R.string.meetup_view_creator, meetup.creator.name))

        setTextView(R.id.tv_ViewStartDate, startDate)
        setTextView(R.id.tv_ViewEndDate,endDate)
    }

    fun onEdit(v: View){
        val intent = Intent(this, MeetUpCreation::class.java).apply {
            putExtra(MEETUP_EDIT, viewModel.meetUp.value?.uuid)
        }
        startActivity(intent)
    }
}