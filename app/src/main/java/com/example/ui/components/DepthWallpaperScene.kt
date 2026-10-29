package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.data.model.ClockCustomization
import com.example.data.model.ClockFontStyle
import com.example.data.model.DepthWallpaper
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 3-Layer Depth-Effect Wallpaper Rendering Engine.
 * Exact Layering Order:
 * LAYER 1: 16:9 Cinematic Background Image + Atmospheric Dimming
 * LAYER 2: Large Digital Clock + Date (Positioned BEHIND foreground subject)
 * LAYER 3: Transparent Foreground Subject Cutout (Alpha channel overlapping the clock digits)
 * LAYER 4: Bottom Content Transition Gradient
 */
@Composable
fun DepthWallpaperScene(
    wallpaper: DepthWallpaper,
    clockConfig: ClockCustomization,
    modifier: Modifier = Modifier,
    heightDp: Int = 360,
    showBottomGradient: Boolean = true
) {
    val context = LocalContext.current
    var liveTime by remember(clockConfig.is24Hour) {
        mutableStateOf(getFormattedTime(clockConfig.is24Hour))
    }
    var liveDate by remember {
        mutableStateOf(getFormattedDate())
    }

    LaunchedEffect(clockConfig.is24Hour) {
        while (true) {
            liveTime = getFormattedTime(clockConfig.is24Hour)
            liveDate = getFormattedDate()
            delay(15000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(TvBackground)
            .testTag("depth_wallpaper_scene")
    ) {
        // ==========================================
        // LAYER 1: BACKGROUND WALLPAPER IMAGE
        // ==========================================
        if (wallpaper.bgDrawableRes != null) {
            Image(
                painter = painterResource(id = wallpaper.bgDrawableRes),
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("layer1_bg_drawable")
            )
        } else if (wallpaper.customBgUri != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(Uri.parse(wallpaper.customBgUri))
                        .crossfade(true)
                        .build()
                ),
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("layer1_bg_custom")
            )
        }

        // Atmospheric Dimming Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = clockConfig.dimmingFactor))
        )

        // ==========================================
        // LAYER 2: LARGE DIGITAL CLOCK
        // Positioned BEHIND the foreground subject
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = clockConfig.verticalOffsetY.dp)
                .testTag("layer2_clock_container"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Typography selection
                val (fontFamily, fontWeight, fontStyle, letterSpacing) = when (clockConfig.fontStyle) {
                    ClockFontStyle.MODERN_BOLD -> Quadruple(FontFamily.SansSerif, FontWeight.Black, FontStyle.Normal, (-1.5).sp)
                    ClockFontStyle.ELEGANT_THIN -> Quadruple(FontFamily.SansSerif, FontWeight.ExtraLight, FontStyle.Normal, 4.sp)
                    ClockFontStyle.ROUNDED -> Quadruple(FontFamily.Default, FontWeight.Bold, FontStyle.Normal, 1.sp)
                    ClockFontStyle.MONO_TECH -> Quadruple(FontFamily.Monospace, FontWeight.Bold, FontStyle.Normal, (-2).sp)
                    ClockFontStyle.SERIF_CINEMA -> Quadruple(FontFamily.Serif, FontWeight.Normal, FontStyle.Italic, 2.sp)
                }

                // Oversized Digital Clock Numbers
                Text(
                    text = liveTime,
                    color = Color(clockConfig.colorHex),
                    fontSize = clockConfig.sizeSp.sp,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    fontStyle = fontStyle,
                    letterSpacing = letterSpacing,
                    modifier = Modifier.shadow(
                        elevation = 8.dp,
                        spotColor = Color.Black.copy(alpha = 0.7f)
                    )
                )

                // Optional Date Badge
                if (clockConfig.showDate) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x55000000))
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = liveDate.uppercase(),
                            color = TextPrimary.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // LAYER 3: TRANSPARENT FOREGROUND SUBJECT
        // Positioned OVER the clock!
        // ==========================================
        if (wallpaper.fgDrawableRes != null) {
            Image(
                painter = painterResource(id = wallpaper.fgDrawableRes),
                contentDescription = "${wallpaper.title} Foreground Subject",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("layer3_fg_cutout")
            )
        } else if (wallpaper.customFgUri != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(Uri.parse(wallpaper.customFgUri))
                        .crossfade(true)
                        .build()
                ),
                contentDescription = "${wallpaper.title} Custom Foreground",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("layer3_fg_custom_cutout")
            )
        }

        // ==========================================
        // LAYER 4: BOTTOM CINEMATIC GRADIENT VIGNETTE
        // Seamless transition into launcher content
        // ==========================================
        if (showBottomGradient) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                TvBackground.copy(alpha = 0.70f),
                                TvBackground
                            ),
                            startY = (heightDp * 0.45f),
                            endY = (heightDp.toFloat() * 2.8f)
                        )
                    )
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun getFormattedTime(is24Hour: Boolean): String {
    val pattern = if (is24Hour) "HH:mm" else "h:mm"
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date())
}

private fun getFormattedDate(): String {
    val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    return sdf.format(Date())
}
