package com.github.palFinderTeam.palfinder.tag

import kotlinx.serialization.Serializable

/**
 * All possible activity tags.
 * Also the tagName should probably be a string resource to facilitate translation.
 */
@Serializable
enum class Category(override val tagName: String) : Tag {
    // We should later populate with meaningful tags
    DRINKING("Drinking"),
    WORKING_OUT("Working out"),
    CINEMA("Cinema"),
    DUMMY_TAG1("Walking/Hiking"),
    DUMMY_TAG2("Bowling"),
    DUMMY_TAG3("Darts"),
    DUMMY_TAG4("Coffee break"),
    DUMMY_TAG5("Chilling"),
    DUMMY_TAG6("Sports"),
    DUMMY_TAG7("Games"),
    DUMMY_TAG8("Video games"),
    DUMMY_TAG9("Party"),
    DUMMY_TAG10("Eating"),
    DUMMY_TAG11("Exposition"),
}