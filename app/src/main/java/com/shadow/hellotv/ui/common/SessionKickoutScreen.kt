package com.shadow.hellotv.ui.common

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
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

    // Fade-in animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "fadeIn"
    )

    // Pulse animation for warning icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .graphicsLayer { this.alpha = alpha }
    ) {
        // Centered warm gold glow
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.Center)
                .background(Brush.radialGradient(listOf(AccentGold.copy(alpha = 0.05f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(pad),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with pulse animation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Pulse glow behind icon
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 44.dp else 56.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(AccentGold.copy(alpha = pulseAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 36.dp else 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning, null,
                            tint = AccentGold,
                            modifier = Modifier.size(if (isCompact) 20.dp else 28.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Device Limit Reached",
                        color = TextPrimary,
                        fontSize = if (isCompact) 18.sp else 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Choose a device to replace",
                        color = TextMuted,
                        fontSize = if (isCompact) 12.sp else 14.sp
                    )
                }
            }

            Spacer(Modifier.height(if (isCompact) 10.dp else 20.dp))

            // Error
            errorMessage?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StatusLive.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = StatusLive, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(it, color = StatusLive, fontSize = 13.sp)
                }
                Spacer(Modifier.height(10.dp))
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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

            Spacer(Modifier.height(if (isCompact) 8.dp else 16.dp))

            // Logout button
            var logoutFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (logoutFocused) StatusLive.copy(alpha = 0.10f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (logoutFocused) StatusLive else StatusLive.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
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
                    .clickable { onLogout() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, null, tint = StatusLive, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Sign Out Instead",
                        color = StatusLive,
                        fontSize = 14.sp,
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

    // Animated border/bg
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) AccentGold else SurfaceSeparator.copy(alpha = 0.5f),
        animationSpec = tween(250),
        label = "cardBorder"
    )
    val cardBg by animateColorAsState(
        targetValue = if (isFocused) AccentGoldSoft.copy(alpha = 0.3f) else Color.Transparent,
        animationSpec = tween(250),
        label = "cardBg"
    )

    val icon = when {
        session.deviceType.contains("tv", ignoreCase = true) -> Icons.Default.Tv
        session.deviceType.contains("mobile", ignoreCase = true) -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Devices
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                if (isFocused) 16.dp else 0.dp,
                RoundedCornerShape(16.dp),
                spotColor = AccentGold.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .background(cardBg)
            .border(
                if (isFocused) 1.5.dp else 0.5.dp,
                borderColor,
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
                // Device icon in circle
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 36.dp else 40.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = AccentGold, modifier = Modifier.size(if (isCompact) 18.dp else 22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.deviceName.ifEmpty { session.deviceModel },
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        session.deviceModel,
                        color = TextMuted,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(if (isCompact) 8.dp else 12.dp))

            InfoRow("IP Address", session.ipAddress, isCompact)
            InfoRow("Type", session.deviceType.replace("_", " ").replaceFirstChar { it.uppercase() }, isCompact)
            InfoRow("Last Login", session.lastLogin.take(16), isCompact)

            Spacer(Modifier.height(if (isCompact) 10.dp else 14.dp))

            // Replace button - gold gradient, full width
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(GradientGoldStart, GradientGoldEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Replace This Device",
                        color = Color.Black,
                        fontSize = if (isCompact) 13.sp else 14.sp,
                        fontWeight = FontWeight.Bold
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
            .padding(vertical = if (isCompact) 2.dp else 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = if (isCompact) 11.sp else 13.sp)
        Text(value, color = TextSecondary, fontSize = if (isCompact) 11.sp else 13.sp, fontWeight = FontWeight.Medium)
    }
}
