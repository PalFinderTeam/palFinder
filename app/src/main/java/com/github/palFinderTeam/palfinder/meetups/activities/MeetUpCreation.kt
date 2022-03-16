package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.map.LOCATION_SELECT
import com.github.palFinderTeam.palfinder.map.LOCATION_SELECTED
import com.github.palFinderTeam.palfinder.map.MapsActivity
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.tag.TagsViewModel
import com.github.palFinderTeam.palfinder.utils.*
import com.google.android.gms.maps.model.LatLng


const val MEETUP_EDIT = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_EDIT"
const val defaultTimeDelta = 1000 * 60 * 60

@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
class MeetUpCreation : AppCompatActivity() {
    val model: MeetUpCreationViewModel by viewModels()
    private lateinit var tagsViewModel: TagsViewModel<Category>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var dateFormat = SimpleDateFormat()

    override fun onCreate(savedInstanceState: Bundle?) {
        registerActivityResult()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_creation)

        dateFormat = SimpleDateFormat(getString(R.string.date_long_format))

        loadIntent()

        if (savedInstanceState == null) {
            addTagsToFragmentManager(supportFragmentManager, R.id.fc_tags)
        }

        loadObservers()
    }

    private fun registerActivityResult(){
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    onLocationSelected(data.getParcelableExtra(LOCATION_SELECTED)!!)
                }
            }
        }
    }

    private fun loadObservers() {
        val startDateObs = Observer<Calendar> { newDate ->
            checkDateIntegrity()
            setTextView(R.id.tv_StartDate, dateFormat.format(newDate))
        }
        model.startDate.observe(this, startDateObs)

        val endDateObs = Observer<Calendar> { newDate ->
            checkDateIntegrity()
            setTextView(R.id.tv_EndDate, dateFormat.format(newDate))
        }
        model.endDate.observe(this, endDateObs)

        val locationObs = Observer<Location> { newLocation ->
            setTextView(R.id.tv_LocationCoordinate, newLocation.toString())
        }
        model.location.observe(this, locationObs)
    }

    private fun loadIntent() {
        lateinit var tagsSet: Set<Category>
        if (intent.hasExtra(MEETUP_EDIT)) {
            val meetup = intent.getSerializableExtra(MEETUP_EDIT) as MeetUp
            tagsSet = meetup.tags
            fillFields(meetup)
        } else {
            model.startDate.value = Calendar.getInstance()
            model.endDate.value = Calendar.getInstance()
            tagsSet = emptySet()
        }

        tagsViewModel = createTagFragmentModel(this, tagsSet, true)
    }

    private fun fillFields(meetUp: MeetUp){
        setTextView(R.id.et_EventName, meetUp.name)
        setTextView(R.id.et_Description, meetUp.description)

        if (meetUp.hasMaxCapacity){
            setTextView(R.id.et_Capacity, meetUp.capacity.toString())
        }

        setStartDate(meetUp.startDate)
        setEndDate(meetUp.endDate)
        model.capacity = meetUp.capacity
        model.hasMaxCapacity = meetUp.hasMaxCapacity
    }

    private fun setTextView(id: Int, value: String){
        findViewById<TextView>(id).apply { this.text = value }
    }

    /**
     * Set Start Date on Model and update UI
     */
    private fun setStartDate(date: Calendar){
        model.startDate.value = date
    }

    /**
     * Set End Date on Model and update UI
     */
    private fun setEndDate(date: Calendar){
        model.endDate.value = date
    }

    fun onStartTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            setStartDate(it)
        }
    }
    fun onEndTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            setEndDate(it)
        }
    }

    /**
     * Enforce that End Date is After Start Date
     */
    private fun checkDateIntegrity(){
        // Check if at least defaultTimeDelta between start and end
        if (!model.startDate.value!!.isDeltaBefore(model.endDate.value!!, defaultTimeDelta)){
            val newCalendar = Calendar.getInstance()
            newCalendar.timeInMillis = model.startDate.value!!.timeInMillis
            newCalendar.add(Calendar.MILLISECOND, defaultTimeDelta)
            model.endDate.value = newCalendar
        }
    }

    /**
     * Check Name and Description are present
     */
    private fun checkFieldValid(name: String, description: String): Boolean{
        if (name == "" || description == ""){
            showMessage(R.string.meetup_creation_missing_name_desc,
                R.string.meetup_creation_missing_name_desc_title)
            return false
        }
        return true
    }

    fun onDone(v: View){
        val name = findViewById<TextView>(R.id.et_EventName).text.toString()
        val description = findViewById<TextView>(R.id.et_Description).text.toString()
        if (!checkFieldValid(name, description)) return

        val capacityText = findViewById<TextView>(R.id.et_Capacity).text.toString()
        val hasMaxCapacity = (capacityText != "")
        val capacity = if (hasMaxCapacity) { capacityText.toInt()} else { Int.MAX_VALUE }

        val m = MeetUp("dummy", ProfileUser("dummy1", "dummy2", "dummy1", model.startDate.value!!),
            "", name, description,
            model.startDate.value!!, model.endDate.value!!,
            Location(0.0,0.0),
            tagsViewModel.tagContainer.value!!,
            hasMaxCapacity, capacity, mutableListOf())

        val intent = Intent(this, MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, m)
        }
        startActivity(intent)
    }

    /**
     * Show [message] with [title] in an alert box
     */
    fun showMessage(message: Int, title: Int) {
        val dlgAlert = AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton(R.string.ok, null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    fun onSelectLocation(v: View){
        val intent = Intent(this, MapsActivity::class.java).apply {
            putExtra(LOCATION_SELECT, LatLng(0.0,0.0))
        }
        resultLauncher.launch(intent)
    }

    private fun onLocationSelected(p0: LatLng){
        model.setLatLng(p0)
    }

}