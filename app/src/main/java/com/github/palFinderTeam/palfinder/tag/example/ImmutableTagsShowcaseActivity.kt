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
    private lateinit var tagsViewModelFactory: TagsViewModelFactory
    private lateinit var tagsViewModel: TagsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // Create the viewModel here, it will be automatically shared to every child fragments.
        tagsViewModelFactory = TagsViewModelFactory(NonEditableTags(mutableSetOf(Tag.DRINKING, Tag.CINEMA)))
        tagsViewModel = ViewModelProvider(this, tagsViewModelFactory).get(TagsViewModel::class.java)

        setContentView(R.layout.activity_immutable_tags_showcase)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<TagsDisplayFragment>(R.id.tag_fragment_immutable)
            }
        }

        val goButton = findViewById<Button>(R.id.go_to_mutable_tag_showcase)
        goButton.setOnClickListener {
            val intent = Intent(this, TagShowcaseActivity::class.java)
            startActivity(intent)
        }

    }
}