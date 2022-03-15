package com.github.palFinderTeam.palfinder.utils

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.github.palFinderTeam.palfinder.tag.*

fun createTagFragmentModel(that: ViewModelStoreOwner, tags: Set<Category>, mutable: Boolean): TagsViewModel<Category> {
    val all = Category.values().toSet()
    val tagsViewModelFactory = if (mutable) {
            TagsViewModelFactory(
                EditableTags(tags.toMutableSet(), all)
            )
        } else{
            TagsViewModelFactory(
                NonEditableTags(tags, all)
            )
        }

    return ViewModelProvider(
        that,
        tagsViewModelFactory
    ).get(TagsViewModel::class.java) as TagsViewModel<Category>
}

fun addToFragmentManager(supportFragmentManager: FragmentManager, id: Int){
    supportFragmentManager.commit {
        setReorderingAllowed(true)
        add<TagsDisplayFragment<Category>>(id)
    }
}