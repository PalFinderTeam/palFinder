package com.github.palFinderTeam.palfinder.meetups.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetupListAdapter
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import com.github.palFinderTeam.palfinder.utils.addToFragmentManager
import com.github.palFinderTeam.palfinder.utils.createTagFragmentModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MeetupListActivity : AppCompatActivity() {
    private lateinit var meetupList: RecyclerView
    lateinit var adapter: MeetupListAdapter
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>

    private val viewModel: MeetUpListViewModel by viewModels()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        meetupList = findViewById(R.id.meetup_list_recycler)
        meetupList.layoutManager = LinearLayoutManager(this)
        val searchField = findViewById<SearchView>(R.id.search_list)
        searchField.imeOptions = EditorInfo.IME_ACTION_DONE

        viewModel.listOfMeetUp.observe(this) { meetups ->
            adapter = MeetupListAdapter(meetups) { onListItemClick(it) }
            meetupList.adapter = adapter
            SearchedFilter.setupSearchField(searchField, adapter.filter)
        }

        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)
        if (savedInstanceState == null) {
            addToFragmentManager(supportFragmentManager, R.id.list_select_tag)
        }
        viewModel.tags.observe(this) {
            tagsViewModel.refreshTags()
            Log.i("Tag thingy",it.toString())
            filterByTag(it)
        }

    }

    fun filterByTag(tags: Set<Category>?) {
        if (::adapter.isInitialized) {
            adapter.currentDataSet.clear()
            viewModel.listOfMeetUp.value?.let { meetups -> performFilterByTag(meetups, adapter.currentDataSet, tags) }
            adapter.notifyDataSetChanged()
        }
    }

    private fun performFilterByTag(
        meetups: List<MeetUp>,
        currentDataSet: MutableList<MeetUp>,
        tags: Set<Category>?
    ) {
        if (tags!!.isEmpty()) {
            currentDataSet.addAll(meetups)
        } else {
            for (meetup: MeetUp in meetups) {
                if (meetup.tags.containsAll(tags)) {
                    currentDataSet.add(meetup)
                }
            }
        }
    }

    fun sortByCap(view: View?) {
        if (::adapter.isInitialized) {
            adapter.currentDataSet.clear()
            viewModel.listOfMeetUp.value?.let { meetups -> adapter.currentDataSet.addAll(meetups.sortedBy { it.capacity }) }
            adapter.notifyDataSetChanged()
        }
    }

    fun sortByName(view: View?) {
        if (::adapter.isInitialized) {
            adapter.currentDataSet.clear()
            viewModel.listOfMeetUp.value?.let { meetups -> adapter.currentDataSet.addAll(meetups.sortedBy { it.name.lowercase() }) }
            adapter.notifyDataSetChanged()
        }
    }


    private fun onListItemClick(position: Int) {
        val intent = Intent(this, MeetUpView::class.java)
            .apply { putExtra(MEETUP_SHOWN, viewModel.listOfMeetUp.value?.get(position)?.uuid) }
        startActivity(intent)
    }
}