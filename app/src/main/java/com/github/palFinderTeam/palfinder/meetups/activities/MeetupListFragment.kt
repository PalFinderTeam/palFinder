package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.content.Intent
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
import com.github.palFinderTeam.palfinder.utils.time.*
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private lateinit var selectStartTime: TextView
    private lateinit var selectEndTime: TextView

    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar

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

        //setup the searchField that will be given to the adapter as argument
        val searchField = view.findViewById<SearchView>(R.id.search_list)
        searchField.imeOptions = EditorInfo.IME_ACTION_DONE

        startTime = Calendar.getInstance()

        endTime = Calendar.getInstance()
        context?.resources?.let { endTime.add(Calendar.DAY_OF_MONTH, it.getInteger(R.integer.base_day_interval)) }

        selectStartTime = view.findViewById(R.id.startDateFilter)
        selectEndTime = view.findViewById(R.id.endDateFilter)

        selectStartTime.setOnClickListener {
            onStartTimeSelect()
        }

        selectEndTime.setOnClickListener {
            onEndTimeSelect()
        }


        radiusSlider = view.findViewById(R.id.distance_slider)

        radiusSlider.value = max(radiusSlider.valueFrom, min(radiusSlider.valueTo, viewModel.searchRadius.value!!.toFloat()))

        //updates the meetUps at each slider change in real time
        radiusSlider.addOnChangeListener { _, value, _ ->
            viewModel.setSearchParamAndFetch(radiusInKm = value.toDouble())
        }

        //radioGroup to choose between the different options about followers
        val followerOptions: RadioGroup = view.findViewById(R.id.follower_options_group)
        when (args.showParam) {
            ShowParam.ALL -> view.findViewById<RadioButton>(R.id.button_all).isChecked = true
            ShowParam.ONLY_JOINED -> view.findViewById<RadioButton>(R.id.joinedButton).isChecked = true
            ShowParam.PAL_PARTCIPATING -> view.findViewById<RadioButton>(R.id.participate_button).isChecked = true
            ShowParam.PAL_CREATOR -> view.findViewById<RadioButton>(R.id.created_button).isChecked = true
        }
        followerOptions.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = view.findViewById(checkedId)
            when (followerOptions.indexOfChild(radio)) {
                0 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.ALL)
                1 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.PAL_PARTCIPATING)
                2 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.PAL_CREATOR)
                3 -> viewModel.setSearchParamAndFetch(showParam = ShowParam.ONLY_JOINED)
            }
        }


        //generate a new adapter for the recyclerView every time the meetUps dataset changes
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
                    viewModel.searchLocation.value!!,
                    requireContext()
                )
                { onListItemClick(it) }
                meetupList.adapter = adapter
                SearchedFilter.setupSearchField(searchField, adapter.filter)
            }

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
        viewModel.tags.observe(requireActivity()) {
            tagsViewModel.refreshTags()
            filter(it)
        }

        view.findViewById<Button>(R.id.sort_list).setOnClickListener { showMenu(it) }
        view.findViewById<ImageButton>(R.id.search_place).setOnClickListener { searchOnMap() }

        viewModel.setSearchParamAndFetch(showParam = args.showParam, showOnlyAvailable = true, forceFetch = true)
    }

    /**
     * function used to filter one meetup according to the current list of tags of the viewModel
     * @param meetup current meetup
     */
    private fun filterTags(meetup: MeetUp): Boolean {
        return meetup.tags.containsAll(viewModel.tags.value!!)
    }



    private fun filterDate(meetup: MeetUp): Boolean{
        return meetup.startDate.after(startTime) and meetup.endDate.before(endTime)
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
            (tags == null || it.tags.containsAll(tags)) and filterDate(it)
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
        viewModel.listOfMeetUpResponse.value?.let { adapter.currentDataSet.addAll(sorted) }
        adapter.notifyDataSetChanged()
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
                else -> {}
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
                    (viewModel.listOfMeetUpResponse.value as Response.Success).data[position].uuid
                )
            }
        startActivity(intent)
    }

    //button to select the start Date of meetup
    private fun onStartTimeSelect() {
        askTime(
            childFragmentManager,
            startTime.toSimpleDate(),
            startTime.toSimpleTime(),
            minDate = Calendar.getInstance()
        ).thenAccept {
            val interval = endTime.timeInMillis - startTime.timeInMillis
            startTime = it.clone() as Calendar
            endTime = it.clone() as Calendar
            endTime.timeInMillis = it.timeInMillis + interval
        }
    }

    //button to select the end Date of meetup
    private fun onEndTimeSelect() {
        askTime(
            childFragmentManager,
            endTime.toSimpleDate(),
            endTime.toSimpleTime(),
            minDate = startTime
        ).thenAccept {
            endTime = it.clone() as Calendar
        }
    }

}


