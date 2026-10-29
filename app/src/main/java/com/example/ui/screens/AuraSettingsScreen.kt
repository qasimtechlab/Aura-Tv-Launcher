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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PinVaultDialog
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderFocused
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import com.example.ui.viewmodel.AuraLauncherViewModel

@Composable
fun AuraSettingsScreen(
    viewModel: AuraLauncherViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clockConfig by viewModel.clockCustomization.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val showPinDialog by viewModel.showPinDialog.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 36.dp, vertical = 12.dp)
            .testTag("aura_settings_screen")
    ) {
        // Title
        Text(
            text = "Personalization & TV Settings",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Configure home launcher preferences, depth wallpaper, and system controls",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Launcher Integration
        Text(
            text = "Home Launcher Integration",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingRowCard(
            icon = Icons.Default.Home,
            title = "Set as Default Android TV Launcher",
            subtitle = "Opens Android Home settings so Aura TV launches when pressing Home on TV remote",
            onClick = { viewModel.openHomeSettings() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingRowCard(
            icon = Icons.Default.Refresh,
            title = "Reload Installed Applications",
            subtitle = "Re-scans all TV and sideloaded APK packages on your device",
            onClick = { viewModel.loadApps() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Wallpaper & Clock
        Text(
            text = "Depth Wallpaper & Clock",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingRowCard(
            icon = Icons.Default.Image,
            title = "Open Depth Wallpaper Studio",
            subtitle = "Customize 3-layer wallpapers, typography, clock size, and foreground cutouts",
            onClick = onNavigateToStudio
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 24-Hour Clock Toggle
        SettingToggleCard(
            icon = Icons.Default.Schedule,
            title = "24-Hour Time Format",
            subtitle = if (clockConfig.is24Hour) "Using 24-hour military format (e.g. 23:45)" else "Using standard 12-hour format (e.g. 11:45)",
            isChecked = clockConfig.is24Hour,
            onCheckedChange = { viewModel.toggle24HourFormat(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Date Display Toggle
        SettingToggleCard(
            icon = Icons.Default.Schedule,
            title = "Show Date Under Clock",
            subtitle = if (clockConfig.showDate) "Date is visible below the oversized clock" else "Date badge hidden",
            isChecked = clockConfig.showDate,
            onCheckedChange = { viewModel.toggleDateVisibility(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Security & Device Settings
        Text(
            text = "Privacy & System",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingRowCard(
            icon = Icons.Default.Lock,
            title = if (isVaultUnlocked) "Lock Protected Apps Vault" else "Unlock Protected Apps Vault (PIN: 0000)",
            subtitle = if (isVaultUnlocked) "Protected hidden apps are currently visible in launcher" else "Hidden apps are locked behind 4-digit PIN",
            onClick = {
                if (isVaultUnlocked) viewModel.lockVault() else viewModel.setPinDialogVisible(true)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingRowCard(
            icon = Icons.Default.Settings,
            title = "Android TV System Settings",
            subtitle = "Open TV device settings: Wi-Fi, Bluetooth, Audio passthrough, Display",
            onClick = { viewModel.openSystemSettings() }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Footer Info
        Text(
            text = "Aura TV • Apple TV & Depth Wallpaper Edition for Android TV\nVersion 2.0 • Ultra-HD Leanback Launcher",
            color = Color(0x66FFFFFF),
            fontSize = 11.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(30.dp))
    }

    PinVaultDialog(
        isOpen = showPinDialog,
        onDismiss = { viewModel.setPinDialogVisible(false) },
        onVerifyPin = { viewModel.verifyPinAndUnlock(it) }
    )
}

@Composable
private fun SettingRowCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color.White.copy(alpha = 0.22f) else TvSurface)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) BorderFocused else BorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("setting_row_$title"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isFocused) AppleBlue else Color(0x22FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.White else TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SettingToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color.White.copy(alpha = 0.22f) else TvSurface)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) BorderFocused else BorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x22FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppleBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}
