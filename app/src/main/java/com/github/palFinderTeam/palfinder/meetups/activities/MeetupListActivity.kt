package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetupListAdapter
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MeetupListActivity : MapListSuperActivity() {
    private lateinit var meetupList: RecyclerView
    lateinit var adapter: MeetupListAdapter
    lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    lateinit var tagsViewModel: TagsViewModel<Category>



    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        viewModel = ViewModelProvider(this)[MeetUpListViewModel::class.java]
        viewModel.update(null)

        meetupList = findViewById(R.id.meetup_list_recycler)
        meetupList.layoutManager = LinearLayoutManager(this)
        val searchField = findViewById<SearchView>(R.id.search_list)
        searchField.imeOptions = EditorInfo.IME_ACTION_DONE


        viewModel.listOfMeetUpResponse.observe(this) { it ->
            val meetups = (it as Response.Success).data
            adapter = MeetupListAdapter(meetups, meetups.toMutableList(),
                SearchedFilter(
                    meetups, meetups.toMutableList(), ::filterTag
                ) {
                    adapter.notifyDataSetChanged()
                })
            { onListItemClick(it) }
            meetupList.adapter = adapter
            SearchedFilter.setupSearchField(searchField, adapter.filter)
        }

        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)
        if (savedInstanceState == null) {
            addTagsToFragmentManager(supportFragmentManager, R.id.list_select_tag)
        }
        viewModel.tags.observe(this) {
            tagsViewModel.refreshTags()
            filterByTag(it)
        }

    }

    private fun filterTag(meetup : MeetUp): Boolean {
        return meetup.tags.containsAll(viewModel.tags.value!!)
    }


    fun filterByTag(tags: Set<Category>?) {
        if (::adapter.isInitialized) {
            adapter.currentDataSet.clear()
            viewModel.listOfMeetUpResponse.value?.let { meetups -> performFilterByTag((meetups as Response.Success).data, adapter.currentDataSet, tags) }
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

    fun sortByCap() {
        if (::adapter.isInitialized) {
            val sorted = adapter.currentDataSet.sortedBy { it.capacity }
            sort(sorted)
        }
    }

    fun sortByName() {
        if (::adapter.isInitialized) {
            val sorted = adapter.currentDataSet.sortedBy { it.name.lowercase() }
            sort(sorted)
        }
    }

    fun sortByDist() {
        if (::adapter.isInitialized) {
            val sorted = adapter.currentDataSet.sortedBy { it.location.distanceInKm(Location(0.0, 0.0)) }
            sort(sorted)
        }
    }

    private fun sort(sorted: List<MeetUp>) {
        adapter.currentDataSet.clear()
        viewModel.listOfMeetUpResponse.value?.let { it -> adapter.currentDataSet.addAll(sorted) }
        adapter.notifyDataSetChanged()
    }

    fun showMenu(view: View?) {
        val popupMenu = PopupMenu(this, view) //View will be an anchor for PopupMenu
        popupMenu.inflate(R.menu.sort)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            item ->
            when (item.itemId) {
                R.id.menu_sort_name -> sortByName()
                R.id.menu_sort_cap -> sortByCap()
                R.id.menu_sort_loc -> sortByDist()
                else -> {}
            }
            false
        })
        popupMenu.show()
    }



    private fun onListItemClick(position: Int) {
        val intent = Intent(this, MeetUpView::class.java)
            .apply { putExtra(MEETUP_SHOWN, (viewModel.listOfMeetUpResponse.value as Response.Success).data[position].uuid) }
        startActivity(intent)
    }

}


