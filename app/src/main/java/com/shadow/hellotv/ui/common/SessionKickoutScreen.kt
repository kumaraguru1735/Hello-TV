package com.shadow.hellotv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(HotstarNavy, SurfaceDark, Color(0xFF0A0F1C))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Icon(
                Icons.Default.Devices,
                contentDescription = null,
                tint = StatusWarning,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Device Limit Reached",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Replace an existing device to continue on this one",
                color = TextMuted,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            // Error
            errorMessage?.let {
                Text(it, color = StatusLive, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(color = HotstarBlue, modifier = Modifier.padding(32.dp))
            } else {
                // Device grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(sessions) { session ->
                        DeviceCard(
                            session = session,
                            onReplace = { onReplaceDevice(session.id) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Logout button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = ButtonDefaults.outlinedButtonBorder(true)
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out Instead")
            }
        }
    }
}

@Composable
private fun DeviceCard(
    session: SessionInfo,
    onReplace: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val icon = when {
        session.deviceType.contains("tv", ignoreCase = true) -> Icons.Default.Tv
        session.deviceType.contains("mobile", ignoreCase = true) -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Devices
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .then(
                if (isFocused) Modifier.border(2.dp, HotstarBlue, RoundedCornerShape(14.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HotstarBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = HotstarBlue, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.deviceName.ifEmpty { session.deviceModel },
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        session.deviceModel,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Info rows
            InfoRow("IP Address", session.ipAddress)
            InfoRow("Type", session.deviceType.replace("_", " ").replaceFirstChar { it.uppercase() })
            InfoRow("Last Login", session.lastLogin.take(16))

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onReplace,
                modifier = Modifier.fillMaxWidth().height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusLive)
            ) {
                Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Replace This Device", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
