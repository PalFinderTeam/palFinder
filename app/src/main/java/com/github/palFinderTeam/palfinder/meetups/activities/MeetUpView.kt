package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.addTagsToFragmentManager
import com.github.palFinderTeam.palfinder.utils.createTagFragmentModel


const val MEETUP_SHOWN = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_SHOWN"

@SuppressLint("SimpleDateFormat")
class MeetUpView : AppCompatActivity() {
    private lateinit var  model: MeetUpViewViewModel
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_view)

        model = MeetUpViewViewModel.new(intent.getSerializableExtra(MEETUP_SHOWN) as MeetUp)

        loadTag()

        if (savedInstanceState == null) {
            addTagsToFragmentManager(supportFragmentManager, R.id.fc_tags)
        }
    }

    private fun loadTag(){
        tagsViewModel = createTagFragmentModel(this, model.meetUp.value!!.tags, false)
    }

    fun onEdit(v: View){
        val intent = Intent(this, MeetUpCreation::class.java).apply {
            putExtra(MEETUP_EDIT, model.meetUp.value)
        }
        startActivity(intent)
    }
}