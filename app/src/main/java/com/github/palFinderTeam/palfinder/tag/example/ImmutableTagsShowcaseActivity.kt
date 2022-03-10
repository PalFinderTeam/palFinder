package com.github.palFinderTeam.palfinder.tag.example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.tag.*

class ImmutableTagsShowcaseActivity : AppCompatActivity() {
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        // Get tags from intent if any
        tagsViewModelFactory = if (intent.hasExtra(TAG_LIST)) {
            val tags = intent.getSerializableExtra(TAG_LIST) as Array<Category>
            TagsViewModelFactory(NonEditableTags(tags.toSet(), Category.values().toSet()))
        } else {
            TagsViewModelFactory(NonEditableTags(setOf(Category.DRINKING, Category.CINEMA), Category.values().toSet()))
        }
        // Create the viewModel here, it will be automatically shared to every child fragments.
        tagsViewModel = ViewModelProvider(this, tagsViewModelFactory).get(TagsViewModel::class.java) as TagsViewModel<Category>

        setContentView(R.layout.activity_immutable_tags_showcase)
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<TagsDisplayFragment<Category>>(R.id.tag_fragment_immutable)
            }
        }

        val goButton = findViewById<Button>(R.id.go_to_mutable_tag_showcase)
        goButton.setOnClickListener {
            val intent = Intent(this, TagShowcaseActivity::class.java).apply {
                putExtra(TAG_LIST, tagsViewModel.tagContainer.value!!.toTypedArray())
            }
            startActivity(intent)
        }

    }
}