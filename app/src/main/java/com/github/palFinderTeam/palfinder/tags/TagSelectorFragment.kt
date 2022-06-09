package com.github.palFinderTeam.palfinder.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.SearchedFilter


/**
 * Fragment that shows a list tags and let the user choose one of them.
 *
 * @property availableTags options that are displayed.
 * @property tagClickListener lambda to call when an option is chosen.
 */
class TagSelectorFragment<T: Tag>(private val availableTags: List<T>, private val tagClickListener: (T) -> Unit) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //inflate layout with recycler view
        val v: View = inflater.inflate(R.layout.dialog_tag_selector, container, false)
        val recyclerView = v.findViewById(R.id.tag_selector_recycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = TagAdapter(availableTags)
        recyclerView.adapter = adapter
        val searchField = v.findViewById<SearchView>(R.id.tag_selector_search)
        searchField.imeOptions = EditorInfo.IME_ACTION_DONE
        SearchedFilter.setupSearchField(searchField, adapter.filter )

        val addTagButton = v.findViewById<Button>(R.id.add_tag_button)
        addTagButton.setOnClickListener {
            adapter.selectedTags.forEach {
                tagClickListener(it)
            }
            dialog?.dismiss()
        }
        //get your recycler view and populate it.
        return v
    }

    override fun onStart() {
        super.onStart()

        // Force the dialog to take whole width
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
}