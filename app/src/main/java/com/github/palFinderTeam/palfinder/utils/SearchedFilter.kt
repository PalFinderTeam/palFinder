package com.github.palFinderTeam.palfinder.utils

import android.widget.Filter
import android.widget.SearchView
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Tag
import java.util.*

class SearchedFilter<T>(
    private val dataSet: List<T>,
    private val currentDataSet: MutableList<T>,
    private val filterTag: ((MeetUp) -> Boolean)?,
    private val callBack: () -> Unit
) : Filter() {
    override fun performFiltering(constraint: CharSequence): FilterResults {
        val filteredList: MutableList<T> = mutableListOf()
        if (constraint.isEmpty() && filterTag == null) {
            filteredList.addAll(dataSet)
        } else {
            val filterPattern =
                constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
            for (item in dataSet) {
                val isContained = when (item) {
                    is Tag ->
                        filter((item as Tag).tagName, filterPattern)
                    is MeetUp ->
                        filter((item as MeetUp).name, filterPattern) && filterTag!!((item as MeetUp))
                    is ProfileUser ->
                        filter((item as ProfileUser).username, filterPattern) || filter((item as ProfileUser).fullName(), filterPattern)
                    else -> break
                }
                if (isContained) {
                    filteredList.add(item)
                }
            }
        }
        val results = FilterResults()
        results.values = filteredList
        return results
    }

    private fun filter(field: String, filterPattern: String): Boolean {
        return field.lowercase(Locale.getDefault()).contains(filterPattern)
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        currentDataSet.clear()
        currentDataSet.addAll(results.values as List<T>)
        callBack.invoke()
    }

    companion object {
        fun setupSearchField(searchField: SearchView, filter: Filter) {
            searchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filter.filter(newText)
                    return false
                }

            })
        }
    }
}