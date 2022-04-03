package com.github.palFinderTeam.palfinder.fragment.picker

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.github.palFinderTeam.palfinder.utils.SimpleDate
import java.util.concurrent.CompletableFuture

/**
 * Ask user a date (year, month and day) and return the value in the future "value"
 * @param date: Date to display by default
 */
// Based on Android Official Documentation
@RequiresApi(Build.VERSION_CODES.N)
class DatePickerFragment(
    private val date: SimpleDate? = null,
    private val minDate: Calendar? = null,
    private val maxDate: Calendar? = null
) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    val value: CompletableFuture<SimpleDate> = CompletableFuture()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            this,
            date?.year ?: year,
            date?.month ?: month,
            date?.day ?: day
        ).also { dialog ->
            minDate?.let { dialog.datePicker.minDate = it.timeInMillis }
            maxDate?.let { dialog.datePicker.maxDate = it.timeInMillis }
        }
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        value.complete(SimpleDate(year, month, day))
    }
}