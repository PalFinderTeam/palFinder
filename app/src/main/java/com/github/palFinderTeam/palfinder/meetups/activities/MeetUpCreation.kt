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
import com.google.android.material.snackbar.Snackbar

const val MEETUP_EDIT = "com.github.palFinderTeam.palFinder.meetup_view.MEETUP_EDIT"
const val defaultTimeDelta = 1000 * 60 * 60

@SuppressLint("SimpleDateFormat") // Apps Crash with the alternative to SimpleDateFormat
class MeetUpCreation : AppCompatActivity() {
    private val viewModel: MeetUpCreationViewModel by viewModels()
    private var dateFormat = SimpleDateFormat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up_creation)

        dateFormat = SimpleDateFormat(getString(R.string.date_long_format))

        viewModel.setStartDate(Calendar.getInstance())
        viewModel.setEndDate(Calendar.getInstance())

        if (intent.hasExtra(MEETUP_EDIT)) {
            val meetupId = intent.getStringExtra(MEETUP_EDIT)
            if (meetupId != null) {
                viewModel.loadMeetUp(meetupId)
            }
        }

        viewModel.startDate.observe(this) { newDate ->
            setTextView(R.id.tv_StartDate, dateFormat.format(newDate))
        }
        viewModel.endDate.observe(this) { newDate ->
            setTextView(R.id.tv_EndDate, dateFormat.format(newDate))
        }
        viewModel.name.observe(this) { newName ->
            setTextView(R.id.et_EventName, newName)
        }
        viewModel.description.observe(this) { newDescription ->
            setTextView(R.id.et_Description, newDescription)
        }
        viewModel.hasMaxCapacity.observe(this) { hasMaxCapacity ->
            if (hasMaxCapacity) {
                setTextView(R.id.et_Capacity, viewModel.capacity.toString())
            } else {
                // TODO Hide Capacity
            }
        }
    }

    private fun setTextView(id: Int, value: String){
        findViewById<TextView>(id).apply { this.text = value }
    }


    fun onStartTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            viewModel.setStartDate(it)
        }
    }
    fun onEndTimeSelectButton(v: View){
        askTime(supportFragmentManager).thenAccept{
            viewModel.setEndDate(it)
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

        viewModel.sendSuccess.observe(this) { isSuccessFull ->
            if (isSuccessFull) {
                val intent = Intent(this, MeetUpView::class.java).apply {
                    putExtra(MEETUP_SHOWN, viewModel.getMeetUpId())
                }
                startActivity(intent)
            } else {
                val errorSnackbar = Snackbar.make(v, "Lmao", 4)
                errorSnackbar.show()
            }
        }

        val name = findViewById<TextView>(R.id.et_EventName).text.toString()
        val description = findViewById<TextView>(R.id.et_Description).text.toString()
        if (!checkFieldValid(name, description)) return

        viewModel.sendMeetUp()

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