package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetupListAdapter
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel.Companion.TEXT_FILTER
import com.github.palFinderTeam.palfinder.meetups.activities.ShowParam.ONLY_JOINED
import com.github.palFinderTeam.palfinder.meetups.fragments.CriterionsFragment
import com.github.palFinderTeam.palfinder.meetups.fragments.MeetupFilterFragment
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.*
import com.github.palFinderTeam.palfinder.utils.generics.filterByText
import com.github.palFinderTeam.palfinder.utils.generics.setupSearchField
import com.github.palFinderTeam.palfinder.utils.time.*
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.time.Period
import kotlin.math.max
import kotlin.math.min


const val LOCATION_RESULT = "location"

@ExperimentalCoroutinesApi
@AndroidEntryPoint
/**
 * Fragment used to display a list of the meetUps, with several ways to filter it.
 */
class MeetupListFragment : Fragment() {
    //recyclerView and adapter to handle the list and each meetup
    private lateinit var meetupList: RecyclerView
    lateinit var adapter: MeetupListAdapter

    //allows the user to add tags to filter the list
    private lateinit var tagsViewModel: TagsViewModel<Category>

    //allows the user to change the radius of search of meetUps around location
    private lateinit var radiusSlider: Slider

    private lateinit var filterSelectButton: Button

    private lateinit var searchMapButton: ImageButton


    //viewModel to fetch the meetups and handle the localisation
    val viewModel: MapListViewModel by activityViewModels()

    //navigation args, mainly showParam to select the type of meetUps displayed
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

        setupSearchField(view, R.id.search_list,viewModel.filterer)

        radiusSlider = view.findViewById(R.id.distance_slider)

        radiusSlider.value = max(
            radiusSlider.valueFrom,
            min(radiusSlider.valueTo, viewModel.searchRadius.value!!.toFloat())
        )

        //updates the meetUps at each slider change in real time
        radiusSlider.addOnChangeListener { _, value, _ ->
            viewModel.setSearchParamAndFetch(radiusInKm = value.toDouble())
        }

        searchMapButton = view.findViewById(R.id.search_place)
        searchMapButton.setOnClickListener { searchOnMap() }

        viewModel.showParam.observe(requireActivity()) {
            val visibility = if (it == ONLY_JOINED) {
                View.GONE
            } else {
                View.VISIBLE
            }
            radiusSlider.visibility = visibility
            searchMapButton.visibility = visibility
        }


        //generate a new adapter for the recyclerView every time the meetUps dataset changes
        viewModel.listOfMeetUp.observe(requireActivity()) { it ->
            val meetups = it
            adapter = MeetupListAdapter(
                meetups, meetups.toMutableList(),
                viewModel.searchLocation.value!!,
                requireContext()
            )
            { onListItemClick(it) }
            meetupList.adapter = adapter
        }

        // Listen for result of the map fragment, when using the select precise location functionality.
        getNavigationResultLiveData<Location>(LOCATION_RESULT)?.observe(viewLifecycleOwner) { result ->
            viewModel.setSearchParamAndFetch(location = result)
            // Make sure to consume the value
            removeNavigationResult<Location>(LOCATION_RESULT)
        }

        // Prepare and add tags fragment.
        tagsViewModel = createTagFragmentModel(this, TagsViewModelFactory(viewModel.tagRepository))
        if (savedInstanceState == null) {
            addTagsToFragmentManager(childFragmentManager, R.id.list_select_tag)
        }
        viewModel.tags.observe(requireActivity()) { tags ->
            tagsViewModel.refreshTags()
            viewModel.filterer.setFilter("tags") { meetup ->
                meetup.tags.containsAll(tags)
            }
        }

        // Setup fragment filter window
        filterSelectButton = view.findViewById(R.id.select_filters)
        filterSelectButton.setOnClickListener {
            MeetupFilterFragment(viewModel).show(
                childFragmentManager,
                getString(R.string.meetup_filter_title)
            )
        }

        view.findViewById<Button>(R.id.sort_list).setOnClickListener { showMenu(it) }

        viewModel.setSearchParamAndFetch(
            showParam = args.showParam,
            showOnlyAvailable = true,
            forceFetch = true
        )
    }

    /**
     * Sort the list of meetUps by capacity
     */
    private fun sortByCap() {
        viewModel.filterer.setSorter { it.capacity }

    }

    /**
     * Sort the list of meetUps by Name
     */
    private fun sortByName() {
        viewModel.filterer.setSorter { it.name.lowercase() }
    }

    /**
     * Sort the list of meetUps by distance
     */
    private fun sortByDist() {
        viewModel.filterer.setSorter {
            it.location.distanceInKm(
                viewModel.searchLocation.value ?: MapListViewModel.START_LOCATION
            )
        }
    }

    /**
     * Set the tags. (Use for testing)
     */
    fun setTags(tags: Set<Category>) {
        viewModel.tags.postValue(tags)
    }

    //button make a menu appears, with several sort options
    private fun showMenu(view: View?) {
        val popupMenu = PopupMenu(requireContext(), view) //View will be an anchor for PopupMenu
        popupMenu.inflate(R.menu.sort)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_sort_name -> sortByName()
                R.id.menu_sort_cap -> sortByCap()
                R.id.menu_sort_loc -> sortByDist()
                else -> {
                }
            }
            false
        })
        popupMenu.show()
    }

    //opens another map fragment to select the location to fetch around
    private fun searchOnMap() {
        val action = MeetupListFragmentDirections.actionListPickLocation()
        findNavController().navigate(action)
    }

    //allows to jump to the meetupView when clicking on a meetup in the list
    private fun onListItemClick(position: Int) {
        val intent = Intent(requireContext(), MeetUpView::class.java)
            .apply {
                putExtra(
                    MEETUP_SHOWN,
                    viewModel.listOfMeetUp.value!![position].uuid
                )
            }
        startActivity(intent)
    }


}


