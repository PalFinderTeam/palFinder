package com.github.palFinderTeam.palfinder.fragment.picker

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.github.palFinderTeam.palfinder.utils.time.SimpleTime
import java.util.*
import java.util.concurrent.CompletableFuture

// Based on Android Official Documentation
/**
 * Ask user a time (hour and minute) and return the value in the future "value"
 * @param time: Time to display by default
 */
@RequiresApi(Build.VERSION_CODES.N)
class TimePickerFragment(private val time: SimpleTime? = null) : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    val value: CompletableFuture<SimpleTime> = CompletableFuture()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if (time == null) {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
        }
        else{
            TimePickerDialog(activity, this, time.hour, time.minute, DateFormat.is24HourFormat(activity))
        }
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        value.complete(SimpleTime(hourOfDay, minute))
    }
}