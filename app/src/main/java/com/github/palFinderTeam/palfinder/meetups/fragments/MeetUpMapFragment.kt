package com.github.palFinderTeam.palfinder.meetups.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.MapsSelectionModel
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class MeetUpMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnCameraMoveListener, SearchView.OnQueryTextListener {


    private val mapSelection: MapsSelectionModel by viewModels()
    private val viewModel: MapListViewModel by activityViewModels()

    private lateinit var button: FloatingActionButton
    private lateinit var mapView: View
    private lateinit var searchLocation: SearchView

    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val args: MeetUpMapFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_map, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setUserLocation()
        childFragmentManager.commit {

        }
        return binding.rootView
    }

    private fun setUserLocation() {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button = view.findViewById(R.id.bt_locationSelection)
        viewModel.update()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        viewModel.listOfMeetUpResponse.observe(viewLifecycleOwner) {
            if (it is Response.Success) {
                viewModel.refresh()
            }
        }

        searchLocation = view.findViewById(R.id.search_on_map)
        searchLocation.imeOptions = EditorInfo.IME_ACTION_DONE
        searchLocation.setOnQueryTextListener(this)
    }

    private fun loadSelectionButton() {
        args.replaceLocation
        val selectedLocation = args.replaceLocation
        if (args.selectLocation) {
            mapSelection.active.value = true
            button.apply {
                this.isEnabled = false
                this.show()
            }
            if (selectedLocation != null) {
                setSelectionMarker(selectedLocation)
            }
            button.setOnClickListener {
                onConfirm(it)
            }
        } else {
            mapSelection.active.value = false
        }
    }


    /**
     * When a meetUp marker is clicked, open the marker description
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        // Avoid clicking on your own marker
        if (mapSelection.targetMarker.value != marker) {
            val id = marker.title
            if (id != null) {
                val action = MeetUpMapFragmentDirections.actionMapFragmentToMeetupShowFragment(id)
                findNavController().navigate(action)
                return true
            }
        }
        return false
    }

    private fun onMapClick(p0: LatLng) {
        // Add a marker if the map is used to select a location
        if (mapSelection.active.value!!) {
            setSelectionMarker(p0)
        }
    }

    /**
     * Add or Update the Position Selection Marker
     */
    private fun setSelectionMarker(p0: LatLng) {
        mapSelection.targetMarker.value?.remove()
        mapSelection.targetMarker.value = map.addMarker(
            MarkerOptions().position(p0).title("Here").draggable(true)
        )
        button.apply { this.isEnabled = mapSelection.targetMarker.value != null }
    }

    /**
     * Return the selected Location to the previous activity
     */
    private fun onConfirm(v: View) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            "selectedLocation",
            mapSelection.targetMarker.value!!.position
        )
        findNavController().navigateUp()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        viewModel.setGmap(map)
        viewModel.mapReady = true
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        map.setOnCameraMoveListener(this)
        setUserLocation()


        map.setOnMapClickListener { onMapClick(it) }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapView = mapFragment.requireView()
        mapView.contentDescription = "MAP NOT READY"
        mapView.contentDescription = "MAP READY"
        loadSelectionButton()
    }

    override fun onCameraMove() {
        viewModel.update()
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        var addressList: List<Address>? = null

        if (p0 == null || p0 == "") {
            Toast.makeText(context, getString(R.string.search_no_location), Toast.LENGTH_SHORT)
                .show()
        } else {
            val geoCoder = Geocoder(context)
            try {
                addressList = geoCoder.getFromLocationName(p0, 1)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (addressList == null || addressList.isEmpty()) {
                Toast.makeText(
                    context, getString(R.string.search_location_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                viewModel.setCameraPosition(latLng)
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                viewModel.update()
            }
        }
        return false
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        return false
    }

    companion object {
        private const val USER_LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}