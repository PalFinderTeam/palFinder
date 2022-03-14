package com.github.palFinderTeam.palfinder.tag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.SearchedFilter
import com.google.android.material.chip.Chip
import java.util.*

class TagAdapter<T: Tag>(private val dataSet: List<T>) : RecyclerView.Adapter<TagAdapter.ViewHolder>(), Filterable {
    private val currentDataSet = dataSet.toMutableList()
    private val _selectedTags = mutableSetOf<T>()
    val selectedTags: Set<T> = _selectedTags
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chipView: Chip = view.findViewById(R.id.chip)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.tag_row_item, viewGroup, false)

        return ViewHolder(view)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val chipView = viewHolder.chipView
        chipView.text = currentDataSet[position].tagName
        chipView.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                _selectedTags.add(currentDataSet[position])
            } else {
                _selectedTags.remove(currentDataSet[position])
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = currentDataSet.size
    override fun getFilter() = SearchedFilter(dataSet, currentDataSet, { notifyDataSetChanged() })


}