package com.github.palFinderTeam.palfinder.tag

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class TagDisplayFragmentFactory<T>(private val tagClass: Class<T>) : FragmentFactory()
    where T : Enum<T>,
          T : Tag {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
            TagsDisplayFragment::class.java.name -> TagsDisplayFragment(tagClass)
            else -> super.instantiate(classLoader, className)
        }
    }
}