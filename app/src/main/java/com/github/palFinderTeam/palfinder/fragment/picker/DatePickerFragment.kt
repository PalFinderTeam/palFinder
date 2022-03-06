package com.github.palFinderTeam.palfinder.fragment.picker

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import android.widget.SimpleAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.github.palFinderTeam.palfinder.utils.SimpleDate
import com.github.palFinderTeam.palfinder.utils.SimpleTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

// Based on Android Official Documentation
@RequiresApi(Build.VERSION_CODES.N)
class DatePickerFragment(private val date: SimpleDate? = null) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    val value: CompletableFuture<SimpleDate> = CompletableFuture()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if (date == null) {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), this, year, month, day)
        } else{
            DatePickerDialog(requireContext(), this, date.year, date.month, date.day)
        }
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        value.complete(SimpleDate(year, month, day))
    }
}