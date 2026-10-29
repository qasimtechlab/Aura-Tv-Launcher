package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClockCustomization
import com.example.data.model.ClockFontStyle
import com.example.data.model.DepthWallpaper
import com.example.ui.components.DepthWallpaperScene
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGold
import com.example.ui.theme.BorderFocused
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import com.example.ui.viewmodel.AuraLauncherViewModel

@Composable
fun DepthStudioScreen(
    viewModel: AuraLauncherViewModel,
    modifier: Modifier = Modifier
) {
    val activeWallpaper by viewModel.activeWallpaper.collectAsState()
    val clockConfig by viewModel.clockCustomization.collectAsState()
    val builtInWallpapers = viewModel.builtInDepthWallpapers

    // Local temporary preview state so user can tweak without saving until "Apply"
    var previewWallpaper by remember(activeWallpaper) { mutableStateOf(activeWallpaper) }
    var previewClock by remember(clockConfig) { mutableStateOf(clockConfig) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 36.dp, vertical = 12.dp)
            .testTag("depth_studio_screen")
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = AppleBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Depth Wallpaper Studio",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "Cinematic 3-Layer Rendering Engine: Background → Oversized Clock → Foreground Cutout",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Apply Button
            var isApplyFocused by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    viewModel.saveDepthStudioConfiguration(previewWallpaper, previewClock)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isApplyFocused) Color.White else AppleBlue,
                    contentColor = if (isApplyFocused) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .onFocusChanged { isApplyFocused = it.isFocused }
                    .focusable()
                    .testTag("studio_apply_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply to Home Screen", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. LIVE RENDERER PREVIEW CANVAS (Shows true Depth effect with Clock behind Foreground Cutout)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, BorderSubtle, RoundedCornerShape(24.dp))
        ) {
            DepthWallpaperScene(
                wallpaper = previewWallpaper,
                clockConfig = previewClock,
                heightDp = 300,
                showBottomGradient = true
            )

            // Live Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC000000))
                    .border(1.dp, AppleBlue, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LIVE 3-LAYER PREVIEW",
                    color = AppleBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. CURATED DEPTH WALLPAPERS SELECTOR
        Text(
            text = "Select Depth Wallpaper",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(builtInWallpapers, key = { it.id }) { theme ->
                val isSelected = theme.id == previewWallpaper.id
                var isFocused by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isFocused) Color.White.copy(alpha = 0.2f) else TvSurface)
                        .border(
                            width = if (isFocused) 3.dp else if (isSelected) 2.dp else 1.dp,
                            color = if (isFocused) BorderFocused else if (isSelected) AppleBlue else BorderSubtle,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .onFocusChanged { isFocused = it.isFocused }
                        .focusable()
                        .clickable { previewWallpaper = theme }
                        .padding(12.dp)
                        .testTag("wallpaper_theme_${theme.id}")
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            DepthWallpaperScene(
                                wallpaper = theme,
                                clockConfig = previewClock.copy(sizeSp = 48, verticalOffsetY = 14),
                                heightDp = 90,
                                showBottomGradient = false
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = theme.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = theme.subtitle,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. CLOCK TYPOGRAPHY & FONT STYLES
        Text(
            text = "Clock Typography Style",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ClockFontStyle.values().forEach { style ->
                val isSelected = previewClock.fontStyle == style
                var isFocused by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFocused) Color.White.copy(alpha = 0.25f) else if (isSelected) AppleBlue.copy(alpha = 0.3f) else Color(0x11FFFFFF))
                        .border(
                            width = if (isFocused) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                            color = if (isFocused) BorderFocused else if (isSelected) AppleBlue else BorderSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .onFocusChanged { isFocused = it.isFocused }
                        .focusable()
                        .clickable { previewClock = previewClock.copy(fontStyle = style) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = style.displayName,
                        color = if (isSelected || isFocused) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. CLOCK COLOR & POSITION CONTROLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Colors Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Clock Accent Color",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val colors = listOf(
                    Pair("White", 0xFFFFFFFF),
                    Pair("Warm White", 0xFFFFF7ED),
                    Pair("Amber Gold", 0xFFFDE047),
                    Pair("Ice Cyan", 0xFF67E8F9),
                    Pair("Neon Purple", 0xFFC084FC)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colors.forEach { (name, hex) ->
                        val isSelected = previewClock.colorHex == hex
                        var isFocused by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (isFocused) 3.dp else if (isSelected) 2.dp else 0.dp,
                                    color = if (isFocused) BorderFocused else AppleBlue,
                                    shape = CircleShape
                                )
                                .onFocusChanged { isFocused = it.isFocused }
                                .focusable()
                                .clickable { previewClock = previewClock.copy(colorHex = hex) }
                                .testTag("color_button_$name")
                        )
                    }
                }
            }

            // Size & Offset Controls Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Clock Size & Vertical Alignment",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Pair("Compact", 72),
                        Pair("Standard", 92),
                        Pair("Huge 4K", 112)
                    ).forEach { (label, size) ->
                        val isSelected = previewClock.sizeSp == size
                        var isFocused by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isFocused) Color.White.copy(alpha = 0.25f) else if (isSelected) AppleBlue.copy(alpha = 0.3f) else Color(0x11FFFFFF))
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) BorderFocused else if (isSelected) AppleBlue else BorderSubtle,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .onFocusChanged { isFocused = it.isFocused }
                                .focusable()
                                .clickable { previewClock = previewClock.copy(sizeSp = size) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected || isFocused) Color.White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. RESTORE DEFAULT BUTTON
        var isResetFocused by remember { mutableStateOf(false) }
        OutlinedButton(
            onClick = {
                viewModel.resetToDefaultWallpaper()
                previewWallpaper = viewModel.builtInDepthWallpapers.first()
                previewClock = ClockCustomization()
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isResetFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                contentColor = TextSecondary
            ),
            modifier = Modifier
                .onFocusChanged { isResetFocused = it.isFocused }
                .focusable()
                .testTag("studio_reset_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset to Default Tokyo Wallpaper", fontSize = 12.sp)
        }
    }
}
