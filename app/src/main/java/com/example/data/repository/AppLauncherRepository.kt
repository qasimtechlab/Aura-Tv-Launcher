package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import com.example.R
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.ClockCustomization
import com.example.data.model.ClockFontStyle
import com.example.data.model.ClockPosition
import com.example.data.model.DepthWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppLauncherRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aura_tv_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FAVORITES = "key_favorites"
        private const val KEY_HIDDEN = "key_hidden"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_ACTIVE_WALLPAPER_ID = "key_active_wallpaper_id"
        private const val KEY_CLOCK_FONT = "key_clock_font"
        private const val KEY_CLOCK_COLOR = "key_clock_color"
        private const val KEY_CLOCK_SIZE = "key_clock_size"
        private const val KEY_CLOCK_OFFSET = "key_clock_offset"
        private const val KEY_CLOCK_DATE = "key_clock_date"
        private const val KEY_CLOCK_24H = "key_clock_24h"
        private const val KEY_DIMMING = "key_dimming"
        private const val KEY_CUSTOM_BG_URI = "key_custom_bg_uri"
        private const val KEY_CUSTOM_FG_URI = "key_custom_fg_uri"
        private const val DEFAULT_PIN = "0000"
    }

    // Built-in Curated Depth Wallpapers with paired Background + Foreground transparent PNG cutouts
    val builtInDepthWallpapers: List<DepthWallpaper> = listOf(
        DepthWallpaper(
            id = "depth_tokyo",
            title = "Neo Tokyo Metropolis",
            subtitle = "Skyline Towers & Spire Depth Effect",
            category = "Metropolis",
            bgDrawableRes = R.drawable.bg_depth_tokyo,
            fgDrawableRes = R.drawable.fg_depth_tokyo,
            defaultClockPosition = ClockPosition.UPPER_CENTER,
            defaultClockOffsetY = 28,
            defaultClockScale = 1.0f,
            defaultDimming = 0.20f
        ),
        DepthWallpaper(
            id = "depth_mountain",
            title = "Alpine Solitude",
            subtitle = "Snow-Capped Summit Over Clock Depth",
            category = "Nature",
            bgDrawableRes = R.drawable.bg_depth_mountain,
            fgDrawableRes = R.drawable.fg_depth_mountain,
            defaultClockPosition = ClockPosition.UPPER_CENTER,
            defaultClockOffsetY = 32,
            defaultClockScale = 1.05f,
            defaultDimming = 0.15f
        ),
        DepthWallpaper(
            id = "depth_cosmos",
            title = "Celestial Earthrise",
            subtitle = "Curved Atmospheric Limb Depth Effect",
            category = "Space",
            bgDrawableRes = R.drawable.bg_depth_cosmos,
            fgDrawableRes = R.drawable.fg_depth_cosmos,
            defaultClockPosition = ClockPosition.UPPER_CENTER,
            defaultClockOffsetY = 36,
            defaultClockScale = 1.0f,
            defaultDimming = 0.18f
        ),
        DepthWallpaper(
            id = "depth_cyber",
            title = "Cyber Monolith",
            subtitle = "Futuristic Spire & Neon Skyline Depth",
            category = "Cyberpunk",
            bgDrawableRes = R.drawable.bg_depth_cyber,
            fgDrawableRes = R.drawable.fg_depth_cyber,
            defaultClockPosition = ClockPosition.UPPER_CENTER,
            defaultClockOffsetY = 26,
            defaultClockScale = 1.0f,
            defaultDimming = 0.22f
        )
    )

    fun getActiveWallpaper(): DepthWallpaper {
        val savedId = prefs.getString(KEY_ACTIVE_WALLPAPER_ID, "depth_tokyo")
        if (savedId == "custom_wallpaper") {
            val bgUri = prefs.getString(KEY_CUSTOM_BG_URI, null)
            val fgUri = prefs.getString(KEY_CUSTOM_FG_URI, null)
            if (bgUri != null) {
                return DepthWallpaper(
                    id = "custom_wallpaper",
                    title = "Custom User Wallpaper",
                    subtitle = if (fgUri != null) "Dual-Layer Custom Depth Studio" else "Single Layer Background",
                    category = "Custom",
                    customBgUri = bgUri,
                    customFgUri = fgUri,
                    isCustom = true
                )
            }
        }
        return builtInDepthWallpapers.find { it.id == savedId } ?: builtInDepthWallpapers.first()
    }

    fun setActiveWallpaper(wallpaper: DepthWallpaper) {
        if (wallpaper.isCustom) {
            prefs.edit()
                .putString(KEY_ACTIVE_WALLPAPER_ID, "custom_wallpaper")
                .putString(KEY_CUSTOM_BG_URI, wallpaper.customBgUri)
                .putString(KEY_CUSTOM_FG_URI, wallpaper.customFgUri)
                .apply()
        } else {
            prefs.edit()
                .putString(KEY_ACTIVE_WALLPAPER_ID, wallpaper.id)
                .apply()
        }
    }

    fun getClockCustomization(): ClockCustomization {
        val fontName = prefs.getString(KEY_CLOCK_FONT, ClockFontStyle.MODERN_BOLD.name) ?: ClockFontStyle.MODERN_BOLD.name
        val fontStyle = try { ClockFontStyle.valueOf(fontName) } catch (_: Exception) { ClockFontStyle.MODERN_BOLD }
        val colorHex = prefs.getLong(KEY_CLOCK_COLOR, 0xFFFFFFFF)
        val sizeSp = prefs.getInt(KEY_CLOCK_SIZE, 96)
        val offset = prefs.getInt(KEY_CLOCK_OFFSET, 36)
        val showDate = prefs.getBoolean(KEY_CLOCK_DATE, true)
        val is24h = prefs.getBoolean(KEY_CLOCK_24H, false)
        val dimming = prefs.getFloat(KEY_DIMMING, 0.20f)

        return ClockCustomization(
            fontStyle = fontStyle,
            colorHex = colorHex,
            sizeSp = sizeSp,
            verticalOffsetY = offset,
            showDate = showDate,
            is24Hour = is24h,
            dimmingFactor = dimming
        )
    }

    fun saveClockCustomization(config: ClockCustomization) {
        prefs.edit()
            .putString(KEY_CLOCK_FONT, config.fontStyle.name)
            .putLong(KEY_CLOCK_COLOR, config.colorHex)
            .putInt(KEY_CLOCK_SIZE, config.sizeSp)
            .putInt(KEY_CLOCK_OFFSET, config.verticalOffsetY)
            .putBoolean(KEY_CLOCK_DATE, config.showDate)
            .putBoolean(KEY_CLOCK_24H, config.is24Hour)
            .putFloat(KEY_DIMMING, config.dimmingFactor)
            .apply()
    }

    fun setCustomWallpaper(bgUri: String, fgUri: String? = null) {
        val custom = DepthWallpaper(
            id = "custom_wallpaper",
            title = "Custom Wallpaper",
            subtitle = if (fgUri != null) "Dual-Layer Custom Depth Studio" else "Single Layer Image",
            category = "Custom",
            customBgUri = bgUri,
            customFgUri = fgUri,
            isCustom = true
        )
        setActiveWallpaper(custom)
    }

    fun resetToDefaultWallpaper() {
        prefs.edit()
            .putString(KEY_ACTIVE_WALLPAPER_ID, "depth_tokyo")
            .remove(KEY_CUSTOM_BG_URI)
            .remove(KEY_CUSTOM_FG_URI)
            .apply()
    }

    fun getFavoritePackageNames(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun setFavoritePackageNames(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, packages).apply()
    }

    fun getHiddenPackageNames(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()
    }

    fun setHiddenPackageNames(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN, packages).apply()
    }

    fun verifyPin(enteredPin: String): Boolean {
        val savedPin = prefs.getString(KEY_PIN_HASH, DEFAULT_PIN) ?: DEFAULT_PIN
        return enteredPin == savedPin
    }

    fun setPin(newPin: String) {
        prefs.edit().putString(KEY_PIN_HASH, newPin).apply()
    }

    suspend fun getInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val favorites = getFavoritePackageNames()
        val hidden = getHiddenPackageNames()

        val leanbackIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val mobileIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val leanbackApps: List<ResolveInfo> = try {
            pm.queryIntentActivities(leanbackIntent, 0)
        } catch (_: Exception) {
            emptyList()
        }

        val mobileApps: List<ResolveInfo> = try {
            pm.queryIntentActivities(mobileIntent, 0)
        } catch (_: Exception) {
            emptyList()
        }

        val allResolveInfos = LinkedHashMap<String, Pair<ResolveInfo, Boolean>>()
        for (info in leanbackApps) {
            val pkg = info.activityInfo.packageName
            if (pkg != context.packageName) {
                allResolveInfos[pkg] = Pair(info, true)
            }
        }
        for (info in mobileApps) {
            val pkg = info.activityInfo.packageName
            if (pkg != context.packageName && !allResolveInfos.containsKey(pkg)) {
                allResolveInfos[pkg] = Pair(info, false)
            }
        }

        val resultList = mutableListOf<AppItem>()

        for ((pkg, pair) in allResolveInfos) {
            val info = pair.first
            val isTv = pair.second
            val label = info.loadLabel(pm).toString()
            val icon = try {
                info.loadIcon(pm)
            } catch (_: Exception) {
                null
            }
            val category = categorizeApp(pkg, label)
            val isFav = favorites.contains(pkg)
            val isHid = hidden.contains(pkg)

            resultList.add(
                AppItem(
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    label = label,
                    icon = icon,
                    isTvApp = isTv,
                    isFavorite = isFav,
                    isHidden = isHid,
                    category = category,
                    backdropRes = getCuratedBackdrop(category),
                    tagLine = getCuratedTagLine(label, category)
                )
            )
        }

        if (resultList.size < 4) {
            val showcaseApps = getShowcaseApps(favorites, hidden)
            for (showcase in showcaseApps) {
                if (resultList.none { it.packageName == showcase.packageName }) {
                    resultList.add(showcase)
                }
            }
        }

        resultList.sortedWith(compareBy({ !it.isFavorite }, { it.label.lowercase() }))
    }

    private fun categorizeApp(packageName: String, label: String): AppCategory {
        val lowerPkg = packageName.lowercase()
        val lowerLabel = label.lowercase()

        return when {
            lowerPkg.contains("netflix") || lowerPkg.contains("youtube") || lowerPkg.contains("primevideo")
                    || lowerPkg.contains("hulu") || lowerPkg.contains("disney") || lowerPkg.contains("plex")
                    || lowerPkg.contains("kodi") || lowerPkg.contains("vlc") || lowerPkg.contains("tv")
                    || lowerLabel.contains("video") || lowerLabel.contains("cinema") || lowerLabel.contains("stream") ->
                AppCategory.STREAMING

            lowerPkg.contains("game") || lowerPkg.contains("arcade") || lowerPkg.contains("steam")
                    || lowerPkg.contains("retroarch") || lowerLabel.contains("play") ->
                AppCategory.GAMES

            lowerPkg.contains("music") || lowerPkg.contains("spotify") || lowerPkg.contains("podcast")
                    || lowerPkg.contains("audio") || lowerPkg.contains("radio") || lowerPkg.contains("sound") ->
                AppCategory.MUSIC

            lowerPkg.contains("setting") || lowerPkg.contains("file") || lowerPkg.contains("browser")
                    || lowerPkg.contains("cleaner") || lowerPkg.contains("speedtest") || lowerPkg.contains("store") ->
                AppCategory.TOOLS

            else -> AppCategory.ALL
        }
    }

    private fun getCuratedBackdrop(category: AppCategory): Int {
        return when (category) {
            AppCategory.STREAMING -> R.drawable.bg_depth_tokyo
            AppCategory.GAMES -> R.drawable.bg_depth_cyber
            AppCategory.MUSIC -> R.drawable.bg_depth_cosmos
            else -> R.drawable.bg_depth_mountain
        }
    }

    private fun getCuratedTagLine(label: String, category: AppCategory): String {
        return when (category) {
            AppCategory.STREAMING -> "Stream trending 4K HDR releases, live broadcasts, and blockbusters."
            AppCategory.GAMES -> "Play immersive console-grade TV gaming and high-framerate arcades."
            AppCategory.MUSIC -> "Stream millions of lossless tracks and spatial audio podcasts."
            AppCategory.TOOLS -> "Optimize system hardware, display calibration, and peripherals."
            else -> "Fast leanback experience optimized for your Android TV television."
        }
    }

    private fun getShowcaseApps(favorites: Set<String>, hidden: Set<String>): List<AppItem> {
        return listOf(
            AppItem(
                packageName = "com.google.android.youtube.tv",
                activityName = "com.google.android.apps.youtube.tv.activity.ShellActivity",
                label = "YouTube TV",
                isTvApp = true,
                isFavorite = favorites.contains("com.google.android.youtube.tv") || favorites.isEmpty(),
                isHidden = hidden.contains("com.google.android.youtube.tv"),
                category = AppCategory.STREAMING,
                backdropRes = R.drawable.bg_depth_tokyo,
                tagLine = "Explore millions of 4K creators, live global broadcasts, and trending clips.",
                badges = listOf("4K HDR", "DOLBY AUDIO", "POPULAR")
            ),
            AppItem(
                packageName = "com.netflix.ninja",
                activityName = "com.netflix.ninja.MainActivity",
                label = "Netflix",
                isTvApp = true,
                isFavorite = favorites.contains("com.netflix.ninja") || favorites.isEmpty(),
                isHidden = hidden.contains("com.netflix.ninja"),
                category = AppCategory.STREAMING,
                backdropRes = R.drawable.bg_depth_cyber,
                tagLine = "Watch award-winning original series, blockbuster films, and documentaries.",
                badges = listOf("DOLBY VISION", "ATMOS", "ORIGINAL")
            ),
            AppItem(
                packageName = "com.amazon.amazonvideo.livingroom",
                activityName = "com.amazon.ignition.IgnitionActivity",
                label = "Prime Video",
                isTvApp = true,
                isFavorite = favorites.contains("com.amazon.amazonvideo.livingroom") || favorites.isEmpty(),
                isHidden = hidden.contains("com.amazon.amazonvideo.livingroom"),
                category = AppCategory.STREAMING,
                backdropRes = R.drawable.bg_depth_mountain,
                tagLine = "Stream critically acclaimed Prime Originals, sports, and box office movies.",
                badges = listOf("4K UHD", "HDR10+", "FEATURED")
            ),
            AppItem(
                packageName = "com.spotify.tv.android",
                activityName = "com.spotify.tv.android.SpotifyTVActivity",
                label = "Spotify TV",
                isTvApp = true,
                isFavorite = favorites.contains("com.spotify.tv.android"),
                isHidden = hidden.contains("com.spotify.tv.android"),
                category = AppCategory.MUSIC,
                backdropRes = R.drawable.bg_depth_cosmos,
                tagLine = "Millions of songs and background tunes curated directly for big screens.",
                badges = listOf("LOSSLESS", "HI-FI", "MUSIC")
            ),
            AppItem(
                packageName = "com.retroarch",
                activityName = "com.retroarch.browser.retroactivity.RetroActivityFuture",
                label = "RetroArcade",
                isTvApp = true,
                isFavorite = favorites.contains("com.retroarch"),
                isHidden = hidden.contains("com.retroarch"),
                category = AppCategory.GAMES,
                backdropRes = R.drawable.bg_depth_cyber,
                tagLine = "Next-generation multi-system classic gaming with gamepad controller support.",
                badges = listOf("GAMEPAD READY", "60 FPS", "ARCADE")
            ),
            AppItem(
                packageName = "com.android.tv.settings",
                activityName = "com.android.tv.settings.MainSettings",
                label = "TV Settings",
                isTvApp = true,
                isFavorite = favorites.contains("com.android.tv.settings"),
                isHidden = hidden.contains("com.android.tv.settings"),
                category = AppCategory.TOOLS,
                backdropRes = R.drawable.bg_depth_mountain,
                tagLine = "Configure network, HDMI-CEC, audio passthrough, and display calibration.",
                badges = listOf("SYSTEM", "SETTINGS", "TOOLS")
            )
        )
    }

    fun launchApp(packageName: String): Boolean {
        val pm = context.packageManager
        val leanbackIntent = pm.getLeanbackLaunchIntentForPackage(packageName)
        if (leanbackIntent != null) {
            leanbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            try {
                context.startActivity(leanbackIntent)
                return true
            } catch (_: Exception) { }
        }

        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            try {
                context.startActivity(launchIntent)
                return true
            } catch (_: Exception) { }
        }

        val queryIntent = Intent(Intent.ACTION_MAIN)
        queryIntent.setPackage(packageName)
        queryIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        queryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            context.startActivity(queryIntent)
            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    fun openSystemSettings() {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    fun openHomeSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openSystemSettings()
        }
    }
}
