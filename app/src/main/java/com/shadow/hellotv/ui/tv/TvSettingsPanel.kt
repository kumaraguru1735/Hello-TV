package com.shadow.hellotv.ui.tv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

private enum class ExpandedSection {
    NONE, QUALITY, AUDIO, SUBTITLES, RESIZE
}

private enum class ResizeMode(val label: String) {
    FIT("Fit"),
    ZOOM("Zoom"),
    FILL("Fill")
}

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
    var expandedSection by remember { mutableStateOf(ExpandedSection.NONE) }
    var currentResizeMode by remember { mutableStateOf(ResizeMode.FIT) }

    val tracks = exoPlayer?.currentTracks ?: Tracks.EMPTY
    val videoGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_VIDEO }
    val audioGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
    val subtitleGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }

    // Derive current values for display
    val currentQuality = remember(tracks) {
        val selectedVideo = videoGroups.firstNotNullOfOrNull { group ->
            (0 until group.length).firstOrNull { group.isTrackSelected(it) }
                ?.let { group.getTrackFormat(it) }
        }
        selectedVideo?.let { "${it.height}p" } ?: "Auto"
    }

    val currentAudio = remember(tracks) {
        val selectedAudio = audioGroups.firstNotNullOfOrNull { group ->
            (0 until group.length).firstOrNull { group.isTrackSelected(it) }
                ?.let { group.getTrackFormat(it) }
        }
        selectedAudio?.let { fmt ->
            fmt.label ?: fmt.language?.uppercase() ?: "Default"
        } ?: "Default"
    }

    val currentSubtitle = remember(tracks) {
        val selectedSub = subtitleGroups.firstNotNullOfOrNull { group ->
            (0 until group.length).firstOrNull { group.isTrackSelected(it) }
                ?.let { group.getTrackFormat(it) }
        }
        selectedSub?.let { fmt ->
            fmt.label ?: fmt.language?.uppercase() ?: "On"
        } ?: "Off"
    }

    Box(
        modifier = modifier
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        TvSurface.copy(alpha = 0.85f),
                        TvSurface.copy(alpha = 0.96f),
                        TvSurface
                    ),
                    startX = 0f,
                    endX = 400f
                )
            )
            .padding(top = 20.dp, start = 8.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Settings title
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 20.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // ── Profile section ──
                item {
                    ProfileRow(subscriber, subscriptionInfo)
                }

                item { SettingsDivider() }

                // ── Quality row ──
                item {
                    SettingsRow(
                        icon = Icons.Default.HighQuality,
                        title = "Quality",
                        value = currentQuality,
                        isExpanded = expandedSection == ExpandedSection.QUALITY,
                        onClick = {
                            expandedSection = if (expandedSection == ExpandedSection.QUALITY)
                                ExpandedSection.NONE else ExpandedSection.QUALITY
                        }
                    )
                }

                // Quality sub-options
                item {
                    AnimatedVisibility(
                        visible = expandedSection == ExpandedSection.QUALITY,
                        enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                        exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
                    ) {
                        Column(modifier = Modifier.padding(start = 40.dp, end = 8.dp)) {
                            // Auto option
                            val hasOverride = exoPlayer?.trackSelectionParameters
                                ?.overrides?.any { it.key.type == C.TRACK_TYPE_VIDEO } == true
                            TrackSubItem(
                                label = "Auto",
                                detail = "Adaptive",
                                isSelected = !hasOverride,
                                onClick = {
                                    exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                        ?.buildUpon()
                                        ?.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                        ?.build() ?: return@TrackSubItem
                                }
                            )
                            videoGroups.forEach { group ->
                                for (trackIdx in 0 until group.length) {
                                    val format = group.getTrackFormat(trackIdx)
                                    val isSelected = group.isTrackSelected(trackIdx)
                                    TrackSubItem(
                                        label = "${format.height}p",
                                        detail = "${format.width}x${format.height} · ${format.bitrate / 1000}kbps",
                                        isSelected = isSelected,
                                        onClick = {
                                            exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                                ?.buildUpon()
                                                ?.setOverrideForType(
                                                    androidx.media3.common.TrackSelectionOverride(
                                                        group.mediaTrackGroup, trackIdx
                                                    )
                                                )
                                                ?.build() ?: return@TrackSubItem
                                        }
                                    )
                                }
                            }
                            if (videoGroups.isEmpty()) {
                                Text(
                                    "No video tracks available",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // ── Audio row ──
                item {
                    SettingsRow(
                        icon = Icons.Default.Audiotrack,
                        title = "Audio",
                        value = currentAudio,
                        isExpanded = expandedSection == ExpandedSection.AUDIO,
                        onClick = {
                            expandedSection = if (expandedSection == ExpandedSection.AUDIO)
                                ExpandedSection.NONE else ExpandedSection.AUDIO
                        }
                    )
                }

                // Audio sub-options
                item {
                    AnimatedVisibility(
                        visible = expandedSection == ExpandedSection.AUDIO,
                        enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                        exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
                    ) {
                        Column(modifier = Modifier.padding(start = 40.dp, end = 8.dp)) {
                            audioGroups.forEach { group ->
                                for (trackIdx in 0 until group.length) {
                                    val format = group.getTrackFormat(trackIdx)
                                    val isSelected = group.isTrackSelected(trackIdx)
                                    TrackSubItem(
                                        label = format.label
                                            ?: format.language?.uppercase()
                                            ?: "Track ${trackIdx + 1}",
                                        detail = "${format.channelCount}ch · ${format.sampleRate / 1000}kHz",
                                        isSelected = isSelected,
                                        onClick = {
                                            exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                                ?.buildUpon()
                                                ?.setOverrideForType(
                                                    androidx.media3.common.TrackSelectionOverride(
                                                        group.mediaTrackGroup, trackIdx
                                                    )
                                                )
                                                ?.build() ?: return@TrackSubItem
                                        }
                                    )
                                }
                            }
                            if (audioGroups.isEmpty()) {
                                Text(
                                    "No audio tracks available",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // ── Subtitles row ──
                item {
                    SettingsRow(
                        icon = Icons.Default.ClosedCaption,
                        title = "Subtitles",
                        value = currentSubtitle,
                        isExpanded = expandedSection == ExpandedSection.SUBTITLES,
                        onClick = {
                            expandedSection = if (expandedSection == ExpandedSection.SUBTITLES)
                                ExpandedSection.NONE else ExpandedSection.SUBTITLES
                        }
                    )
                }

                // Subtitle sub-options
                item {
                    AnimatedVisibility(
                        visible = expandedSection == ExpandedSection.SUBTITLES,
                        enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                        exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
                    ) {
                        Column(modifier = Modifier.padding(start = 40.dp, end = 8.dp)) {
                            // Off option
                            val hasSubOverride = subtitleGroups.any { group ->
                                (0 until group.length).any { group.isTrackSelected(it) }
                            }
                            TrackSubItem(
                                label = "Off",
                                detail = "Disable subtitles",
                                isSelected = !hasSubOverride,
                                onClick = {
                                    exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                        ?.buildUpon()
                                        ?.clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                        ?.setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT.inv())
                                        ?.build() ?: return@TrackSubItem
                                }
                            )
                            subtitleGroups.forEach { group ->
                                for (trackIdx in 0 until group.length) {
                                    val format = group.getTrackFormat(trackIdx)
                                    val isSelected = group.isTrackSelected(trackIdx)
                                    TrackSubItem(
                                        label = format.label
                                            ?: format.language?.uppercase()
                                            ?: "Subtitle ${trackIdx + 1}",
                                        detail = format.language ?: "",
                                        isSelected = isSelected,
                                        onClick = {
                                            exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                                ?.buildUpon()
                                                ?.setOverrideForType(
                                                    androidx.media3.common.TrackSelectionOverride(
                                                        group.mediaTrackGroup, trackIdx
                                                    )
                                                )
                                                ?.build() ?: return@TrackSubItem
                                        }
                                    )
                                }
                            }
                            if (subtitleGroups.isEmpty()) {
                                Text(
                                    "No subtitles available",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // ── Resize row ──
                item {
                    SettingsRow(
                        icon = Icons.Default.AspectRatio,
                        title = "Resize",
                        value = currentResizeMode.label,
                        isExpanded = expandedSection == ExpandedSection.RESIZE,
                        onClick = {
                            expandedSection = if (expandedSection == ExpandedSection.RESIZE)
                                ExpandedSection.NONE else ExpandedSection.RESIZE
                        }
                    )
                }

                // Resize sub-options
                item {
                    AnimatedVisibility(
                        visible = expandedSection == ExpandedSection.RESIZE,
                        enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                        exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
                    ) {
                        Column(modifier = Modifier.padding(start = 40.dp, end = 8.dp)) {
                            ResizeMode.entries.forEach { mode ->
                                TrackSubItem(
                                    label = mode.label,
                                    detail = when (mode) {
                                        ResizeMode.FIT -> "Fit within screen"
                                        ResizeMode.ZOOM -> "Zoom to fill, may crop"
                                        ResizeMode.FILL -> "Stretch to fill"
                                    },
                                    isSelected = currentResizeMode == mode,
                                    onClick = {
                                        currentResizeMode = mode
                                        exoPlayer?.videoScalingMode = when (mode) {
                                            ResizeMode.FIT -> C.VIDEO_SCALING_MODE_DEFAULT
                                            ResizeMode.ZOOM -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                                            ResizeMode.FILL -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                item { SettingsDivider() }

                // ── Sessions row ──
                item {
                    SettingsRow(
                        icon = Icons.Default.Devices,
                        title = "Sessions",
                        value = "Manage",
                        isExpanded = false,
                        onClick = onManageSessions
                    )
                }

                // ── Logout row ──
                item {
                    LogoutRow(onLogout)
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(
    subscriber: Subscriber?,
    subscriptionInfo: SubscriptionInfo?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AccentGold.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subscriber?.name ?: "User",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusText = subscriptionInfo?.status ?: "Active"
                val statusColor = if (statusText.equals("active", ignoreCase = true))
                    StatusSuccess else StatusLive
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                subscriptionInfo?.expiredIn?.let {
                    Text(
                        text = " · $it",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        thickness = 0.5.dp,
        color = TvSeparator
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderWidth by animateDpAsState(
        targetValue = when {
            isFocused -> 1.5.dp
            isExpanded -> 1.dp
            else -> 0.dp
        },
        animationSpec = tween(150),
        label = "borderWidth"
    )
    val borderColor = when {
        isFocused -> AccentGold
        isExpanded -> AccentGold.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    val bgColor = when {
        isFocused -> AccentGoldSoft
        isExpanded -> AccentGold.copy(alpha = 0.06f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .then(
                if (borderWidth > 0.dp) Modifier.border(
                    borderWidth,
                    borderColor,
                    RoundedCornerShape(10.dp)
                ) else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFocused) AccentGold else TextMuted,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(14.dp))

        Text(
            text = title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = if (isFocused) AccentGoldLight else TextSecondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown
            else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun TrackSubItem(
    label: String,
    detail: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFocused -> AccentGoldSoft
                    else -> Color.Transparent
                }
            )
            .then(
                if (isFocused) Modifier.border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radio indicator
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 5.dp else 1.5.dp,
                    color = if (isSelected) AccentGold
                    else if (isFocused) AccentGold.copy(alpha = 0.5f)
                    else TextMuted.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = if (isSelected) AccentGoldLight else TextPrimary,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            if (detail.isNotEmpty()) {
                Text(
                    text = detail,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun LogoutRow(onLogout: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isFocused) StatusLive.copy(alpha = 0.12f) else Color.Transparent
            )
            .then(
                if (isFocused) Modifier.border(1.5.dp, StatusLive.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onLogout() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Logout,
            contentDescription = null,
            tint = StatusLive,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(14.dp))

        Text(
            text = "Sign Out",
            color = StatusLive,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
