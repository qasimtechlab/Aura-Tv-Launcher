package com.example.data.model

import com.example.R

data class MovieItem(
    val id: String,
    val title: String,
    val tagline: String,
    val synopsis: String,
    val rating: String,
    val year: String,
    val duration: String,
    val quality: String,
    val posterRes: Int,
    val backdropRes: Int,
    val genre: String,
    val targetPackage: String? = null
)

object EntertainmentCatalog {
    val featuredMovies = listOf(
        MovieItem(
            id = "movie_interstellar",
            title = "Beyond The Nebula",
            tagline = "The final frontier lies beyond human comprehension.",
            synopsis = "A team of deep-space pioneers journeys through a gravitational anomaly at the edge of Saturn to find humanity's next sanctuary among the stars.",
            rating = "98% MATCH",
            year = "2026",
            duration = "2h 45m",
            quality = "4K UHD • DOLBY VISION • ATMOS",
            posterRes = R.drawable.poster_interstellar,
            backdropRes = R.drawable.bg_depth_cosmos,
            genre = "Sci-Fi Epic",
            targetPackage = "com.google.android.youtube.tv"
        ),
        MovieItem(
            id = "movie_cyber_odyssey",
            title = "Neon Syndicate: 2099",
            tagline = "In a city of chrome and shadows, memories are currency.",
            synopsis = "In the rain-drenched megacity of New Kyoto, a cybernetic detective unravels a conspiracy that threatens to overwrite the consciousness of millions.",
            rating = "95% MATCH",
            year = "2026",
            duration = "2h 12m",
            quality = "4K UHD • HDR10+ • 60 FPS",
            posterRes = R.drawable.poster_cyber,
            backdropRes = R.drawable.bg_depth_cyber,
            genre = "Cyberpunk Action",
            targetPackage = "com.netflix.ninja"
        ),
        MovieItem(
            id = "movie_tokyo_lights",
            title = "Tokyo Midnight Echoes",
            tagline = "The pulse of the world's greatest metropolis.",
            synopsis = "An atmospheric nocturnal voyage through Tokyo's neon towers, underground car clubs, and tranquil historic shrines under the twilight rain.",
            rating = "94% MATCH",
            year = "2025",
            duration = "1h 54m",
            quality = "4K HDR • SPATIAL AUDIO",
            posterRes = R.drawable.poster_cyber,
            backdropRes = R.drawable.bg_depth_tokyo,
            genre = "Atmospheric Cinema",
            targetPackage = "com.amazon.amazonvideo.livingroom"
        ),
        MovieItem(
            id = "movie_alpine_solitude",
            title = "Above The Clouds",
            tagline = "Extreme alpine expeditions on Earth's highest summits.",
            synopsis = "Witness the raw power of nature as elite mountain climbers conquer the unclimbed north face of the Himalayas during the winter solstice.",
            rating = "99% MATCH",
            year = "2026",
            duration = "1h 48m",
            quality = "8K MASTER • DOLBY AUDIO",
            posterRes = R.drawable.poster_interstellar,
            backdropRes = R.drawable.bg_depth_mountain,
            genre = "Documentary",
            targetPackage = "com.google.android.youtube.tv"
        )
    )
}
