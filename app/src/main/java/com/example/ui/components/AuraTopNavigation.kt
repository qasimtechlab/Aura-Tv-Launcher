package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderFocused
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class LauncherScreen(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    APPS("All Apps", Icons.Default.Apps),
    AI_POWER("AI Power", Icons.Default.AutoAwesome),
    DEPTH_STUDIO("Depth Studio", Icons.Default.Image),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun AuraTopNavigation(
    currentScreen: LauncherScreen,
    onScreenSelected: (LauncherScreen) -> Unit,
    isVaultUnlocked: Boolean,
    onToggleVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Aura TV Monogram
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33FFFFFF))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "AURA TV",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.6.sp
                )
            }
        }

        // Center: Primary Navigation Tabs (Apple TV pill style)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x33000000))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LauncherScreen.values().forEach { screen ->
                NavTabButton(
                    screen = screen,
                    isSelected = currentScreen == screen,
                    onClick = { onScreenSelected(screen) }
                )
            }
        }

        // Right: Vault Toggle Status
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isVaultUnlocked) AppleBlue.copy(alpha = 0.25f) else Color(0x22FFFFFF))
                .border(
                    width = 1.dp,
                    color = if (isVaultUnlocked) AppleBlue else BorderSubtle,
                    shape = CircleShape
                )
                .clickable(onClick = onToggleVault)
                .testTag("nav_vault_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = "Vault",
                tint = if (isVaultUnlocked) AppleBlue else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun NavTabButton(
    screen: LauncherScreen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val bg = when {
        isFocused -> Color.White
        isSelected -> Color.White.copy(alpha = 0.22f)
        else -> Color.Transparent
    }

    val textColor = when {
        isFocused -> Color.Black
        isSelected -> TextPrimary
        else -> TextSecondary
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = BorderFocused,
                shape = RoundedCornerShape(16.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag("nav_tab_${screen.name}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = screen.title,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}
