package com.github.palFinderTeam.palfinder.meetups.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.meetups.TempUser
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.askTime
import com.github.palFinderTeam.palfinder.utils.isDeltaBefore

const val MEETUP_EDIT = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_EDIT"
const val defaultTimeDelta = 1000 * 60 * 60

@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
class MeetUpCreation : AppCompatActivity() {
    private val model: MeetUpCreationViewModel by viewModels()
    private var dateFormat = SimpleDateFormat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_creation)

        dateFormat = SimpleDateFormat(getString(R.string.date_long_format))

        if (intent.hasExtra(MEETUP_EDIT)) {
            val meetup = intent.getSerializableExtra(MEETUP_EDIT) as MeetUp
            fillFields(meetup)
        } else {
            updateDateFields()
        }
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

    private fun updateDateFields(){
        // Fills Date field with current date
        findViewById<TextView>(R.id.tv_StartDate).apply {
            text = dateFormat.format(model.startDate)
        }
        findViewById<TextView>(R.id.tv_EndDate).apply {
            text = dateFormat.format(model.endDate)
        }
    }

    /**
     * Set Start Date on Model and update UI
     */
    private fun setStartDate(date: Calendar){
        model.startDate = date
        checkDateIntegrity()
        updateDateFields()
    }

    /**
     * Set End Date on Model and update UI
     */
    private fun setEndDate(date: Calendar){
        model.endDate = date
        checkDateIntegrity()
        updateDateFields()
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
        if (!model.startDate.isDeltaBefore(model.endDate, defaultTimeDelta)){
            model.endDate = model.startDate
            model.endDate.add(Calendar.MILLISECOND, defaultTimeDelta)
        }
    }

    /**
     * Check Name and Description are present
     */
    private fun checkFieldValid(name: String, description: String): Boolean{
        if (name == ""){
            showMessage(R.string.meetup_creation_missing_name,
                R.string.meetup_creation_missing_name_title)
            return false
        }
        if (description == ""){
            showMessage(R.string.meetup_creation_missing_description,
                R.string.meetup_creation_missing_description_title)
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

        val m = MeetUp("dummy", TempUser("", "dummy"),
            "", name, description,
            model.startDate, model.endDate,
            Location(0.0,0.0),
            emptyList(),
            hasMaxCapacity, capacity, mutableListOf())

        val intent = Intent(this, MeetUpView::class.java).apply {
            putExtra(MEETUP_SHOWN, m)
        }
        startActivity(intent)
    }

    private fun showMessage(message: Int, title: Int) {
        val dlgAlert = AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton(R.string.ok, null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}