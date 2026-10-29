package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.ui.components.AppContextMenuDialog
import com.example.ui.components.TvAppCard
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderFocused
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import com.example.ui.viewmodel.AuraLauncherViewModel

@Composable
fun AuraAppsScreen(
    viewModel: AuraLauncherViewModel,
    modifier: Modifier = Modifier
) {
    val allApps by viewModel.allApps.collectAsState()
    val hiddenPackages by viewModel.hiddenPackages.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val contextMenuApp by viewModel.contextMenuApp.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }

    // Filtered apps based on search, category and hidden vault status
    val visibleApps = remember(allApps, hiddenPackages, isVaultUnlocked, searchQuery, selectedCategory) {
        allApps.filter { app ->
            val isNotHidden = isVaultUnlocked || !hiddenPackages.contains(app.packageName)
            val matchesSearch = searchQuery.isBlank() || app.label.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || selectedCategory == AppCategory.ALL || app.category == selectedCategory
            isNotHidden && matchesSearch && matchesCategory
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(horizontal = 36.dp, vertical = 12.dp)
            .testTag("aura_apps_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Live Search Box & Category Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Installed Applications",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${visibleApps.size} apps available on TV",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Search Bar
                var isSearchFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSearchFocused) Color.White.copy(alpha = 0.22f) else TvSurface)
                        .border(
                            width = if (isSearchFocused) 2.dp else 1.dp,
                            color = if (isSearchFocused) BorderFocused else BorderSubtle,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isSearchFocused) Color.White else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(AppleBlue),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { isSearchFocused = it.isFocused }
                            .testTag("app_search_field"),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search apps...",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val categories = listOf(
                    Pair("All", null),
                    Pair("Streaming", AppCategory.STREAMING),
                    Pair("Games", AppCategory.GAMES),
                    Pair("Music", AppCategory.MUSIC),
                    Pair("Tools", AppCategory.TOOLS)
                )

                items(categories) { (label, cat) ->
                    val isSelected = selectedCategory == cat
                    var isFocused by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFocused) Color.White.copy(alpha = 0.25f) else if (isSelected) AppleBlue else Color(0x18FFFFFF))
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) BorderFocused else BorderSubtle,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .onFocusChanged { isFocused = it.isFocused }
                            .focusable()
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("filter_chip_$label")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected || isFocused) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Responsive 6-column Application Grid
            if (visibleApps.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    contentPadding = PaddingValues(bottom = 36.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleApps, key = { it.packageName }) { app ->
                        TvAppCard(
                            app = app,
                            onLaunch = { viewModel.launchApp(it) },
                            onOptions = { viewModel.openContextMenu(it) },
                            onFocus = { viewModel.onAppFocused(it) }
                        )
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No applications found",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try adjusting your search query or filter category",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

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
