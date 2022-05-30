package com.github.palFinderTeam.palfinder.utils.transformer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.github.palFinderTeam.palfinder.utils.Response

/**
 * Class to transform the data from the API to the data that is used in the UI
 * by filtering and sorting the data
 */
class ListTransformer<T> {
    private var filters = emptyMap<String, (T) -> Boolean>().toMutableMap()
    private var sorter: ((T) -> Any)? = null

    private var output = MutableLiveData<List<T>>()
    private var input: MutableLiveData<List<T>> = MutableLiveData(listOf())

    /**
     * Updates the list of items to be filtered and sorted.
     */
    private fun update(){
        output.postValue(transform(input.value!!))
    }

    /**
     * set a filter
     * @param uuid: Id of the filter (useful to remove it later)
     * @param filter
     */
    fun setFilter(uuid: String, filter: (T) -> Boolean){
        filters[uuid] = filter
        update()
    }

    /**
     * Remove a filter
     * @param uuid: Id of the filter
     */
    fun removeFilter(uuid: String){
        filters.remove(uuid)
        update()
    }

    /**
     * Set a sorter
     * @param sorter
     */
    fun setSorter(sorter: (T) -> Any){
        this.sorter = sorter
        update()
    }

    /**
     * Transform the list by applying all filters and sorters
     */
    fun transform(list: List<T>):List<T>{
        var lst = list
        for (filter in filters.values){
            lst = lst.filter { filter(it) }
        }
        if (sorter != null){
            lst = lst.sortedBy { sorter!!(it) as Comparable<Any> }
        }
        return lst
    }

    /**
     * Set the input of the transformer
     * @param list: List to be transformed with the filters and sorters
     */
    fun transformResponseList(list: MutableLiveData<Response<List<T>>>): MutableLiveData<List<T>>{
        list.observeForever {
            when (it){
                is Response.Success -> {
                    input.postValue(it.data!!)
                }
                else -> {
                    if (input.value == null){
                        input.postValue(emptyList())
                    }
                }
            }
        }

        return transform(input)
    }

    /**
     * Set the input of the transformer
     * @param list: List to be transformed with the filters and sorters
     */
    fun transform(list: MutableLiveData<List<T>>): MutableLiveData<List<T>>{
        input = list

        input.observeForever {
            output.value = transform(it)
        }
        return output
    }
}