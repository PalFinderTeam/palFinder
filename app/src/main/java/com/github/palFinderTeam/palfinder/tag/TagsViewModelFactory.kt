package com.github.palFinderTeam.palfinder.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Since we cannot pass arguments to viewModels directly, we can use a factory class to do the job.
 *
 * @property tagsRepository repository pass to the viewModel.
 */
class TagsViewModelFactory<T: Tag>(private val tagsRepository: TagsRepository<T>) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagsViewModel::class.java)) {
            return TagsViewModel(tagsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}