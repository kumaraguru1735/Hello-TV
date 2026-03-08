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
import androidx.media3.common.Tracks
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
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextMuted)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile section
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(HotstarBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = HotstarBlue, modifier = Modifier.size(22.dp))
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
                            Text(
                                it.status.replaceFirstChar { c -> c.uppercase() },
                                color = StatusSuccess,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Video tracks
            item { SectionHeader("Video Quality") }
            item {
                TrackOptionItem("Auto (Adaptive)", true) {
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
                        TrackOptionItem("${fmt.width}x${fmt.height} (${fmt.bitrate / 1000}kbps)", selected) {
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

            // Audio tracks
            item { SectionHeader("Audio") }
            val audioGroups = vm.currentExoPlayer?.currentTracks?.groups?.filter { it.type == C.TRACK_TYPE_AUDIO } ?: emptyList()
            if (audioGroups.isEmpty()) {
                item { Text("No audio tracks", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(8.dp)) }
            }
            audioGroups.forEach { group ->
                for (i in 0 until group.length) {
                    val fmt = group.getTrackFormat(i)
                    val selected = group.isTrackSelected(i)
                    item {
                        TrackOptionItem(
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

            // Actions
            item { Spacer(Modifier.height(4.dp)) }
            item {
                ActionItem("Manage Sessions", Icons.Default.Devices) {
                    vm.showSessionManager = true
                    onDismiss()
                }
            }
            item {
                ActionItem("Sign Out", Icons.Default.Logout, tint = StatusLive) {
                    vm.doLogout()
                    onDismiss()
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        color = TextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun TrackOptionItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) HotstarBlue.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (isSelected) HotstarBlueLight else TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, null, tint = HotstarBlue, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ActionItem(label: String, icon: ImageVector, tint: Color = TextSecondary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = tint, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
