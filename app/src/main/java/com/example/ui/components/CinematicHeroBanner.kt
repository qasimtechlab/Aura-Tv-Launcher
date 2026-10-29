package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.MovieItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderFocused
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground

@Composable
fun CinematicHeroBanner(
    movie: MovieItem,
    onWatch: (MovieItem) -> Unit,
    onDetails: (MovieItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(TvBackground)
            .testTag("cinematic_hero_banner")
    ) {
        AnimatedContent(
            targetState = movie,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "hero_banner_transition"
        ) { currentMovie ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Backdrop Image
                Image(
                    painter = painterResource(id = currentMovie.backdropRes),
                    contentDescription = currentMovie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Vignette Gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    TvBackground.copy(alpha = 0.96f),
                                    TvBackground.copy(alpha = 0.82f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 1300f
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
                                startY = 80f
                            )
                        )
                )

                // Content Details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Category & Quality Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BadgePill(text = currentMovie.genre.uppercase(), isAccent = true)
                        BadgePill(text = currentMovie.rating, isAccent = false)
                        BadgePill(text = currentMovie.year, isAccent = false)
                        BadgePill(text = currentMovie.duration, isAccent = false)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentMovie.title,
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentMovie.synopsis,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.60f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var isPlayFocused by remember { mutableStateOf(false) }
                        Button(
                            onClick = { onWatch(currentMovie) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlayFocused) Color.White else AppleBlue,
                                contentColor = if (isPlayFocused) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("hero_watch_button")
                                .onFocusChanged { isPlayFocused = it.isFocused }
                                .border(
                                    width = if (isPlayFocused) 3.dp else 0.dp,
                                    color = BorderFocused,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .height(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Watch Feature",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        var isInfoFocused by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { onDetails(currentMovie) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isInfoFocused) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                contentColor = TextPrimary
                            ),
                            modifier = Modifier
                                .testTag("hero_info_button")
                                .onFocusChanged { isInfoFocused = it.isFocused }
                                .border(
                                    width = if (isInfoFocused) 2.dp else 1.dp,
                                    color = if (isInfoFocused) BorderFocused else Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .height(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "More Details",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
