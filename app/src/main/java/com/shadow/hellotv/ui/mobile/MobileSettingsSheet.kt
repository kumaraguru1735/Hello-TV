package com.shadow.hellotv.ui.mobile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import com.shadow.hellotv.ui.theme.*
import com.shadow.hellotv.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, UnstableApi::class)
@Composable
fun MobileSettingsSheet(
    vm: MainViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

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
                Text("Settings", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // -- Profile card --
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar: gold gradient circle with initial
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, AccentGold, CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(GradientGoldStart, GradientGoldEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (vm.subscriber?.name?.firstOrNull()?.uppercase() ?: "U"),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                vm.subscriber?.name ?: "User",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                vm.subscriber?.phone ?: "",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        vm.subscriptionInfo?.let {
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StatusSuccess.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        it.status.replaceFirstChar { c -> c.uppercase() },
                                        color = StatusSuccess,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                it.expiredAt?.let { expiry ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        expiry,
                                        color = AccentGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -- Video Quality --
            item { SectionLabel("VIDEO QUALITY") }
            item {
                TrackItem("Auto (Adaptive)", true) {
                    vm.currentExoPlayer?.let { player ->
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                            .build()
                    }
                }
            }

            val videoGroups = vm.currentExoPlayer?.currentTracks?.groups?.filter { it.type == C.TRACK_TYPE_VIDEO } ?: emptyList()
            videoGroups.forEach { group ->
                for (i in 0 until group.length) {
                    val fmt = group.getTrackFormat(i)
                    val selected = group.isTrackSelected(i)
                    item {
                        TrackItem("${fmt.width}x${fmt.height} (${fmt.bitrate / 1000}kbps)", selected) {
                            vm.currentExoPlayer?.let { player ->
                                player.trackSelectionParameters = player.trackSelectionParameters
                                    .buildUpon()
                                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                    .build()
                            }
                        }
                    }
                }
            }

            // -- Audio --
            item { SectionLabel("AUDIO") }
            val audioGroups = vm.currentExoPlayer?.currentTracks?.groups?.filter { it.type == C.TRACK_TYPE_AUDIO } ?: emptyList()
            if (audioGroups.isEmpty()) {
                item { Text("No audio tracks available", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            }
            audioGroups.forEach { group ->
                for (i in 0 until group.length) {
                    val fmt = group.getTrackFormat(i)
                    val selected = group.isTrackSelected(i)
                    item {
                        TrackItem(
                            "${fmt.language?.uppercase() ?: "Track ${i + 1}"} - ${fmt.label ?: "${fmt.channelCount}ch"}",
                            selected
                        ) {
                            vm.currentExoPlayer?.let { player ->
                                player.trackSelectionParameters = player.trackSelectionParameters
                                    .buildUpon()
                                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                    .build()
                            }
                        }
                    }
                }
            }

            // -- Actions --
            item { Spacer(Modifier.height(4.dp)) }
            item {
                HorizontalDivider(thickness = 0.5.dp, color = SurfaceSeparator)
            }
            item {
                ActionRow("Manage Sessions", Icons.Default.Devices, tint = AccentGold) {
                    vm.showSessionManager = true
                    onDismiss()
                }
            }
            item {
                HorizontalDivider(thickness = 0.5.dp, color = SurfaceSeparator)
            }
            item {
                ActionRow("Sign Out", Icons.Default.Logout, tint = StatusLive) {
                    vm.doLogout()
                    onDismiss()
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        title,
        color = AccentGold,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
private fun TrackItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) AccentGold else TextSecondary,
        animationSpec = tween(200),
        label = "trackText"
    )
    val circleColor by animateColorAsState(
        targetValue = if (isSelected) AccentGold else TextMuted,
        animationSpec = tween(200),
        label = "trackCircle"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radio circle
        Box(
            modifier = Modifier
                .size(16.dp)
                .then(
                    if (isSelected) {
                        Modifier
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .border(1.5.dp, circleColor, CircleShape)
                    } else {
                        Modifier
                            .clip(CircleShape)
                            .border(1.5.dp, circleColor, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentGold)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            color = textColor,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionRow(label: String, icon: ImageVector, tint: Color = TextSecondary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = if (tint == StatusLive) StatusLive else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextDisabled, modifier = Modifier.size(18.dp))
    }
}
