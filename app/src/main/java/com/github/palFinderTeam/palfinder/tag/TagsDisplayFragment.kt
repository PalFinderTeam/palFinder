package com.github.palFinderTeam.palfinder.tag

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.palFinderTeam.palfinder.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Fragment that display a set of tag as chips. If the set is editable, it also provides visual ways
 * to modify it.
 */
class TagsDisplayFragment() : Fragment() {

    // Grab the viewModel from the parent activity/fragment.
    private val viewModel: TagsViewModel by activityViewModels()
    private lateinit var tagGroup: ChipGroup
    private lateinit var plusButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_tags_display, container, false)

        tagGroup = binding.findViewById(R.id.tag_group)
        plusButton = binding.findViewById(R.id.addTagButton)

        if (!viewModel.isEditable) {
            plusButton.visibility = View.GONE
        }
        plusButton.setOnClickListener { showTagSelector() }

        viewModel.tagContainer.observe(viewLifecycleOwner) { newTags ->
            displayTags(newTags)
        }

        return binding.rootView
    }

    private fun displayTags(tags: Set<Tag>) {
        // We recreate the whole list, it is a bit overkill but much simpler
        tagGroup.removeAllViews()
        tags.forEach { tag ->
            val chip = Chip(activity)
            val paddingDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f,
                resources.displayMetrics
            ).toInt()
            chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
            chip.text = tag.tagName
            chip.isCloseIconVisible = viewModel.isEditable
            chip.setOnCloseIconClickListener {
                viewModel.removeTag(tag)
            }

            tagGroup.addView(chip)
        }
    }

    private fun showTagSelector() {
        val fm = parentFragmentManager
        val currentTags = viewModel.tagContainer.value

        if (currentTags != null) {
            // Only show tags that are not already added
            val tagsOptions = Tag.values().toSet().minus(currentTags)
            TagSelectorFragment(tagsOptions.toList()) { tag -> viewModel.addTag(tag)}
                .show(fm, "Tag selector")
        }
    }
}