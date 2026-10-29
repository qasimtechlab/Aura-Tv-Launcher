package com.example.data.model

import android.graphics.drawable.Drawable

/**
 * Represents an installed Android TV / Mobile application on the device.
 */
data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable? = null,
    val isTvApp: Boolean = true,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val category: AppCategory = AppCategory.ALL,
    val backdropRes: Int? = null,
    val tagLine: String = "High Definition TV Entertainment",
    val badges: List<String> = listOf("4K UHD", "SURROUND SOUND")
)

enum class AppCategory(val title: String) {
    FAVORITES("Top Favorites"),
    STREAMING("Movies & Streaming"),
    GAMES("Games & Arcade"),
    MUSIC("Music & Podcasts"),
    TOOLS("Tools & System"),
    ALL("All Applications")
}
