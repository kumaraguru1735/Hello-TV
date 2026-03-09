package com.shadow.hellotv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.rotate
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

    // Expandable sections state
    var qualityExpanded by remember { mutableStateOf(false) }
    var audioExpanded by remember { mutableStateOf(false) }
    var subtitlesExpanded by remember { mutableStateOf(false) }

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
        // Panel background: gradient from right (SurfacePrimary -> transparent left edge)
        Box(
            modifier = Modifier
                .width(if (isTV) 360.dp else 320.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SurfacePrimary.copy(0.95f),
                            SurfacePrimary
                        ),
                        startX = 0f,
                        endX = 100f
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfacePrimary.copy(0.97f))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // -- Profile Section --
                    if (subscriber != null) {
                        item {
                            ProfileSection(
                                subscriber = subscriber,
                                subscriptionStatus = subscriptionStatus,
                                expiredIn = expiredIn,
                                isTV = isTV
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsDivider()
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // -- Now Playing --
                    if (channel != null) {
                        item {
                            NowPlayingRow(channel, isTV)
                            Spacer(modifier = Modifier.height(4.dp))
                            SettingsDivider()
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // -- Quality Section (expandable) --
                    item {
                        val currentQuality = videoTracks.firstOrNull { it.isSelected }?.format?.let {
                            when {
                                it.height >= 2160 -> "4K"
                                it.height >= 1080 -> "1080p"
                                it.height >= 720 -> "720p"
                                it.height >= 480 -> "480p"
                                else -> "Auto"
                            }
                        } ?: "Auto"

                        SettingsRow(
                            icon = Icons.Default.HighQuality,
                            title = "Quality",
                            value = currentQuality,
                            isExpanded = qualityExpanded,
                            onClick = { qualityExpanded = !qualityExpanded }
                        )
                    }

                    // Quality sub-options
                    if (qualityExpanded) {
                        item {
                            RadioOptionItem(
                                label = "Auto",
                                subtitle = "Best quality for your connection",
                                isSelected = videoTracks.isEmpty() || videoTracks.none { it.isSelected },
                                onClick = {
                                    exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                        ?.buildUpon()
                                        ?.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                        ?.build() ?: return@RadioOptionItem
                                }
                            )
                        }
                        if (videoTracks.isNotEmpty()) {
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

                                RadioOptionItem(
                                    label = label,
                                    subtitle = "$resolution ${if (bitrate.isNotEmpty()) "| $bitrate" else ""}".trim(),
                                    isSelected = track.isSelected,
                                    onClick = {
                                        selectTrack(exoPlayer, C.TRACK_TYPE_VIDEO, track.groupIndex, track.trackIndex)
                                    }
                                )
                            }
                        }
                    }

                    // -- Audio Section (expandable) --
                    item {
                        val currentAudio = audioTracks.firstOrNull { it.isSelected }?.let { track ->
                            track.format?.language?.let { java.util.Locale(it).displayLanguage }
                                ?: track.label.ifEmpty { "Default" }
                        } ?: "Default"

                        SettingsRow(
                            icon = Icons.Default.Audiotrack,
                            title = "Audio",
                            value = currentAudio,
                            isExpanded = audioExpanded,
                            onClick = { audioExpanded = !audioExpanded }
                        )
                    }

                    if (audioExpanded) {
                        if (audioTracks.isEmpty()) {
                            item {
                                RadioOptionItem(
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

                                RadioOptionItem(
                                    label = track.label.ifEmpty { lang.ifEmpty { "Track ${track.trackIndex + 1}" } },
                                    subtitle = listOf(lang, channels).filter { it.isNotEmpty() }.joinToString(" | "),
                                    isSelected = track.isSelected,
                                    onClick = {
                                        selectTrack(exoPlayer, C.TRACK_TYPE_AUDIO, track.groupIndex, track.trackIndex)
                                    }
                                )
                            }
                        }
                    }

                    // -- Subtitles Section (expandable) --
                    if (subtitleTracks.isNotEmpty()) {
                        item {
                            val currentSub = subtitleTracks.firstOrNull { it.isSelected }?.let { track ->
                                track.format?.language?.let { java.util.Locale(it).displayLanguage }
                                    ?: track.label.ifEmpty { "On" }
                            } ?: "Off"

                            SettingsRow(
                                icon = Icons.Default.Subtitles,
                                title = "Subtitles",
                                value = currentSub,
                                isExpanded = subtitlesExpanded,
                                onClick = { subtitlesExpanded = !subtitlesExpanded }
                            )
                        }

                        if (subtitlesExpanded) {
                            item {
                                RadioOptionItem(
                                    label = "Off",
                                    subtitle = "Disable subtitles",
                                    isSelected = subtitleTracks.none { it.isSelected },
                                    onClick = {
                                        exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                            ?.buildUpon()
                                            ?.clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                            ?.build() ?: return@RadioOptionItem
                                    }
                                )
                            }
                            items(subtitleTracks) { track ->
                                val lang = track.format?.language?.let {
                                    java.util.Locale(it).displayLanguage
                                } ?: "Track ${track.trackIndex + 1}"

                                RadioOptionItem(
                                    label = track.label.ifEmpty { lang },
                                    subtitle = track.format?.sampleMimeType ?: "",
                                    isSelected = track.isSelected,
                                    onClick = {
                                        selectTrack(exoPlayer, C.TRACK_TYPE_TEXT, track.groupIndex, track.trackIndex)
                                    }
                                )
                            }
                        }
                    }

                    item {
                        SettingsDivider()
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // -- Manage Devices --
                    item {
                        SettingsRow(
                            icon = Icons.Default.DevicesOther,
                            title = "Manage Devices",
                            value = "",
                            showChevron = true,
                            onClick = onManageSessions
                        )
                    }

                    // -- Logout at bottom --
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SettingsDivider()
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onLogout() }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = StatusLive,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Sign Out",
                                    color = StatusLive,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "\u2190 to close | \u2191\u2193 navigate",
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(if (isTV) 52.dp else 44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(GradientGoldStart, GradientGoldEnd))
                )
                .border(1.5.dp, AccentGold.copy(0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = subscriber.name.firstOrNull()?.uppercase() ?: "U",
                color = Color.Black,
                fontSize = if (isTV) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subscriber.name,
                color = TextPrimary,
                fontSize = if (isTV) 16.sp else 15.sp,
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
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (subscriptionStatus == "Active") StatusSuccess
                                else StatusLive
                            )
                    )
                    Text(
                        text = if (expiredIn != null) "$subscriptionStatus | $expiredIn" else subscriptionStatus,
                        color = if (subscriptionStatus == "Active") StatusSuccess else StatusLive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun NowPlayingRow(channel: Channel, isTV: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = null,
            tint = AccentGold,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = channel.streamType.uppercase(),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!channel.drmType.isNullOrEmpty()) {
                    Text(
                        text = "\u2022 DRM",
                        color = StatusWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String,
    isExpanded: Boolean = false,
    showChevron: Boolean = false,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(200),
        label = "chevron"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused) AccentGoldSoft else Color.Transparent
            )
            .then(
                if (isFocused) Modifier.border(1.5.dp, AccentGold, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable { onClick() }
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(chevronRotation)
            )
        }
    }
}

@Composable
private fun RadioOptionItem(
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
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isFocused) AccentGoldSoft else Color.Transparent
            )
            .then(
                if (isFocused) Modifier.border(1.dp, AccentGold.copy(0.3f), RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable { onClick() }
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Radio circle
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        if (isSelected) AccentGold else TextMuted,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AccentGold)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = if (isSelected) AccentGoldLight else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
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
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = SurfaceSeparator,
        thickness = 0.5.dp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

// Keep the public TrackOptionItem for backward compat
@Composable
fun TrackOptionItem(
    label: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    RadioOptionItem(label = label, subtitle = subtitle, isSelected = isSelected, onClick = onClick)
}

// -- Track extraction helpers --

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
