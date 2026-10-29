package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.MovieItem
import com.example.ui.components.AppContextMenuDialog
import com.example.ui.components.AuraTopNavigation
import com.example.ui.components.CinematicHeroBanner
import com.example.ui.components.DepthWallpaperScene
import com.example.ui.components.LauncherScreen
import com.example.ui.components.MoviePosterCard
import com.example.ui.components.PinVaultDialog
import com.example.ui.components.TvAppCard
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.viewmodel.AuraLauncherViewModel
import kotlinx.coroutines.delay

@Composable
fun AuraHomeScreen(
    viewModel: AuraLauncherViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(LauncherScreen.HOME) }

    val activeWallpaper by viewModel.activeWallpaper.collectAsState()
    val clockConfig by viewModel.clockCustomization.collectAsState()
    val focusedApp by viewModel.focusedApp.collectAsState()
    val focusedMovie by viewModel.focusedMovie.collectAsState()
    val shelves by viewModel.shelves.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val showPinDialog by viewModel.showPinDialog.collectAsState()
    val contextMenuApp by viewModel.contextMenuApp.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    // Remote Back handling: close dialogs or return to Home tab
    val hasOpenDialog = showPinDialog || (contextMenuApp != null)
    BackHandler(enabled = hasOpenDialog || currentScreen != LauncherScreen.HOME) {
        if (showPinDialog) {
            viewModel.setPinDialogVisible(false)
        } else if (contextMenuApp != null) {
            viewModel.closeContextMenu()
        } else if (currentScreen != LauncherScreen.HOME) {
            currentScreen = LauncherScreen.HOME
        }
    }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(3000)
            viewModel.clearFeedback()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .testTag("aura_root_container")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Persistent Top TV Navigation Bar
            AuraTopNavigation(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it },
                isVaultUnlocked = isVaultUnlocked,
                onToggleVault = {
                    if (isVaultUnlocked) viewModel.lockVault() else viewModel.setPinDialogVisible(true)
                }
            )

            // 2. Active Screen View
            when (currentScreen) {
                LauncherScreen.HOME -> {
                    HomeContent(
                        viewModel = viewModel,
                        activeWallpaper = activeWallpaper,
                        clockConfig = clockConfig,
                        focusedApp = focusedApp,
                        focusedMovie = focusedMovie,
                        shelves = shelves,
                        onOpenStudio = { currentScreen = LauncherScreen.DEPTH_STUDIO }
                    )
                }

                LauncherScreen.APPS -> {
                    AuraAppsScreen(viewModel = viewModel)
                }

                LauncherScreen.AI_POWER -> {
                    AiPowerChatScreen(viewModel = viewModel)
                }

                LauncherScreen.DEPTH_STUDIO -> {
                    DepthStudioScreen(viewModel = viewModel)
                }

                LauncherScreen.SETTINGS -> {
                    AuraSettingsScreen(
                        viewModel = viewModel,
                        onNavigateToStudio = { currentScreen = LauncherScreen.DEPTH_STUDIO }
                    )
                }
            }
        }

        // Feedback Toast
        AnimatedVisibility(
            visible = feedbackMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            feedbackMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xE61F2633))
                        .border(1.dp, AppleBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = msg,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // PIN Dialog
        PinVaultDialog(
            isOpen = showPinDialog,
            onDismiss = { viewModel.setPinDialogVisible(false) },
            onVerifyPin = { viewModel.verifyPinAndUnlock(it) }
        )

        // Context Menu Dialog
        AppContextMenuDialog(
            app = contextMenuApp,
            onDismiss = { viewModel.closeContextMenu() },
            onLaunch = { viewModel.launchApp(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onHideApp = { viewModel.hideApp(it) },
            onOpenDetails = { viewModel.openAppDetails(it) }
        )
    }
}

@Composable
private fun HomeContent(
    viewModel: AuraLauncherViewModel,
    activeWallpaper: com.example.data.model.DepthWallpaper,
    clockConfig: com.example.data.model.ClockCustomization,
    focusedApp: AppItem?,
    focusedMovie: MovieItem,
    shelves: List<com.example.ui.viewmodel.CategoryShelf>,
    onOpenStudio: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 54.dp)
    ) {
        // TOP REGION: CINEMATIC DEPTH-EFFECT WALLPAPER WITH CLOCK BEHIND FOREGROUND
        item(key = "depth_wallpaper_header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 4.dp)
            ) {
                DepthWallpaperScene(
                    wallpaper = activeWallpaper,
                    clockConfig = clockConfig,
                    heightDp = 340,
                    showBottomGradient = true
                )
            }
        }

        // FEATURED CINEMATIC ENTERTAINMENT HERO
        item(key = "featured_hero_banner") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp, vertical = 8.dp)
            ) {
                CinematicHeroBanner(
                    movie = focusedMovie,
                    onWatch = { viewModel.launchMovieTarget(it) },
                    onDetails = { viewModel.launchMovieTarget(it) }
                )
            }
        }

        // CINEMATIC RECOMMENDATIONS (2:3 VERTICAL POSTERS ROW)
        item(key = "trending_movies_row") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 36.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = AppleBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Featured 4K Cinema & Discoveries",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.featuredMovies, key = { it.id }) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = { viewModel.launchMovieTarget(it) },
                            onFocus = { viewModel.onMovieFocused(it) }
                        )
                    }
                }
            }
        }

        // INSTALLED APPLICATION SHELVES (FAVORITES DOCK, STREAMING, GAMES, TOOLS, ALL)
        items(
            items = shelves,
            key = { shelf -> shelf.category.name }
        ) { shelf ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 36.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (shelf.category == com.example.data.model.AppCategory.FAVORITES) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AppleGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = shelf.title,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = shelf.items,
                        key = { app -> "${shelf.category.name}_${app.packageName}" }
                    ) { app ->
                        TvAppCard(
                            app = app,
                            onLaunch = { viewModel.launchApp(it) },
                            onOptions = { viewModel.openContextMenu(it) },
                            onFocus = { viewModel.onAppFocused(it) }
                        )
                    }
                }
            }
        }
    }
}
