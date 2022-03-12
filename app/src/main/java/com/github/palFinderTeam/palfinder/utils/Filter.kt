package com.github.palFinderTeam.palfinder.utils

import android.widget.Filter
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.tag.Tag
import java.util.*

    public class searchedFilter<T>(private val dataSet : List<T>, private val currentDataSet : MutableList<T>, private val callBack : () -> Unit): Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<T> = mutableListOf()
            if (constraint.isEmpty()) {
                filteredList.addAll(dataSet)
            } else {
                val filterPattern =
                    constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                for (item in dataSet) {
                    when(item) {
                        is Tag ->
                            if ((item as Tag).tagName.lowercase(Locale.getDefault()).contains(filterPattern)) {
                                filteredList.add(item)
                            }
                        is MeetUp ->
                            if ((item as MeetUp).name.lowercase(Locale.getDefault()).contains(filterPattern)) {
                                filteredList.add(item)
                            }
                        else ->
                            break
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            currentDataSet.clear()
            currentDataSet.addAll(results.values as List<T>)
            callBack.invoke()
        }
    }