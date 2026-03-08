package com.shadow.hellotv.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.shadow.hellotv.model.Channel
import com.shadow.hellotv.model.Subscriber
import com.shadow.hellotv.model.SubscriptionInfo
import com.shadow.hellotv.ui.theme.*
import com.shadow.hellotv.utils.SessionManager

enum class SettingsTab { PROFILE, VIDEO, AUDIO, SESSIONS }

@OptIn(UnstableApi::class)
@Composable
fun TvSettingsPanel(
    subscriber: Subscriber?,
    channel: Channel?,
    exoPlayer: ExoPlayer?,
    subscriptionInfo: SubscriptionInfo?,
    sessionManager: SessionManager,
    onManageSessions: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(SettingsTab.PROFILE) }

    Box(
        modifier = modifier
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xE00B1622),
                        Color(0xF00B1622)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab row
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                TabItem(SettingsTab.PROFILE, "Profile", Icons.Default.Person, activeTab) { activeTab = it }
                TabItem(SettingsTab.VIDEO, "Video", Icons.Default.Videocam, activeTab) { activeTab = it }
                TabItem(SettingsTab.AUDIO, "Audio", Icons.Default.VolumeUp, activeTab) { activeTab = it }
                TabItem(SettingsTab.SESSIONS, "Sessions", Icons.Default.Devices, activeTab) { activeTab = it }
            }

            // Tab content
            when (activeTab) {
                SettingsTab.PROFILE -> ProfileTab(subscriber, subscriptionInfo, onLogout)
                SettingsTab.VIDEO -> VideoTrackTab(exoPlayer)
                SettingsTab.AUDIO -> AudioTrackTab(exoPlayer)
                SettingsTab.SESSIONS -> SessionsTab(onManageSessions)
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: SettingsTab,
    label: String,
    icon: ImageVector,
    activeTab: SettingsTab,
    onSelect: (SettingsTab) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val isActive = tab == activeTab
    val bgColor = when {
        isActive -> HotstarBlue
        isFocused -> HotstarBlue.copy(alpha = 0.3f)
        else -> SurfaceElevated
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onSelect(tab) }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isActive) Color.White else TextMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                color = if (isActive || isFocused) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ProfileTab(
    subscriber: Subscriber?,
    subscriptionInfo: SubscriptionInfo?,
    onLogout: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // User card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Column {
                            Text(
                                subscriber?.name ?: "User",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                subscriber?.phone ?: "",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Subscription info
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Subscription", color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            subscriptionInfo?.status ?: "Active",
                            color = StatusSuccess,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    subscriptionInfo?.expiredIn?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Expires In", color = TextSecondary, fontSize = 13.sp)
                            Text(it, color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Logout
        item {
            var isFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isFocused) StatusLive.copy(alpha = 0.2f) else SurfaceCard)
                    .onFocusChanged { isFocused = it.isFocused }
                    .focusable()
                    .clickable { onLogout() }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, null, tint = StatusLive, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Sign Out", color = StatusLive, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoTrackTab(exoPlayer: ExoPlayer?) {
    val tracks = exoPlayer?.currentTracks ?: Tracks.EMPTY
    val videoGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_VIDEO }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Video Quality", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        }

        // Auto option
        item {
            TrackOption(
                label = "Auto",
                detail = "Adaptive",
                isSelected = true,
                onClick = {
                    exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                        ?.buildUpon()
                        ?.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                        ?.build() ?: return@TrackOption
                }
            )
        }

        videoGroups.forEachIndexed { groupIdx, group ->
            for (trackIdx in 0 until group.length) {
                val format = group.getTrackFormat(trackIdx)
                val isSelected = group.isTrackSelected(trackIdx)
                item {
                    TrackOption(
                        label = "${format.width}x${format.height}",
                        detail = "${(format.bitrate / 1000)}kbps",
                        isSelected = isSelected,
                        onClick = {
                            exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                ?.buildUpon()
                                ?.setOverrideForType(
                                    androidx.media3.common.TrackSelectionOverride(group.mediaTrackGroup, trackIdx)
                                )
                                ?.build() ?: return@TrackOption
                        }
                    )
                }
            }
        }

        if (videoGroups.isEmpty()) {
            item {
                Text("No video tracks available", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun AudioTrackTab(exoPlayer: ExoPlayer?) {
    val tracks = exoPlayer?.currentTracks ?: Tracks.EMPTY
    val audioGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Audio Track", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        }

        audioGroups.forEachIndexed { groupIdx, group ->
            for (trackIdx in 0 until group.length) {
                val format = group.getTrackFormat(trackIdx)
                val isSelected = group.isTrackSelected(trackIdx)
                item {
                    TrackOption(
                        label = format.language?.uppercase() ?: "Track ${trackIdx + 1}",
                        detail = format.label ?: "${format.channelCount}ch ${format.sampleRate / 1000}kHz",
                        isSelected = isSelected,
                        onClick = {
                            exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                ?.buildUpon()
                                ?.setOverrideForType(
                                    androidx.media3.common.TrackSelectionOverride(group.mediaTrackGroup, trackIdx)
                                )
                                ?.build() ?: return@TrackOption
                        }
                    )
                }
            }
        }

        if (audioGroups.isEmpty()) {
            item {
                Text("No audio tracks available", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
private fun SessionsTab(onManageSessions: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Devices, null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(8.dp))
        Text("Manage your connected devices", color = TextMuted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        var isFocused by remember { mutableStateOf(false) }
        Button(
            onClick = onManageSessions,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HotstarBlue),
            modifier = Modifier
                .onFocusChanged { isFocused = it.isFocused }
                .focusable()
        ) {
            Text("Manage Sessions")
        }
    }
}

@Composable
private fun TrackOption(
    label: String,
    detail: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val bgColor = when {
        isSelected -> HotstarBlue.copy(alpha = 0.2f)
        isFocused -> SurfaceOverlay
        else -> SurfaceCard
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = if (isSelected) HotstarBlueLight else TextPrimary, fontSize = 14.sp)
            Text(detail, color = TextMuted, fontSize = 11.sp)
        }
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, null, tint = HotstarBlue, modifier = Modifier.size(18.dp))
        }
    }
}
