package com.github.palFinderTeam.palfinder.meetups.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.MeetupListAdapter
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.tag.TagsViewModelFactory
import com.github.palFinderTeam.palfinder.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@SuppressLint("NotifyDataSetChanged")
@AndroidEntryPoint
class MeetupListFragment : Fragment() {


    private lateinit var meetupList: RecyclerView
    private lateinit var adapter: MeetupListAdapter
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>
    private lateinit var searchField: SearchView
    private lateinit var sortButton: Button

    private lateinit var map: GoogleMap
    private lateinit var lastLocation: android.location.Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: MapListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_meetup_list, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setUserLocation()
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        meetupList = view.findViewById(R.id.meetup_list_recycler)
        meetupList.layoutManager = LinearLayoutManager(view.context)

        searchField = view.findViewById(R.id.search_list)
        searchField.imeOptions = EditorInfo.IME_ACTION_DONE

        sortButton = view.findViewById(R.id.sort_list)
        sortButton.setOnClickListener { showMenu(it) }


        //viewModel.showOnlyJoined = intent.getBooleanExtra(SHOW_JOINED_ONLY,false)
        viewModel.update()

        viewModel.listOfMeetUpResponse.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Response.Success -> {
                    val meetups = it.data.filter { filter(it) }
                    adapter = MeetupListAdapter(meetups, meetups.toMutableList(),
                        SearchedFilter(
                            meetups, meetups.toMutableList(), ::filter
                        ) {
                            adapter.notifyDataSetChanged()
                        })
                    { onListItemClick(it) }
                    meetupList.adapter = adapter
                    SearchedFilter.setupSearchField(searchField, adapter.filter)
                }
                is Response.Failure -> TODO()
                else -> {
                    Log.i("jap", "Fetching")
                }
            }
        }

        tagsViewModelFactory = TagsViewModelFactory(viewModel.tagRepository)
        tagsViewModel = createTagFragmentModel(this, tagsViewModelFactory)
        addTagsToFragmentManager(childFragmentManager, R.id.list_select_tag)

        viewModel.tags.observe(viewLifecycleOwner) {
            tagsViewModel.refreshTags()
            filter(it)
        }
    }

    private fun filter(meetup: MeetUp): Boolean {
        return if (viewModel.showOnlyJoined) {
            filterTags(meetup) && isParticipating(meetup)
        } else {
            filterTags(meetup)
        }
    }

    private fun filterTags(meetup: MeetUp): Boolean {
        return meetup.tags.containsAll(viewModel.tags.value!!)
    }

    private fun isParticipating(meetup: MeetUp): Boolean {
        val user = viewModel.getUser()
        return if (user != null) {
            meetup.isParticipating(user)
        } else {
            false
        }
    }


    fun filter(tags: Set<Category>?) {
        if (::adapter.isInitialized) {
            adapter.currentDataSet.clear()
            viewModel.listOfMeetUpResponse.value?.let { meetups ->
                performFilter(
                    (meetups as Response.Success).data,
                    adapter.currentDataSet,
                    tags
                )
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
            (tags == null || it.tags.containsAll(tags)) &&
                    (!viewModel.showOnlyJoined || isParticipating(it))
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
                adapter.currentDataSet.sortedBy { it.location.distanceInKm(Location(0.0, 0.0)) }
            sort(sorted)
        }
    }

    private fun sort(sorted: List<MeetUp>) {
        adapter.currentDataSet.clear()
        viewModel.listOfMeetUpResponse.value?.let { it -> adapter.currentDataSet.addAll(sorted) }
        adapter.notifyDataSetChanged()
    }

    fun showMenu(view: View?) {
        val popupMenu = PopupMenu(context, view) //View will be an anchor for PopupMenu
        popupMenu.inflate(R.menu.sort)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_sort_name -> sortByName()
                R.id.menu_sort_cap -> sortByCap()
                R.id.menu_sort_loc -> sortByDist()
                else -> {}
            }
            false
        }
        popupMenu.show()
    }


    private fun onListItemClick(position: Int) {
        val meetUpId = (viewModel.listOfMeetUpResponse.value as Response.Success).data[position].uuid
        val action = MeetupListFragmentDirections.actionMeetupListFragmentToMeetupShowFragment(meetUpId)
        findNavController().navigate(action)
    }

    fun setUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                USER_LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        if (::map.isInitialized) {
            map.isMyLocationEnabled = true
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                viewModel.setPositionAndZoom(currentLatLng, viewModel.getZoom())
                viewModel.update()
            }
        }
        viewModel.update()
    }

    companion object {
        private const val USER_LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}