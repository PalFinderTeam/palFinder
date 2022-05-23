package com.github.palFinderTeam.palfinder.utils.transformer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

class ListTransformer<T> {
    private var filters = emptyList<(T) -> Boolean>().toMutableList()
    private var sorter: ((T) -> Comparable<Any>)? = null

    private var output = MutableLiveData<List<T>>()
    private var input: MutableLiveData<List<T>>? = null

    private fun update(){
        if (input != null && input!!.value != null){
            output.value = transform(input!!.value!!)
        }
    }

    fun addFilter(filter: (T) -> Boolean){
        filters.add(filter)
        update()
    }

    fun setSorter(sorter: (T) -> Comparable<Any>){
        this.sorter = sorter
        update()
    }

    fun transform(list: List<T>):List<T>{
        var lst = list
        for (filter in filters){
            lst = list.filter { filter(it) }
        }
        if (sorter != null){
            lst.sortedBy { sorter!!(it) }
        }
        return lst
    }

    fun transform(lifecycleOwner: LifecycleOwner, list: MutableLiveData<List<T>>): MutableLiveData<List<T>>{
        list.observe(lifecycleOwner){
            output.value = transform(it)
        }
        return output
    }
}