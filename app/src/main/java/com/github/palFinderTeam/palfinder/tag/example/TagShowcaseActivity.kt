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

/**
 * An activity to showcase the feature, it can be used as an example but should not be part of the
 * final app.
 */
class TagShowcaseActivity : AppCompatActivity() {
    private lateinit var tagsViewModelFactory: TagsViewModelFactory
    private lateinit var tagsViewModel: TagsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // Create the viewModel here, it will be automatically shared to every child fragments.
        tagsViewModelFactory = TagsViewModelFactory(EditableTags(mutableSetOf(Tag.DRINKING, Tag.CINEMA)))
        tagsViewModel = ViewModelProvider(this, tagsViewModelFactory).get(TagsViewModel::class.java)

        setContentView(R.layout.activity_tag_showcase)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<TagsDisplayFragment>(R.id.tag_fragment)
            }
        }
        val goButton = findViewById<Button>(R.id.go_to_immutable_tag_showcase)
        goButton.setOnClickListener {
            val intent = Intent(this, ImmutableTagsShowcaseActivity::class.java)
            startActivity(intent)
        }

    }
}