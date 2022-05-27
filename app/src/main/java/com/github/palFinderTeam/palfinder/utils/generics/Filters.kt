package com.github.palFinderTeam.palfinder.utils.generics

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.meetups.activities.MapListViewModel
import com.github.palFinderTeam.palfinder.utils.transformer.ListTransformer

object Filters{
    val TEXT_FILTER = "text"
}

/**
 * Add a text filter to the filterer
 */
fun <T: StringFilterable> filterByText(filterer: ListTransformer<T>, query: String?) {
    if (query != null) {
        filterer.setFilter(Filters.TEXT_FILTER) { it.containsString(query) }
    } else {
        filterer.removeFilter(Filters.TEXT_FILTER)
    }
}

/**
 * bind the SearchField to the viewModel and set the listener to update the filter list
 */
fun <T: StringFilterable> setupSearchField(view: View, id: Int, filterer: ListTransformer<T>) {
    val searchField = view.findViewById<SearchView>(id)
    searchField.imeOptions = EditorInfo.IME_ACTION_DONE
    searchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            filterByText(filterer, newText)
            return true
        }
    })
}