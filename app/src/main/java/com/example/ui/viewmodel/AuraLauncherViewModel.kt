package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.ClockCustomization
import com.example.data.model.DepthWallpaper
import com.example.data.model.EntertainmentCatalog
import com.example.data.model.MovieItem
import com.example.data.repository.AppLauncherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryShelf(
    val category: AppCategory,
    val title: String,
    val items: List<AppItem>
)

class AuraLauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppLauncherRepository(application.applicationContext)
    private val geminiRepository = com.example.data.gemini.GeminiChatRepository(application.applicationContext)

    val chatMessages = geminiRepository.messages
    val isChatLoading = geminiRepository.isLoading
    val isHistorySavingEnabled = geminiRepository.isHistoryEnabled

    private val _allApps = MutableStateFlow<List<AppItem>>(emptyList())
    val allApps: StateFlow<List<AppItem>> = _allApps.asStateFlow()

    private val _shelves = MutableStateFlow<List<CategoryShelf>>(emptyList())
    val shelves: StateFlow<List<CategoryShelf>> = _shelves.asStateFlow()

    private val _focusedApp = MutableStateFlow<AppItem?>(null)
    val focusedApp: StateFlow<AppItem?> = _focusedApp.asStateFlow()

    private val _activeWallpaper = MutableStateFlow(repository.getActiveWallpaper())
    val activeWallpaper: StateFlow<DepthWallpaper> = _activeWallpaper.asStateFlow()

    val builtInDepthWallpapers: List<DepthWallpaper> = repository.builtInDepthWallpapers

    private val _clockCustomization = MutableStateFlow(repository.getClockCustomization())
    val clockCustomization: StateFlow<ClockCustomization> = _clockCustomization.asStateFlow()

    val featuredMovies: List<MovieItem> = EntertainmentCatalog.featuredMovies

    private val _focusedMovie = MutableStateFlow(featuredMovies.first())
    val focusedMovie: StateFlow<MovieItem> = _focusedMovie.asStateFlow()

    // Hidden apps & Vault
    private val _hiddenPackages = MutableStateFlow(repository.getHiddenPackageNames())
    val hiddenPackages: StateFlow<Set<String>> = _hiddenPackages.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    // Dialog states
    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _contextMenuApp = MutableStateFlow<AppItem?>(null)
    val contextMenuApp: StateFlow<AppItem?> = _contextMenuApp.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            val apps = repository.getInstalledApps()
            _allApps.value = apps
            rebuildShelves(apps, _hiddenPackages.value, _isVaultUnlocked.value)
            if (_focusedApp.value == null && apps.isNotEmpty()) {
                _focusedApp.value = apps.first()
            }
        }
    }

    private fun rebuildShelves(apps: List<AppItem>, hidden: Set<String>, vaultUnlocked: Boolean) {
        val visibleApps = if (vaultUnlocked) {
            apps
        } else {
            apps.filter { !hidden.contains(it.packageName) }
        }

        val shelfList = mutableListOf<CategoryShelf>()

        // 1. Favorites Shelf (The Top Shelf Dock)
        val favorites = visibleApps.filter { it.isFavorite }
        if (favorites.isNotEmpty()) {
            shelfList.add(CategoryShelf(AppCategory.FAVORITES, "Favorites Dock", favorites))
        }

        // 2. Streaming Shelf
        val streaming = visibleApps.filter { it.category == AppCategory.STREAMING }
        if (streaming.isNotEmpty()) {
            shelfList.add(CategoryShelf(AppCategory.STREAMING, "Movies & Streaming", streaming))
        }

        // 3. Games Shelf
        val games = visibleApps.filter { it.category == AppCategory.GAMES }
        if (games.isNotEmpty()) {
            shelfList.add(CategoryShelf(AppCategory.GAMES, "Games & Arcade", games))
        }

        // 4. Music Shelf
        val music = visibleApps.filter { it.category == AppCategory.MUSIC }
        if (music.isNotEmpty()) {
            shelfList.add(CategoryShelf(AppCategory.MUSIC, "Music & Podcasts", music))
        }

        // 5. Tools & Utilities Shelf
        val tools = visibleApps.filter { it.category == AppCategory.TOOLS }
        if (tools.isNotEmpty()) {
            shelfList.add(CategoryShelf(AppCategory.TOOLS, "Tools & Utilities", tools))
        }

        // 6. All Applications
        shelfList.add(CategoryShelf(AppCategory.ALL, "All Applications", visibleApps))

        _shelves.value = shelfList
    }

    fun onAppFocused(app: AppItem) {
        _focusedApp.value = app
    }

    fun onMovieFocused(movie: MovieItem) {
        _focusedMovie.value = movie
    }

    fun launchApp(app: AppItem) {
        val success = repository.launchApp(app.packageName)
        if (!success) {
            _feedbackMessage.value = "Unable to launch ${app.label}"
        }
    }

    fun launchMovieTarget(movie: MovieItem) {
        val pkg = movie.targetPackage ?: "com.google.android.youtube.tv"
        val success = repository.launchApp(pkg)
        if (!success) {
            _feedbackMessage.value = "Launching ${movie.title} preview..."
        }
    }

    fun toggleFavorite(app: AppItem) {
        val currentFavs = repository.getFavoritePackageNames().toMutableSet()
        if (currentFavs.contains(app.packageName)) {
            currentFavs.remove(app.packageName)
            _feedbackMessage.value = "${app.label} removed from Favorites"
        } else {
            currentFavs.add(app.packageName)
            _feedbackMessage.value = "${app.label} added to Favorites"
        }
        repository.setFavoritePackageNames(currentFavs)
        loadApps()
    }

    fun hideApp(app: AppItem) {
        val currentHidden = repository.getHiddenPackageNames().toMutableSet()
        currentHidden.add(app.packageName)
        repository.setHiddenPackageNames(currentHidden)
        _hiddenPackages.value = currentHidden
        _feedbackMessage.value = "${app.label} moved to Protected Vault"
        rebuildShelves(_allApps.value, currentHidden, _isVaultUnlocked.value)
    }

    fun unhideApp(app: AppItem) {
        val currentHidden = repository.getHiddenPackageNames().toMutableSet()
        currentHidden.remove(app.packageName)
        repository.setHiddenPackageNames(currentHidden)
        _hiddenPackages.value = currentHidden
        _feedbackMessage.value = "${app.label} restored to Launcher"
        rebuildShelves(_allApps.value, currentHidden, _isVaultUnlocked.value)
    }

    fun saveDepthStudioConfiguration(wallpaper: DepthWallpaper, clock: ClockCustomization) {
        repository.setActiveWallpaper(wallpaper)
        repository.saveClockCustomization(clock)
        _activeWallpaper.value = wallpaper
        _clockCustomization.value = clock
        _feedbackMessage.value = "Applied ${wallpaper.title} with Depth Effect!"
    }

    fun resetToDefaultWallpaper() {
        repository.resetToDefaultWallpaper()
        val defaultWp = repository.getActiveWallpaper()
        val defaultClock = ClockCustomization()
        repository.saveClockCustomization(defaultClock)
        _activeWallpaper.value = defaultWp
        _clockCustomization.value = defaultClock
        _feedbackMessage.value = "Default Neo Tokyo depth wallpaper restored"
    }

    fun toggle24HourFormat(enable: Boolean) {
        val updated = _clockCustomization.value.copy(is24Hour = enable)
        _clockCustomization.value = updated
        repository.saveClockCustomization(updated)
    }

    fun toggleDateVisibility(visible: Boolean) {
        val updated = _clockCustomization.value.copy(showDate = visible)
        _clockCustomization.value = updated
        repository.saveClockCustomization(updated)
    }

    fun openAppDetails(app: AppItem) {
        repository.openAppDetails(app.packageName)
    }

    fun openSystemSettings() {
        repository.openSystemSettings()
    }

    fun openHomeSettings() {
        repository.openHomeSettings()
    }

    fun openContextMenu(app: AppItem) {
        _contextMenuApp.value = app
    }

    fun closeContextMenu() {
        _contextMenuApp.value = null
    }

    fun setPinDialogVisible(visible: Boolean) {
        _showPinDialog.value = visible
    }

    fun verifyPinAndUnlock(enteredPin: String): Boolean {
        val isValid = repository.verifyPin(enteredPin)
        if (isValid) {
            _isVaultUnlocked.value = true
            _showPinDialog.value = false
            rebuildShelves(_allApps.value, _hiddenPackages.value, true)
            _feedbackMessage.value = "Protected apps unlocked"
        } else {
            _feedbackMessage.value = "Incorrect PIN (Default: 0000)"
        }
        return isValid
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        rebuildShelves(_allApps.value, _hiddenPackages.value, false)
        _feedbackMessage.value = "Protected apps locked"
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun sendChatMessage(text: String) {
        viewModelScope.launch {
            val ok = geminiRepository.sendMessage(text)
            if (!ok) {
                // Friendly status
            }
        }
    }

    fun startNewChat() {
        geminiRepository.startNewChat()
        _feedbackMessage.value = "Started a new conversation"
    }

    fun clearChatHistory() {
        geminiRepository.clearHistory()
        _feedbackMessage.value = "Chat history deleted"
    }

    fun setChatHistorySaving(enabled: Boolean) {
        geminiRepository.setHistoryEnabled(enabled)
        _feedbackMessage.value = if (enabled) "History saving enabled" else "History saving disabled"
    }
}
