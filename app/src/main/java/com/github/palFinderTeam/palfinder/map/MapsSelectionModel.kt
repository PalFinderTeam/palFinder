package com.github.palFinderTeam.palfinder.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.android.gms.maps.model.Marker

class MapsSelectionModel : ViewModel() {
    val targetLocation: MutableLiveData<Location> by lazy {
        MutableLiveData()
    }

    val active: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}