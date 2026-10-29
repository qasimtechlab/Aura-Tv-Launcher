package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderFocused
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground

@Composable
fun TopShelfHero(
    app: AppItem?,
    onLaunch: (AppItem) -> Unit,
    onOpenOptions: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x33000000))
    ) {
        if (app != null) {
            AnimatedContent(
                targetState = app,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "top_shelf_animation"
            ) { currentApp ->
                Box(modifier = Modifier.fillMaxSize()) {
                    // Backdrop Graphic
                    if (currentApp.backdropRes != null) {
                        Image(
                            painter = painterResource(id = currentApp.backdropRes),
                            contentDescription = currentApp.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Cinematic Gradient Vignette
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        TvBackground.copy(alpha = 0.95f),
                                        TvBackground.copy(alpha = 0.75f),
                                        Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = 1400f
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        TvBackground.copy(alpha = 0.85f)
                                    ),
                                    startY = 100f
                                )
                            )
                    )

                    // Hero Content Details
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 36.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Badges Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BadgePill(text = currentApp.category.title.uppercase(), isAccent = true)
                            currentApp.badges.forEach { badge ->
                                BadgePill(text = badge, isAccent = false)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // App Label / Title
                        Text(
                            text = currentApp.label,
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Tagline / Synopsis
                        Text(
                            text = currentApp.tagLine,
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(0.65f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Actions Row (Accessible via D-Pad)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var isPlayFocused by remember { mutableStateOf(false) }
                            Button(
                                onClick = { onLaunch(currentApp) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPlayFocused) Color.White else AppleBlue,
                                    contentColor = if (isPlayFocused) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .testTag("top_shelf_play_button")
                                    .onFocusChanged { isPlayFocused = it.isFocused }
                                    .border(
                                        width = if (isPlayFocused) 3.dp else 0.dp,
                                        color = BorderFocused,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Open App",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            var isOptionsFocused by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { onOpenOptions(currentApp) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isOptionsFocused) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f),
                                    contentColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .testTag("top_shelf_options_button")
                                    .onFocusChanged { isOptionsFocused = it.isFocused }
                                    .border(
                                        width = if (isOptionsFocused) 2.dp else 1.dp,
                                        color = if (isOptionsFocused) BorderFocused else Color(0x44FFFFFF),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Quick Options",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgePill(text: String, isAccent: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isAccent) AppleBlue.copy(alpha = 0.25f) else Color(0x33FFFFFF)
            )
            .border(
                width = 1.dp,
                color = if (isAccent) AppleBlue.copy(alpha = 0.6f) else Color(0x44FFFFFF),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = if (isAccent) AppleBlue else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}
