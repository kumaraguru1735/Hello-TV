package com.shadow.hellotv.ui.common

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadow.hellotv.model.SessionInfo
import com.shadow.hellotv.ui.theme.*

@Composable
fun SessionKickoutScreen(
    sessions: List<SessionInfo>,
    isLoading: Boolean,
    errorMessage: String?,
    onReplaceDevice: (sessionId: Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config = LocalConfiguration.current
    val isCompact = config.screenHeightDp < 400
    val pad = if (isCompact) 14.dp else 28.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceDark)
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(Brush.radialGradient(listOf(AccentGold.copy(alpha = 0.06f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(pad),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 36.dp else 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentGoldSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Devices, null,
                        tint = AccentGold,
                        modifier = Modifier.size(if (isCompact) 20.dp else 28.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Device Limit Reached",
                        color = TextPrimary,
                        fontSize = if (isCompact) 16.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Replace a device to continue on this one",
                        color = TextMuted,
                        fontSize = if (isCompact) 11.sp else 13.sp
                    )
                }
            }

            Spacer(Modifier.height(if (isCompact) 8.dp else 16.dp))

            // Error
            errorMessage?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusLive.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ErrorOutline, null, tint = StatusLive, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(it, color = StatusLive, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGold)
                }
            } else {
                // Device grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = if (isCompact) 220.dp else 260.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(sessions) { session ->
                        DeviceCard(
                            session = session,
                            isCompact = isCompact,
                            onReplace = { onReplaceDevice(session.id) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(if (isCompact) 6.dp else 12.dp))

            // Logout button
            var logoutFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (logoutFocused) StatusLive.copy(alpha = 0.12f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (logoutFocused) StatusLive.copy(alpha = 0.6f) else StatusLive.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    )
                    .onFocusChanged { logoutFocused = it.isFocused }
                    .focusable()
                    .onKeyEvent { event ->
                        if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                            (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                             event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                            onLogout(); true
                        } else false
                    }
                    .clickable { onLogout() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, null, tint = if (logoutFocused) StatusLive else StatusLive.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Sign Out Instead",
                        color = if (logoutFocused) StatusLive else StatusLive.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    session: SessionInfo,
    isCompact: Boolean,
    onReplace: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val icon = when {
        session.deviceType.contains("tv", ignoreCase = true) -> Icons.Default.Tv
        session.deviceType.contains("mobile", ignoreCase = true) -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Devices
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                if (isFocused) 12.dp else 0.dp,
                RoundedCornerShape(16.dp),
                spotColor = AccentGold.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceElevated, SurfacePrimary)
                )
            )
            .border(
                if (isFocused) 1.5.dp else 1.dp,
                if (isFocused) AccentGold else SurfaceSeparator,
                RoundedCornerShape(16.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                     event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onReplace(); true
                } else false
            }
            .clickable { onReplace() }
            .scale(if (isFocused) 1.02f else 1f)
            .padding(if (isCompact) 12.dp else 16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 34.dp else 42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentGoldSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = AccentGold, modifier = Modifier.size(if (isCompact) 18.dp else 22.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.deviceName.ifEmpty { session.deviceModel },
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isCompact) 13.sp else 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        session.deviceModel,
                        color = TextMuted,
                        fontSize = if (isCompact) 10.sp else 12.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(if (isCompact) 6.dp else 10.dp))

            InfoRow("IP Address", session.ipAddress, isCompact)
            InfoRow("Type", session.deviceType.replace("_", " ").replaceFirstChar { it.uppercase() }, isCompact)
            InfoRow("Last Login", session.lastLogin.take(16), isCompact)

            Spacer(Modifier.height(if (isCompact) 8.dp else 12.dp))

            // Replace button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompact) 32.dp else 38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(GradientGoldStart, GradientGoldEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Replace This Device",
                        color = Color.Black,
                        fontSize = if (isCompact) 11.sp else 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isCompact: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 1.dp else 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = if (isCompact) 10.sp else 12.sp)
        Text(value, color = TextSecondary, fontSize = if (isCompact) 10.sp else 12.sp, fontWeight = FontWeight.Medium)
    }
}
