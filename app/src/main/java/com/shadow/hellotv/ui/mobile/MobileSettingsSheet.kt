package com.shadow.hellotv.ui.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            // ── Profile card ──
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(HotstarBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = HotstarBlue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                vm.subscriber?.name ?: "User",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                vm.subscriber?.phone ?: "",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        vm.subscriptionInfo?.let {
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
                        }
                    }
                }
            }

            // ── Video Quality ──
            item { SectionLabel("Video Quality") }
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

            // ── Audio ──
            item { SectionLabel("Audio") }
            val audioGroups = vm.currentExoPlayer?.currentTracks?.groups?.filter { it.type == C.TRACK_TYPE_AUDIO } ?: emptyList()
            if (audioGroups.isEmpty()) {
                item { Text("No audio tracks available", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(8.dp)) }
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

            // ── Actions ──
            item { Spacer(Modifier.height(4.dp)) }
            item {
                ActionRow("Manage Sessions", Icons.Default.Devices) {
                    vm.showSessionManager = true
                    onDismiss()
                }
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
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
    )
}

@Composable
private fun TrackItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) HotstarBlue.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = if (isSelected) HotstarBlueLight else TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, null, tint = HotstarBlue, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ActionRow(label: String, icon: ImageVector, tint: Color = TextSecondary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = tint, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextDisabled, modifier = Modifier.size(18.dp))
    }
}
