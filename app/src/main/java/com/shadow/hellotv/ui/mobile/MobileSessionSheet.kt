package com.shadow.hellotv.ui.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        containerColor = SurfaceDark,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextMuted)
                )
                Spacer(Modifier.height(8.dp))
                Text("Active Sessions", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HotstarBlue)
                }
            } else if (error != null) {
                Text(
                    error ?: "Failed to load sessions",
                    color = StatusLive,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions) { session ->
                        SessionCard(
                            session = session,
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
    onKill: () -> Unit
) {
    val icon = when {
        session.deviceType.contains("tv", ignoreCase = true) -> Icons.Default.Tv
        session.deviceType.contains("mobile", ignoreCase = true) -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Devices
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HotstarBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = HotstarBlue, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.deviceName.ifEmpty { session.deviceModel },
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${session.deviceType.replace("_", " ").replaceFirstChar { it.uppercase() }} • ${session.ipAddress}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    session.lastLogin.take(16),
                    color = TextDisabled,
                    fontSize = 10.sp
                )
            }

            // Remove button
            TextButton(
                onClick = onKill,
                colors = ButtonDefaults.textButtonColors(contentColor = StatusLive)
            ) {
                Text("Remove", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
