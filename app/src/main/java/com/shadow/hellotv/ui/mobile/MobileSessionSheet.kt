package com.shadow.hellotv.ui.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadow.hellotv.model.SessionInfo
import com.shadow.hellotv.network.ApiResult
import com.shadow.hellotv.network.ApiService
import com.shadow.hellotv.ui.theme.*
import com.shadow.hellotv.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileSessionSheet(
    vm: MainViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var sessions by remember { mutableStateOf<List<SessionInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load sessions
    LaunchedEffect(Unit) {
        val token = vm.sessionManager.sessionToken ?: return@LaunchedEffect
        when (val result = ApiService.getSessions(token)) {
            is ApiResult.Success -> {
                sessions = result.data.sessions
                isLoading = false
            }
            is ApiResult.Error -> {
                error = result.message
                isLoading = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfacePrimary,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AccentGold)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Active Sessions",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AccentGold,
                        strokeWidth = 2.dp
                    )
                }
            } else if (error != null) {
                Text(
                    error ?: "Failed to load sessions",
                    color = StatusLive,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (sessions.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Devices,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No active sessions",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sessions) { session ->
                        val isCurrent = session.deviceId == vm.sessionManager.getDeviceId()
                        SessionCard(
                            session = session,
                            isCurrent = isCurrent,
                            onKill = {
                                scope.launch {
                                    val token = vm.sessionManager.sessionToken ?: return@launch
                                    when (ApiService.kickDevice(token, session.id)) {
                                        is ApiResult.Success -> {
                                            sessions = sessions.filter { it.id != session.id }
                                        }
                                        is ApiResult.Error -> {
                                            error = "Failed to remove session"
                                        }
                                    }
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: SessionInfo,
    isCurrent: Boolean,
    onKill: () -> Unit
) {
    val icon = when {
        session.deviceType.contains("tv", ignoreCase = true) -> Icons.Default.Tv
        session.deviceType.contains("mobile", ignoreCase = true) -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Devices
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = if (isCurrent) {
            androidx.compose.foundation.BorderStroke(1.5.dp, AccentGold)
        } else {
            androidx.compose.foundation.BorderStroke(0.5.dp, SurfaceSeparator)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device icon in circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AccentGold, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        session.deviceName.ifEmpty { session.deviceModel },
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrent) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Current",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${session.deviceType.replace("_", " ").replaceFirstChar { it.uppercase() }} ${session.ipAddress}",
                    color = TextMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    session.lastLogin.take(16),
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            // Remove button (not for current session)
            if (!isCurrent) {
                OutlinedButton(
                    onClick = onKill,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusLive),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusLive.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Remove", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
