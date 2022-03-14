package com.github.palFinderTeam.palfinder.utils

import android.widget.Adapter
import android.widget.Filter
import android.widget.SearchView
import com.github.palFinderTeam.palfinder.meetups.MeetUp
import com.github.palFinderTeam.palfinder.tag.Tag
import java.util.*

    public class searchedFilter<T>(private val dataSet : List<T>, private val currentDataSet : MutableList<T>, private val callBack : () -> Unit): Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<T> = mutableListOf()
            if (constraint.isEmpty()) {
                filteredList.addAll(dataSet)
            } else {
                for (item in dataSet) {
                    when(item) {
                        is Tag ->
                            filter(filteredList, (item as Tag).tagName, item, constraint)
                        is MeetUp ->
                            filter(filteredList, (item as MeetUp).name, item, constraint)
                        else ->
                            break
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        private fun filter(filteredList: MutableList<T>, field: String, item: T, constraint:CharSequence) {
            val filterPattern =
                constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
            if (field.lowercase(Locale.getDefault()).contains(filterPattern)) {
                filteredList.add(item)
            }
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