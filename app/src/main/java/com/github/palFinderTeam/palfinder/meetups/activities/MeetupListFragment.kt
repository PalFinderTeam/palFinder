package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import kotlinx.coroutines.ExperimentalCoroutinesApi


const val SHOW_JOINED_ONLY = "com.github.palFinderTeam.palFinder.meetup_list_view.SHOW_JOINED_ONLY"
const val BASE_RADIUS = 500.0
const val LOCATION_RESULT = "location"

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MeetupListFragment : Fragment() {
    private lateinit var meetupList: RecyclerView
    lateinit var adapter: MeetupListAdapter
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>

    val viewModel: MapListViewModel by activityViewModels()

    private val args: MeetupListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_list, container, false).rootView
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        meetupList = view.findViewById(R.id.meetup_list_recycler)
        meetupList.layoutManager = LinearLayoutManager(requireContext())
        val searchField = view.findViewById<SearchView>(R.id.search_list)
        searchField.imeOptions = EditorInfo.IME_ACTION_DONE

        viewModel.listOfMeetUpResponse.observe(requireActivity()) { it ->
            if (it is Response.Success && viewModel.searchLocation.value != null) {
                val meetups = it.data
                adapter = MeetupListAdapter(
                    meetups, meetups.toMutableList(),
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

//        viewModel.searchLocation.observe(requireActivity()) {
//            viewModel.fetchMeetUps()
//        }

        getNavigationResultLiveData<Location>(LOCATION_RESULT)?.observe(viewLifecycleOwner) { result ->
            viewModel.setSearchParamAndFetch(location = result)
            // Make sure to consume the value
            removeNavigationResult<Location>(LOCATION_RESULT)
        }

        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)
        if (savedInstanceState == null) {
            addTagsToFragmentManager(childFragmentManager, R.id.list_select_tag)
        }
        viewModel.tags.observe(requireActivity()) {
            tagsViewModel.refreshTags()
            filter(it)
        }

        view.findViewById<Button>(R.id.sort_list).setOnClickListener { showMenu(it) }
        view.findViewById<ImageButton>(R.id.search_place).setOnClickListener { searchOnMap() }

        viewModel.setSearchParamAndFetch(showOnlyJoined = args.showOnlyJoined)
    }

    private fun filterTags(meetup: MeetUp): Boolean {
        return meetup.tags.containsAll(viewModel.tags.value!!)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun filter(tags: Set<Category>?) {
        if (::adapter.isInitialized) {
            adapter.currentDataSet.clear()
            viewModel.listOfMeetUpResponse.value?.let { meetups ->
                if (meetups is Response.Success) {
                    performFilter(meetups.data, adapter.currentDataSet, tags)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun performFilter(
        meetups: List<MeetUp>,
        currentDataSet: MutableList<MeetUp>,
        tags: Set<Category>?
    ) {
        currentDataSet.addAll(meetups.filter {
            (tags == null || it.tags.containsAll(tags))
        })
    }

    private fun sortByCap() {
        if (::adapter.isInitialized) {
            val sorted = adapter.currentDataSet.sortedBy { it.capacity }
            sort(sorted)
        }
    }

    private fun sortByName() {
        if (::adapter.isInitialized) {
            val sorted = adapter.currentDataSet.sortedBy { it.name.lowercase() }
            sort(sorted)
        }
    }

    private fun sortByDist() {
        if (::adapter.isInitialized) {
            val sorted =
                adapter.currentDataSet.sortedBy {
                    it.location.distanceInKm(
                        viewModel.searchLocation.value ?: MapListViewModel.START_LOCATION
                    )
                }
            sort(sorted)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sort(sorted: List<MeetUp>) {
        adapter.currentDataSet.clear()
        viewModel.listOfMeetUpResponse.value?.let { it -> adapter.currentDataSet.addAll(sorted) }
        adapter.notifyDataSetChanged()
    }

    private fun showMenu(view: View?) {
        val popupMenu = PopupMenu(requireContext(), view) //View will be an anchor for PopupMenu
        popupMenu.inflate(R.menu.sort)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
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

    private fun searchOnMap() {
        val action = MeetupListFragmentDirections.actionListPickLocation()
        findNavController().navigate(action)
    }

    private fun onListItemClick(position: Int) {
        val intent = Intent(requireContext(), MeetUpView::class.java)
            .apply {
                putExtra(
                    MEETUP_SHOWN,
                    (viewModel.listOfMeetUpResponse.value as Response.Success).data[position].uuid
                )
            }
        startActivity(intent)
    }

}


