package com.github.palFinderTeam.palfinder.map

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.activities.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.meetups.activities.MeetUpView
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Location.Companion.toLocation
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.IOException
import kotlin.math.pow


const val LOCATION_SELECT = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECT"
const val LOCATION_SELECTED = "com.github.palFinderTeam.palFinder.MAP.LOCATION_SELECTED"
const val CONTEXT = "com.github.palFinderTeam.palFinder.MAP.CONTEXT"

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    SearchView.OnQueryTextListener {

    private lateinit var selectLocationButton: FloatingActionButton
    private lateinit var selectMapTypeButton: FloatingActionButton
    private lateinit var context: Context
    private lateinit var map: GoogleMap

    private val mapSelection: MapsSelectionModel by viewModels()
    private val viewModel: MapListViewModel by activityViewModels()
    private val args: MapsFragmentArgs by navArgs()

    private val markers = HashMap<String, Marker>()
    private var meetUpForMarkers: Set<MeetUp> = emptySet()
    private var mapReady = false

    enum class Context {
        MARKER,
        SELECT_LOCATION
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_maps, container, false).rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = args.context

        selectLocationButton = view.findViewById(R.id.bt_locationSelection)
        selectMapTypeButton = view.findViewById(R.id.bt_changeMapType)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        when (context) {
            Context.MARKER -> {
                mapSelection.active.value = false

                viewModel.listOfMeetUpResponse.observe(viewLifecycleOwner) {
                    if (it is Response.Success) {
                        refreshMarkers(it.data)
                    }
                }
            }
            Context.SELECT_LOCATION -> {
                mapSelection.active.value = true

                val startSelection = args.startSelection
                if (startSelection != null) {
                    setSelectionMarker(startSelection.toLatLng())
                }
            }
        }

        // TODO make things clear and consistent
        viewModel.userLocation.observe(requireActivity()) { location ->
            setUserLocation(location)
        }

        // Could add a condition when search parameters haven't change

        val searchLocation = view.findViewById<SearchView>(R.id.search_on_map)
        searchLocation.imeOptions = EditorInfo.IME_ACTION_DONE
        searchLocation.setOnQueryTextListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun setUserLocation(location: Location) {
        if (!mapReady) {
            return
        }

        map.isMyLocationEnabled = true

        viewModel.searchLocation.value = location
        viewModel.fetchMeetUps()
        map.animateCamera(CameraUpdateFactory.newLatLng(location.toLatLng()))
    }


    /**
     * When a meetUp marker is clicked, open the marker description
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        val id = marker.title
        if (id != null) {
            val intent = Intent(requireActivity(), MeetUpView::class.java).apply {
                putExtra(MEETUP_SHOWN, id)
            }
            startActivity(intent)
            return true
        }
        return false
    }

    private fun onMapClick(p0: LatLng) {
        // Add a marker if the map is used to select a location
        setSelectionMarker(p0)

    }

    /**
     * Add or Update the Position Selection Marker
     */
    private fun setSelectionMarker(p0: LatLng) {
        mapSelection.targetMarker.value?.remove()
        mapSelection.targetMarker.value = map.addMarker(
            MarkerOptions().position(p0).title("Here").draggable(true)
        )
        selectLocationButton.apply { this.isEnabled = mapSelection.targetMarker.value != null }
        selectLocationButton.apply { this.show() }
    }

    /**
     * Return the selected Location to the previous activity
     */
//    fun onConfirm(v: View) {
//        val resultIntent = Intent()
//        resultIntent.putExtra(LOCATION_SELECTED, mapSelection.targetMarker.value!!.position)
//        setResult(RESULT_OK, resultIntent)
//        finish()
//    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        mapReady = true

        when (context) {
            Context.MARKER -> {
                map.setOnMarkerClickListener(this)
                // Only update markers when camera is still
                map.setOnCameraIdleListener {
                    fetchMeetUpsInView()
                }
            }
            Context.SELECT_LOCATION -> {
                map.setOnMapClickListener { onMapClick(it) }
            }
        }



        selectMapTypeButton.setOnClickListener {
            changeMapType()
        }
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        var addressList: List<Address>? = null

        if (p0 == null || p0 == "") {
            Toast.makeText(
                requireContext(),
                getString(R.string.search_no_location),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val geoCoder = Geocoder(requireContext())
            try {
                addressList = geoCoder.getFromLocationName(p0, 1)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (addressList == null || addressList.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.search_location_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val address = addressList[0]
                val location = Location(address.longitude, address.latitude)
                viewModel.searchLocation.value = location
                viewModel.fetchMeetUps()
                map.animateCamera(CameraUpdateFactory.newLatLng(location.toLatLng()))
            }
        }
        return false
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        return false
    }

    private fun changeMapType() {
        if (map.mapType == GoogleMap.MAP_TYPE_NORMAL) {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
        } else {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    /**
     * get the Marker in this utils memory corresponding to this id
     * @param id: Unique identifier of the meetup
     * @return the marker corresponding to the id, null if non existent
     */
    private fun getMarker(id: String): Marker? {
        return markers[id]
    }

    /**
     * clear the map of all markers
     */
    private fun clearMarkers() {
        val iterator = markers.iterator()
        while (iterator.hasNext()) {
            val marker = iterator.next()
            marker.value.remove()
            iterator.remove()
        }
    }

    /**
     * refresh the map to remove Marker that are not in the meetup list and
     * add those of the meetup list that are not in the map
     * if the map is not ready, do nothing
     */
    private fun refreshMarkers(meetUpList: List<MeetUp>) {
        if (!mapReady) {
            return
        }

        val deletedMarkers = meetUpForMarkers.minus(meetUpList)
        val addedMarkers = meetUpList.minus(meetUpForMarkers)
        meetUpForMarkers = meetUpList.toSet()
        deletedMarkers.forEach { markers[it.uuid]?.remove() }
        addedMarkers.forEach { meetUp ->
            val position = LatLng(meetUp.location.latitude, meetUp.location.longitude)
            map.addMarker(MarkerOptions().position(position).title(meetUp.uuid))
                ?.let { markers[meetUp.uuid] = it }
        }
    }

    /**
     * Fetch meetups that are inside the visible area of the map.
     */
    private fun fetchMeetUpsInView() {
        if (!mapReady) {
            return
        }

        val earthCircumference = 40000.0
        // at zoom 0, the map is of size 256x256 pixels and for every zoom, the number of pixel is multiplied by 2
        val radiusAtZoom0 = earthCircumference / 256
        val radius = radiusAtZoom0 / 2.0.pow(map.cameraPosition.zoom.toDouble())
        val position: Location = map.cameraPosition.target.toLocation()

        viewModel.searchRadius.value = radius
        viewModel.searchLocation.value = position
        viewModel.fetchMeetUps()
    }
}