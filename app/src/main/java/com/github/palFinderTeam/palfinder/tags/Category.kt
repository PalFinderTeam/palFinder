package com.github.palFinderTeam.palfinder.tags

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
    WALKING_HIKING("Walking/Hiking"),
    BOWLING("Bowling"),
    DARTS("Darts"),
    COFFEE("Coffee break"),
    CHILLING("Chilling"),
    SPORTS("Sports"),
    GAMES("Games"),
    VIDEO_GAMES("Video games"),
    PARTY("Party"),
    EATING("Eating"),
    EXPOSITION("Exposition"),
}