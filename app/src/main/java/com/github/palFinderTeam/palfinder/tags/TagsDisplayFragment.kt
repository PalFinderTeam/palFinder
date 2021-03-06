package com.github.palFinderTeam.palfinder.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.palFinderTeam.palfinder.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Fragment that display a set of tag as chips. If the set is editable, it also provides visual ways
 * to modify it.
 */
class TagsDisplayFragment<T: Tag> : Fragment() {

    // Grab the viewModel from the parent activity/fragment.
    private val viewModel: TagsViewModel<T> by viewModels(
        ownerProducer = {
            try {
                requireParentFragment()
            } catch (e: IllegalStateException) {
                requireActivity()
            }
        }
    )

    private lateinit var tagGroup: ChipGroup
    private lateinit var plusButton: ImageView //Changed from button for design purposes
    private lateinit var placeholderText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_tags_display, container, false)

        tagGroup = binding.findViewById(R.id.tag_group)
        plusButton = binding.findViewById(R.id.addTagButton)
        placeholderText = binding.findViewById(R.id.tv_add_tags)

        viewModel.isEditable.observe(viewLifecycleOwner) { isEditable ->
            setEditableUI(isEditable)
        }
        plusButton.setOnClickListener { showTagSelector() }

        viewModel.tagContainer.observe(viewLifecycleOwner) { newTags ->
            displayTags(newTags)
        }

        return binding.rootView
    }

    private fun setEditableUI(editable: Boolean) {
        tagGroup.forEach { chip ->
            if (chip is Chip) {
                chip.isCloseIconVisible = editable
            }
        }

        if (editable) {
            plusButton.visibility = VISIBLE
            placeholderText.setOnClickListener { showTagSelector() }
        } else {
            plusButton.visibility = GONE
            placeholderText.text = getString(R.string.tags_no_tag)
        }
    }

    private fun displayTags(tags: Set<T>) {
        // Display placeholder or tags area
        if(tags.isEmpty()){
            placeholderText.visibility = VISIBLE
            tagGroup.visibility = GONE
        } else {
            placeholderText.visibility = GONE
            tagGroup.visibility = VISIBLE
        }

        // We recreate the whole list, it is a bit overkill but much simpler
        tagGroup.removeAllViews()
        tags.forEach { tag ->
            val chip = Chip(activity)
            chip.text = tag.tagName
            chip.isCloseIconVisible = viewModel.isEditable.value == true
            chip.setOnCloseIconClickListener {
                viewModel.removeTag(tag)
            }

            tagGroup.addView(chip)
        }
    }

    private fun showTagSelector() {
        val fm = parentFragmentManager
        val currentTags = viewModel.tagContainer.value
        val allTags = viewModel.allTags

        if (currentTags != null) {
            // Only show tags that are not already added
            val tagsOptions = allTags.toSet().minus(currentTags)

            TagSelectorFragment(tagsOptions.toList()) { tag -> viewModel.addTag(tag)}
                .show(fm, "Tag selector")
        }
    }
}

