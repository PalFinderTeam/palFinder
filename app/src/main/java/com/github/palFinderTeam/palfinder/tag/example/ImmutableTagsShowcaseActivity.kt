package com.github.palFinderTeam.palfinder.tag.example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.tag.*

class ImmutableTagsShowcaseActivity : AppCompatActivity() {
    private lateinit var tagsViewModelFactory: TagsViewModelFactory<Category>
    private lateinit var tagsViewModel: TagsViewModel<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        // Create the viewModel here, it will be automatically shared to every child fragments.
        tagsViewModelFactory = TagsViewModelFactory(NonEditableTags(setOf(Category.DRINKING, Category.CINEMA)))
        tagsViewModel = ViewModelProvider(this, tagsViewModelFactory).get(TagsViewModel::class.java) as TagsViewModel<Category>

        setContentView(R.layout.activity_immutable_tags_showcase)

        supportFragmentManager.fragmentFactory = TagDisplayFragmentFactory(Category::class.java)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, TagsDisplayFragment::class.java.name)
            supportFragmentManager.beginTransaction()
                .add(R.id.tag_fragment_immutable, fragment)
                .commit()
        }

        val goButton = findViewById<Button>(R.id.go_to_mutable_tag_showcase)
        goButton.setOnClickListener {
            val intent = Intent(this, TagShowcaseActivity::class.java)
            startActivity(intent)
        }

    }
}