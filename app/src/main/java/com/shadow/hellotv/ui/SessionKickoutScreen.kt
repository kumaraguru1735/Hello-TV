package com.shadow.hellotv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val configuration = LocalConfiguration.current
    val isTV = configuration.screenWidthDp >= 1000

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SurfaceDark, HotstarNavy, SurfaceDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = if (isTV) 600.dp else 500.dp)
                .padding(24.dp)
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = StatusWarning.copy(alpha = 0.3f)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(SurfaceCard.copy(0.97f), SurfaceOverlay.copy(0.97f))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(StatusWarning.copy(0.4f), HotstarPink.copy(0.4f))
                        ),
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isTV) 36.dp else 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Warning icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(StatusWarning.copy(0.2f), HotstarPink.copy(0.2f))
                                )
                            )
                            .border(2.dp, StatusWarning.copy(0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DevicesOther,
                            contentDescription = null,
                            tint = StatusWarning,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Device Limit Reached",
                        fontSize = if (isTV) 28.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Select a device to replace with this one",
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusLive.copy(0.15f))
                                .padding(10.dp)
                        ) {
                            Text(errorMessage, color = StatusLive, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sessions list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(sessions) { index, session ->
                            SessionDeviceItem(
                                session = session,
                                isTV = isTV,
                                isLoading = isLoading,
                                onReplace = { onReplaceDevice(session.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logout button
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextMuted
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel & Logout", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionDeviceItem(
    session: SessionInfo,
    isTV: Boolean,
    isLoading: Boolean,
    onReplace: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(enabled = !isLoading) { onReplace() },
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused)
                HotstarBlue.copy(alpha = 0.15f)
            else
                Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = if (isFocused) {
            ButtonDefaults.outlinedButtonBorder(enabled = true)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Device icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (session.deviceType == "android_tv")
                            HotstarBlue.copy(0.2f)
                        else
                            HotstarPink.copy(0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (session.deviceType == "android_tv")
                        Icons.Default.Tv
                    else
                        Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = if (session.deviceType == "android_tv") HotstarBlue else HotstarPink,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Device info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.deviceName.ifEmpty { session.deviceModel },
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${session.deviceType.replace("_", " ").replaceFirstChar { it.uppercase() }} | ${session.ipAddress}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Replace button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(StatusLive.copy(0.8f), HotstarPink.copy(0.8f))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Replace",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
