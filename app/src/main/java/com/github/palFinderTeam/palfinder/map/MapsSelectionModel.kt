package com.github.palFinderTeam.palfinder.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.Marker

class MapsSelectionModel : ViewModel() {
    val targetMarker: MutableLiveData<Marker?> by lazy {
        MutableLiveData<Marker?>()
    }
}