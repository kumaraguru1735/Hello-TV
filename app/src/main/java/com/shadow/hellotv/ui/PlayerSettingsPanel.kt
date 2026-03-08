package com.shadow.hellotv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import com.shadow.hellotv.model.Channel
import com.shadow.hellotv.model.Subscriber
import com.shadow.hellotv.ui.theme.*

data class TrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val label: String,
    val isSelected: Boolean,
    val format: Format? = null
)

@Composable
fun PlayerSettingsPanel(
    show: Boolean,
    subscriber: Subscriber?,
    channel: Channel?,
    exoPlayer: ExoPlayer?,
    subscriptionStatus: String?,
    expiredIn: String?,
    onLogout: () -> Unit,
    onManageSessions: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTV = configuration.screenWidthDp >= 1000

    // Collect track info from player
    var videoTracks by remember { mutableStateOf<List<TrackInfo>>(emptyList()) }
    var audioTracks by remember { mutableStateOf<List<TrackInfo>>(emptyList()) }
    var subtitleTracks by remember { mutableStateOf<List<TrackInfo>>(emptyList()) }

    // Update tracks when player state changes
    LaunchedEffect(exoPlayer, show) {
        if (exoPlayer != null && show) {
            val tracks = exoPlayer.currentTracks
            videoTracks = extractTracks(tracks, C.TRACK_TYPE_VIDEO)
            audioTracks = extractTracks(tracks, C.TRACK_TYPE_AUDIO)
            subtitleTracks = extractTracks(tracks, C.TRACK_TYPE_TEXT)
        }
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(300)) + slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(tween(200)) + slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(200)
        ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .width(if (isTV) 360.dp else 320.dp)
                .fillMaxHeight()
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .shadow(
                    24.dp,
                    RoundedCornerShape(24.dp),
                    spotColor = HotstarBlue.copy(0.3f)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                SurfaceDark.copy(0.97f),
                                SurfaceCard.copy(0.97f),
                                SurfaceDark.copy(0.97f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                HotstarBlue.copy(0.3f),
                                HotstarPink.copy(0.2f),
                                HotstarBlue.copy(0.3f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ── Profile Section ──
                    item {
                        if (subscriber != null) {
                            ProfileSection(
                                subscriber = subscriber,
                                subscriptionStatus = subscriptionStatus,
                                expiredIn = expiredIn,
                                isTV = isTV
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsDivider()
                        }
                    }

                    // ── Now Playing ──
                    item {
                        if (channel != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            SectionHeader("Now Playing", Icons.Default.PlayCircle)
                            Spacer(modifier = Modifier.height(8.dp))
                            NowPlayingCard(channel, isTV)
                            Spacer(modifier = Modifier.height(4.dp))
                            SettingsDivider()
                        }
                    }

                    // ── Video Quality ──
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader("Video Quality", Icons.Default.HighQuality)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (videoTracks.isEmpty()) {
                        item {
                            TrackOptionItem(
                                label = "Auto",
                                subtitle = "Best quality for your connection",
                                isSelected = true,
                                onClick = {}
                            )
                        }
                    } else {
                        // Auto option
                        item {
                            TrackOptionItem(
                                label = "Auto",
                                subtitle = "Adaptive quality",
                                isSelected = videoTracks.none { it.isSelected },
                                onClick = {
                                    exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                        ?.buildUpon()
                                        ?.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                        ?.build() ?: return@TrackOptionItem
                                }
                            )
                        }
                        items(videoTracks) { track ->
                            val resolution = track.format?.let { "${it.width}x${it.height}" } ?: ""
                            val bitrate = track.format?.bitrate?.let {
                                if (it > 1_000_000) "${it / 1_000_000}Mbps" else "${it / 1000}Kbps"
                            } ?: ""
                            val label = track.format?.let {
                                when {
                                    it.height >= 2160 -> "4K Ultra HD"
                                    it.height >= 1080 -> "Full HD 1080p"
                                    it.height >= 720 -> "HD 720p"
                                    it.height >= 480 -> "SD 480p"
                                    it.height >= 360 -> "Low 360p"
                                    else -> resolution
                                }
                            } ?: track.label

                            TrackOptionItem(
                                label = label,
                                subtitle = "$resolution ${if (bitrate.isNotEmpty()) "| $bitrate" else ""}".trim(),
                                isSelected = track.isSelected,
                                onClick = {
                                    selectTrack(exoPlayer, C.TRACK_TYPE_VIDEO, track.groupIndex, track.trackIndex)
                                }
                            )
                        }
                    }

                    item { SettingsDivider() }

                    // ── Audio Track ──
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader("Audio", Icons.Default.Audiotrack)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (audioTracks.isEmpty()) {
                        item {
                            TrackOptionItem(
                                label = "Default",
                                subtitle = "Primary audio track",
                                isSelected = true,
                                onClick = {}
                            )
                        }
                    } else {
                        items(audioTracks) { track ->
                            val lang = track.format?.language?.let { langCode ->
                                java.util.Locale(langCode).displayLanguage
                            } ?: ""
                            val channels = track.format?.channelCount?.let {
                                when (it) {
                                    1 -> "Mono"
                                    2 -> "Stereo"
                                    6 -> "5.1 Surround"
                                    8 -> "7.1 Surround"
                                    else -> "${it}ch"
                                }
                            } ?: ""

                            TrackOptionItem(
                                label = track.label.ifEmpty { lang.ifEmpty { "Track ${track.trackIndex + 1}" } },
                                subtitle = listOf(lang, channels).filter { it.isNotEmpty() }.joinToString(" | "),
                                isSelected = track.isSelected,
                                onClick = {
                                    selectTrack(exoPlayer, C.TRACK_TYPE_AUDIO, track.groupIndex, track.trackIndex)
                                }
                            )
                        }
                    }

                    // ── Subtitles ──
                    if (subtitleTracks.isNotEmpty()) {
                        item { SettingsDivider() }
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            SectionHeader("Subtitles", Icons.Default.Subtitles)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            TrackOptionItem(
                                label = "Off",
                                subtitle = "Disable subtitles",
                                isSelected = subtitleTracks.none { it.isSelected },
                                onClick = {
                                    exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                        ?.buildUpon()
                                        ?.clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                        ?.build() ?: return@TrackOptionItem
                                }
                            )
                        }
                        items(subtitleTracks) { track ->
                            val lang = track.format?.language?.let {
                                java.util.Locale(it).displayLanguage
                            } ?: "Track ${track.trackIndex + 1}"

                            TrackOptionItem(
                                label = track.label.ifEmpty { lang },
                                subtitle = track.format?.sampleMimeType ?: "",
                                isSelected = track.isSelected,
                                onClick = {
                                    selectTrack(exoPlayer, C.TRACK_TYPE_TEXT, track.groupIndex, track.trackIndex)
                                }
                            )
                        }
                    }

                    item { SettingsDivider() }

                    // ── Account Actions ──
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader("Account", Icons.Default.ManageAccounts)
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsActionItem(
                            icon = Icons.Default.DevicesOther,
                            label = "Manage Devices",
                            subtitle = "View and manage connected devices",
                            onClick = onManageSessions
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsActionItem(
                            icon = Icons.Default.Logout,
                            label = "Sign Out",
                            subtitle = "Logout from this device",
                            isDanger = true,
                            onClick = onLogout
                        )
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "← to close | ↑↓ navigate",
                            color = TextDisabled,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    subscriber: Subscriber,
    subscriptionStatus: String?,
    expiredIn: String?,
    isTV: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(if (isTV) 56.dp else 48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(HotstarBlue, HotstarPink))
                )
                .border(2.dp, HotstarBlue.copy(0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = subscriber.name.firstOrNull()?.uppercase() ?: "U",
                color = Color.White,
                fontSize = if (isTV) 24.sp else 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subscriber.name,
                color = Color.White,
                fontSize = if (isTV) 18.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subscriber.phone,
                color = TextMuted,
                fontSize = 13.sp
            )
            if (subscriptionStatus != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (subscriptionStatus == "Active") StatusSuccess
                                else StatusLive
                            )
                    )
                    Text(
                        text = if (expiredIn != null) "$subscriptionStatus | $expiredIn" else subscriptionStatus,
                        color = if (subscriptionStatus == "Active") StatusSuccess else StatusLive,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun NowPlayingCard(channel: Channel, isTV: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HotstarBlue.copy(0.1f))
            .border(1.dp, HotstarBlue.copy(0.2f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = channel.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stream type badge
                SettingsBadge(
                    text = channel.streamType.uppercase(),
                    color = HotstarBlue
                )

                // DRM badge
                if (!channel.drmType.isNullOrEmpty()) {
                    SettingsBadge(
                        text = "DRM",
                        color = StatusWarning
                    )
                }

                // Premium badge
                if (channel.premium == 1) {
                    SettingsBadge(
                        text = "PREMIUM",
                        color = HotstarPink
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HotstarBlue,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = Color.White.copy(0.08f),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.15f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TrackOptionItem(
    label: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> HotstarBlue.copy(0.15f)
                    isFocused -> Color.White.copy(0.08f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isSelected) {
                    Modifier.border(1.dp, HotstarBlue.copy(0.3f), RoundedCornerShape(12.dp))
                } else if (isFocused) {
                    Modifier.border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(12.dp))
                } else Modifier
            )
            .clickable { onClick() }
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = if (isSelected) HotstarBlueLight else Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = HotstarBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val accentColor = if (isDanger) StatusLive else HotstarBlue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) accentColor.copy(0.1f) else Color.White.copy(0.04f))
            .then(
                if (isFocused) Modifier.border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable { onClick() }
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) accentColor else TextMuted,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = if (isDanger && isFocused) accentColor else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextDisabled,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Track extraction helpers ──

private fun extractTracks(tracks: Tracks, trackType: Int): List<TrackInfo> {
    val result = mutableListOf<TrackInfo>()
    for (groupIndex in 0 until tracks.groups.size) {
        val group = tracks.groups[groupIndex]
        if (group.type != trackType) continue

        for (trackIndex in 0 until group.length) {
            if (!group.isTrackSupported(trackIndex)) continue
            val format = group.getTrackFormat(trackIndex)
            val label = format.label ?: format.language?.let { java.util.Locale(it).displayLanguage } ?: ""

            result.add(
                TrackInfo(
                    groupIndex = groupIndex,
                    trackIndex = trackIndex,
                    label = label,
                    isSelected = group.isTrackSelected(trackIndex),
                    format = format
                )
            )
        }
    }
    return result
}

private fun selectTrack(player: ExoPlayer?, trackType: Int, groupIndex: Int, trackIndex: Int) {
    player ?: return
    val tracks = player.currentTracks
    if (groupIndex >= tracks.groups.size) return

    val group = tracks.groups[groupIndex]
    val override = androidx.media3.common.TrackSelectionOverride(group.mediaTrackGroup, listOf(trackIndex))

    player.trackSelectionParameters = player.trackSelectionParameters
        .buildUpon()
        .clearOverridesOfType(trackType)
        .addOverride(override)
        .build()
}
