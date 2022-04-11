package com.github.palFinderTeam.palfinder.meetups.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.LOCATION_SELECTED
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetupListAdapter
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.*
import dagger.hilt.android.AndroidEntryPoint


const val SHOW_JOINED_ONLY = "com.github.palFinderTeam.palFinder.meetup_list_view.SHOW_JOINED_ONLY"
const val BASE_RADIUS = 500.0

@AndroidEntryPoint
class MeetupListFragment : Fragment() {
    private lateinit var meetupList: RecyclerView
    lateinit var adapter: MeetupListAdapter
    lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    lateinit var tagsViewModel: TagsViewModel<Category>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    private val viewModel: MapListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_list, container, false).rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        meetupList = view.findViewById(R.id.meetup_list_recycler)
        meetupList.layoutManager = LinearLayoutManager(requireContext())
        val searchField = view.findViewById<SearchView>(R.id.search_list)
        searchField.imeOptions = EditorInfo.IME_ACTION_DONE


        //viewModel.showOnlyJoined = intent.getBooleanExtra(SHOW_JOINED_ONLY,false)

        viewModel.listOfMeetUpResponse.observe(requireActivity()) { it ->
            if (it is Response.Success && viewModel.searchLocation.value != null) {
                val meetups = it.data
                adapter = MeetupListAdapter(meetups, meetups.toMutableList(),
                    SearchedFilter(
                        meetups, meetups.toMutableList(), ::filterTags
                    ) {
                        adapter.notifyDataSetChanged()
                    },
                        viewModel.searchLocation.value!!
                        )
                { onListItemClick(it) }
                meetupList.adapter = adapter
                SearchedFilter.setupSearchField(searchField, adapter.filter)
            }
        }

//        viewModel.userLocation.observe(requireActivity()) { it ->
//            viewModel.searchLocation.value = it
//            viewModel.fetchMeetUps()
//        }
        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)
        if (savedInstanceState == null) {
            addTagsToFragmentManager(childFragmentManager, R.id.list_select_tag)
        }
        viewModel.tags.observe(requireActivity()) {
            tagsViewModel.refreshTags()
            filter(it)
        }
        registerActivityResult()
    }

    private fun filterTags(meetup : MeetUp): Boolean {
        return meetup.tags.containsAll(viewModel.tags.value!!)
    }

    private fun isParticipating(meetup : MeetUp): Boolean {
        return if (viewModel.showOnlyJoined) {
            val user = viewModel.getUser()
            return if (user != null) {
                meetup.isParticipating(user)
            } else {
                false
            }
        } else {
            true
        }
    }


    fun filter(tags: Set<Category>?) {
        if (::adapter.isInitialized) {
            adapter.currentDataSet.clear()
            viewModel.listOfMeetUpResponse.value?.let { meetups -> performFilter((meetups as Response.Success).data, adapter.currentDataSet, tags) }
            adapter.notifyDataSetChanged()
        }
    }

    private fun performFilter(
        meetups: List<MeetUp>,
        currentDataSet: MutableList<MeetUp>,
        tags: Set<Category>?
    ) {
        currentDataSet.addAll(meetups.filter {
            (tags==null || it.tags.containsAll(tags)) &&
            (!viewModel.showOnlyJoined || isParticipating(it))
        })
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
        val popupMenu = PopupMenu(requireContext(), view) //View will be an anchor for PopupMenu
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

//    fun searchOnMap(view: View?){
//        val intent = Intent(requireContext(), MapsActivity::class.java)
//        val extras = Bundle().apply {
//            putParcelable(LOCATION_SELECT, viewModel.getCameraPosition())
//            putSerializable(CONTEXT, MapsActivity.Companion.SELECT_LOCATION)
//        }
//        intent.putExtras(extras)
//
//        resultLauncher.launch(intent)
//    }

    private fun registerActivityResult() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        viewModel.getMeetupAroundLocation(data.getParcelableExtra(LOCATION_SELECTED)!!, BASE_RADIUS)
                    }
                }
            }
    }



    private fun onListItemClick(position: Int) {
        val intent = Intent(requireContext(), MeetUpView::class.java)
            .apply { putExtra(MEETUP_SHOWN, (viewModel.listOfMeetUpResponse.value as Response.Success).data[position].uuid) }
        startActivity(intent)
    }

}


