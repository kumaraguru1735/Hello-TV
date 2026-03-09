package com.shadow.hellotv.ui.mobile

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.compose.AsyncImage
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.ui.AspectRatioFrameLayout
import com.shadow.hellotv.model.Channel
import com.shadow.hellotv.ui.ExitDialog
import com.shadow.hellotv.ui.ExoPlayerView
import com.shadow.hellotv.ui.theme.*
import com.shadow.hellotv.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs


@Composable
fun MobilePlayerScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var showControls by remember { mutableStateOf(true) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showSessionSheet by remember { mutableStateOf(false) }
    var currentVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    var showVolumeFeedback by remember { mutableStateOf(false) }
    var dragStartY by remember { mutableFloatStateOf(0f) }

    // Auto-hide controls (only in fullscreen, not when channel panel is open)
    var channelPanelOpen by remember { mutableStateOf(false) }
    LaunchedEffect(showControls, vm.isFullscreen, channelPanelOpen) {
        if (showControls && vm.isFullscreen && !channelPanelOpen) {
            delay(5000)
            showControls = false
        }
    }

    // Volume feedback auto-hide
    LaunchedEffect(showVolumeFeedback) {
        if (showVolumeFeedback) { delay(1500); showVolumeFeedback = false }
    }

    // Sync session manager state
    LaunchedEffect(vm.showSessionManager) {
        if (vm.showSessionManager) { showSessionSheet = true; vm.showSessionManager = false }
    }

    if (vm.isFullscreen) {
        // ═══════════════════════════════════════
        // FULLSCREEN LANDSCAPE MODE
        // ═══════════════════════════════════════
        FullscreenPlayer(
            vm = vm,
            audioManager = audioManager,
            showControls = showControls,
            onToggleControls = { showControls = !showControls },
            currentVolume = currentVolume,
            onVolumeChange = { currentVolume = it },
            showVolumeFeedback = showVolumeFeedback,
            onShowVolumeFeedback = { showVolumeFeedback = true },
            dragStartY = dragStartY,
            onDragStartYChange = { dragStartY = it },
            onSettings = { showSettingsSheet = true },
            channelPanelOpen = channelPanelOpen,
            onChannelPanelChange = { channelPanelOpen = it }
        )
    } else {
        // ═══════════════════════════════════════
        // PORTRAIT MODE: Video + Channel Browser
        // ═══════════════════════════════════════
        PortraitPlayer(
            vm = vm,
            onSettings = { showSettingsSheet = true }
        )
    }

    // Exit dialog
    ExitDialog(
        showExitDialog = vm.showExitDialog,
        onDismiss = { vm.showExitDialog = false }
    )

    // Settings bottom sheet
    if (showSettingsSheet) {
        MobileSettingsSheet(vm = vm, onDismiss = { showSettingsSheet = false })
    }

    // Session manager bottom sheet
    if (showSessionSheet) {
        MobileSessionSheet(vm = vm, onDismiss = { showSessionSheet = false })
    }
}

// ═══════════════════════════════════════════════════
// PORTRAIT MODE
// ═══════════════════════════════════════════════════

@Composable
private fun PortraitPlayer(
    vm: MainViewModel,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val channelListState = rememberLazyListState()
    var showVideoOverlay by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    // Scroll to selected channel
    LaunchedEffect(vm.selectedChannelIndex) {
        if (vm.channels.isNotEmpty()) {
            channelListState.animateScrollToItem(vm.selectedChannelIndex.coerceIn(0, vm.channels.size - 1))
        }
    }

    // Auto-hide video overlay
    LaunchedEffect(showVideoOverlay) {
        if (showVideoOverlay) { delay(4000); showVideoOverlay = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfacePrimary)
            .statusBarsPadding()
    ) {
        // ── Search bar + Profile ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search box
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceInput)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = vm.searchQuery,
                    onValueChange = { vm.searchQuery = it; vm.applyFilters() },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(AccentGold),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box {
                            if (vm.searchQuery.isEmpty()) {
                                Text(
                                    vm.channels.getOrNull(vm.selectedChannelIndex)?.name ?: "Search channels",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (vm.searchQuery.isNotEmpty()) {
                    Text(
                        "Clear",
                        color = AccentGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { vm.searchQuery = ""; vm.applyFilters() }
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Profile icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AccentGold.copy(alpha = 0.15f))
                    .border(1.dp, AccentGold.copy(alpha = 0.3f), CircleShape)
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = AccentGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Video player area (16:9 aspect) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(PlayerBackground)
                .clickable { showVideoOverlay = !showVideoOverlay }
        ) {
            vm.channels.getOrNull(vm.selectedChannelIndex)?.let { channel ->
                ExoPlayerView(
                    channel = channel,
                    onPlayerReady = { vm.currentExoPlayer = it },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Overlay: mute (bottom-left) + fullscreen (bottom-right)
            androidx.compose.animation.AnimatedVisibility(
                visible = showVideoOverlay,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PlayerOverlay.copy(alpha = 0.3f))
                ) {
                    // Center: play/pause with scale animation
                    var playTapped by remember { mutableStateOf(false) }
                    val playScale by animateFloatAsState(
                        targetValue = if (playTapped) 0.85f else 1f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 800f),
                        label = "playScale",
                        finishedListener = { playTapped = false }
                    )
                    IconButton(
                        onClick = {
                            playTapped = true
                            vm.currentExoPlayer?.let { player ->
                                player.playWhenReady = !player.playWhenReady
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PlayerControlBg)
                            .graphicsLayer(scaleX = playScale, scaleY = playScale)
                    ) {
                        Icon(
                            if (vm.currentExoPlayer?.playWhenReady == true) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Bottom-left: mute/unmute with animated tint
                    val muteTint by animateColorAsState(
                        targetValue = if (isMuted) StatusLive else TextPrimary,
                        animationSpec = tween(300),
                        label = "muteTint"
                    )
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            vm.currentExoPlayer?.volume = if (isMuted) 0f else 1f
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PlayerControlBg)
                    ) {
                        Icon(
                            if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = muteTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Bottom-right: fullscreen with circle bg
                    IconButton(
                        onClick = { vm.isFullscreen = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PlayerControlBg)
                    ) {
                        Icon(
                            Icons.Rounded.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // ── Language chips (horizontal scroll, bordered style) ──
        if (vm.languages.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
                item {
                    LanguageChip("All", vm.selectedLanguageId == null) {
                        vm.selectedLanguageId = null; vm.applyFilters()
                    }
                }
                items(vm.languages) { lang ->
                    LanguageChip(
                        lang.name.lowercase().replaceFirstChar { it.uppercase() },
                        vm.selectedLanguageId == lang.id
                    ) {
                        vm.selectedLanguageId = if (vm.selectedLanguageId == lang.id) null else lang.id
                        vm.applyFilters()
                    }
                }
            }
        }

        // ── Categories (left) + Channels (right) ──
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: categories
            if (vm.categories.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight()
                        .background(SurfacePrimary),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        CategoryItem("All", vm.selectedCategoryId == null) {
                            vm.selectedCategoryId = null; vm.applyFilters()
                        }
                    }
                    items(vm.categories) { cat ->
                        CategoryItem(cat.name, vm.selectedCategoryId == cat.id) {
                            vm.selectedCategoryId = if (vm.selectedCategoryId == cat.id) null else cat.id
                            vm.applyFilters()
                        }
                    }
                }
            }

            // Right: channels
            LazyColumn(
                state = channelListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                itemsIndexed(vm.channels) { index, channel ->
                    PortraitChannelRow(
                        channel = channel,
                        isSelected = index == vm.selectedChannelIndex,
                        onClick = { vm.selectChannel(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val chipBg by animateColorAsState(
        targetValue = if (selected) AccentGold else Color.Transparent,
        animationSpec = tween(300), label = "chipBg"
    )
    val chipTextColor by animateColorAsState(
        targetValue = if (selected) Color.Black else TextSecondary,
        animationSpec = tween(300), label = "chipText"
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .then(
                if (selected) Modifier
                    .background(chipBg)
                else Modifier
                    .border(1.dp, SurfaceSeparator, RoundedCornerShape(50))
                    .background(Color.Transparent)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = chipTextColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun CategoryItem(name: String, selected: Boolean, onClick: () -> Unit) {
    val displayName = if (name.all { it.isUpperCase() || !it.isLetter() } && name.length <= 3) name
        else name.lowercase().replaceFirstChar { it.uppercase() }

    val accentColor by animateColorAsState(
        targetValue = if (selected) AccentGold else Color.Transparent,
        animationSpec = tween(300), label = "catAccent"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) TextPrimary else TextMuted,
        animationSpec = tween(300), label = "catText"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gold left border indicator (3dp, rounded)
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .background(
                    accentColor,
                    RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )
        Text(
            displayName,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun PortraitChannelRow(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val rowBg by animateColorAsState(
        targetValue = if (isSelected) AccentGold.copy(alpha = 0.10f) else SurfaceCard,
        animationSpec = tween(300), label = "portraitRowBg"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(rowBg)
            .then(
                if (isSelected) Modifier.border(1.dp, AccentGold, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel logo
        if (channel.image.isNotEmpty()) {
            AsyncImage(
                model = channel.image,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LiveTv, null, tint = TextMuted, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            channel.name,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        // Gold star / equalizer icon
        Icon(
            if (isSelected) Icons.Rounded.Equalizer else Icons.Rounded.Star,
            contentDescription = null,
            tint = AccentGold,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ═══════════════════════════════════════════════════
// FULLSCREEN LANDSCAPE MODE
// ═══════════════════════════════════════════════════

@Composable
private fun FullscreenPlayer(
    vm: MainViewModel,
    audioManager: AudioManager,
    showControls: Boolean,
    onToggleControls: () -> Unit,
    currentVolume: Int,
    onVolumeChange: (Int) -> Unit,
    showVolumeFeedback: Boolean,
    onShowVolumeFeedback: () -> Unit,
    dragStartY: Float,
    onDragStartYChange: (Float) -> Unit,
    onSettings: () -> Unit,
    channelPanelOpen: Boolean,
    onChannelPanelChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isLocked by remember { mutableStateOf(false) }
    var showQualityPanel by remember { mutableStateOf(false) }
    var showAudioPanel by remember { mutableStateOf(false) }
    var isFitMode by remember { mutableStateOf(true) } // true = fit, false = zoom/fill
    var showBrightnessFeedback by remember { mutableStateOf(false) }
    var currentBrightness by remember {
        mutableFloatStateOf(
            activity?.window?.attributes?.screenBrightness?.let { if (it < 0) 0.5f else it } ?: 0.5f
        )
    }

    // Auto-hide brightness feedback
    LaunchedEffect(showBrightnessFeedback) {
        if (showBrightnessFeedback) { delay(1500); showBrightnessFeedback = false }
    }

    Box(modifier = Modifier.fillMaxSize().background(PlayerBackground)) {
        // Video player
        vm.channels.getOrNull(vm.selectedChannelIndex)?.let { channel ->
            ExoPlayerView(
                channel = channel,
                onPlayerReady = { vm.currentExoPlayer = it },
                resizeMode = if (isFitMode) AspectRatioFrameLayout.RESIZE_MODE_FIT
                    else AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                // Tap toggles controls (unlock button when locked, full controls when unlocked)
                                onToggleControls()
                            },
                            onDoubleTap = { offset ->
                                if (!isLocked) {
                                    if (offset.x < size.width / 2) vm.previousChannel()
                                    else vm.nextChannel()
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> onDragStartYChange(offset.y) },
                            onDragEnd = {}
                        ) { change, _ ->
                            if (isLocked) return@detectDragGestures
                            val deltaY = change.position.y - dragStartY
                            val isLeftSide = change.position.x < size.width / 2

                            if (abs(deltaY) > 60) {
                                if (isLeftSide) {
                                    // Left side: brightness
                                    val delta = if (deltaY < 0) 0.03f else -0.03f
                                    currentBrightness = (currentBrightness + delta).coerceIn(0.01f, 1f)
                                    activity?.window?.attributes = activity.window.attributes.apply {
                                        screenBrightness = currentBrightness
                                    }
                                    showBrightnessFeedback = true
                                } else {
                                    // Right side: volume
                                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val newVol = if (deltaY < 0) (currentVolume + 1).coerceAtMost(maxVol)
                                    else (currentVolume - 1).coerceAtLeast(0)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    onVolumeChange(newVol)
                                    onShowVolumeFeedback()
                                }
                                onDragStartYChange(change.position.y)
                            }
                        }
                    }
            )
        }

        // Lock overlay - show unlock button at bottom center
        if (isLocked) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(PlayerControlBg)
                        .clickable { isLocked = false }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val lockPulse by rememberInfiniteTransition(label = "lockPulse").animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "lockPulseScale"
                    )
                    Icon(
                        Icons.Rounded.LockOpen, "Unlock", tint = AccentGold,
                        modifier = Modifier.size(22.dp).graphicsLayer(scaleX = lockPulse, scaleY = lockPulse)
                    )
                    Text("Tap to Unlock", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        if (!isLocked) {
            // ── TOP BAR ──
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val topBarIconTint by animateColorAsState(
                        targetValue = TextPrimary,
                        animationSpec = tween(300),
                        label = "topBarIconTint"
                    )
                    IconButton(onClick = { vm.isFullscreen = false }) {
                        Icon(Icons.Rounded.ArrowBack, "Back", tint = topBarIconTint, modifier = Modifier.size(24.dp))
                    }
                    vm.channels.getOrNull(vm.selectedChannelIndex)?.let { channel ->
                        Text(
                            channel.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    } ?: Spacer(Modifier.weight(1f))
                    // Screen fit/zoom toggle
                    IconButton(onClick = { isFitMode = !isFitMode }) {
                        Icon(
                            if (isFitMode) Icons.Rounded.FitScreen else Icons.Rounded.Fullscreen,
                            if (isFitMode) "Zoom to Fill" else "Fit to Screen",
                            tint = topBarIconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { vm.isFullscreen = false }) {
                        Icon(Icons.Rounded.FullscreenExit, "Exit Fullscreen", tint = topBarIconTint, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { /* cast placeholder */ }) {
                        Icon(Icons.Rounded.Cast, "Cast", tint = topBarIconTint, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // ── CENTER: 10s Rewind | Play/Pause | 10s Forward ──
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 10s Rewind
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                vm.currentExoPlayer?.let { p ->
                                    p.seekTo((p.currentPosition - 10000).coerceAtLeast(0))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Replay, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("10s", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Play/Pause - large with scale animation
                    var fsPlayTapped by remember { mutableStateOf(false) }
                    val fsPlayScale by animateFloatAsState(
                        targetValue = if (fsPlayTapped) 0.85f else 1f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 800f),
                        label = "fsPlayScale",
                        finishedListener = { fsPlayTapped = false }
                    )
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer(scaleX = fsPlayScale, scaleY = fsPlayScale)
                            .clickable {
                                fsPlayTapped = true
                                vm.currentExoPlayer?.let { player ->
                                    player.playWhenReady = !player.playWhenReady
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (vm.currentExoPlayer?.playWhenReady == true) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    // 10s Forward
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                vm.currentExoPlayer?.let { p ->
                                    p.seekTo(p.currentPosition + 10000)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Forward10, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("10s", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── BOTTOM BAR ──
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                val currentTime = remember { SimpleDateFormat("H:mm:ss", Locale.getDefault()) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                        .navigationBarsPadding()
                ) {
                    // Seek bar row: timestamp | progress bar | timestamp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            currentTime.format(Date()),
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        // Gold progress bar (full = LIVE)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(PlayerSeekBarBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(PlayerSeekBar)
                            )
                            // Thumb circle with shadow
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(12.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(AccentGold)
                            )
                        }
                        Text(
                            currentTime.format(Date()),
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    // Bottom buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FullscreenBottomButton(Icons.Rounded.HighQuality, "Quality") {
                            showQualityPanel = !showQualityPanel; showAudioPanel = false
                        }
                        FullscreenBottomButton(Icons.Rounded.Lock, "Lock") { isLocked = true; onToggleControls() }
                        FullscreenBottomButton(Icons.Rounded.Audiotrack, "Audio") {
                            showAudioPanel = !showAudioPanel; showQualityPanel = false
                        }
                        FullscreenBottomButton(Icons.Rounded.LiveTv, "Channels") {
                            onChannelPanelChange(!channelPanelOpen); showQualityPanel = false; showAudioPanel = false
                        }
                    }
                }
            }
        }

        // ── BRIGHTNESS FEEDBACK (left side) ──
        AnimatedVisibility(
            visible = showBrightnessFeedback,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PlayerControlBg)
                    .padding(12.dp)
            ) {
                val brightPulse by rememberInfiniteTransition(label = "brightPulse").animateFloat(
                    initialValue = 1f, targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                    label = "brightPulseScale"
                )
                Icon(Icons.Rounded.BrightnessHigh, null, tint = AccentGold,
                    modifier = Modifier.size(28.dp).graphicsLayer(scaleX = brightPulse, scaleY = brightPulse))
                Spacer(Modifier.height(4.dp))
                val animatedBrightness by animateFloatAsState(
                    targetValue = currentBrightness * 100f,
                    animationSpec = tween(200),
                    label = "animBrightness"
                )
                Text("${animatedBrightness.toInt()}%", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── VOLUME FEEDBACK (right side) ──
        AnimatedVisibility(
            visible = showVolumeFeedback,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)
        ) {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PlayerControlBg)
                    .padding(12.dp)
            ) {
                val volPulse by rememberInfiniteTransition(label = "volPulse").animateFloat(
                    initialValue = 1f, targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                    label = "volPulseScale"
                )
                Icon(
                    if (currentVolume == 0) Icons.Rounded.VolumeOff
                    else if (currentVolume < maxVol / 2) Icons.Rounded.VolumeDown
                    else Icons.Rounded.VolumeUp,
                    null, tint = AccentGold,
                    modifier = Modifier.size(28.dp).graphicsLayer(scaleX = volPulse, scaleY = volPulse)
                )
                Spacer(Modifier.height(4.dp))
                val animatedVolume by animateFloatAsState(
                    targetValue = currentVolume * 100f / maxVol,
                    animationSpec = tween(200),
                    label = "animVolume"
                )
                Text("${animatedVolume.toInt()}%", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── QUALITY PANEL (floating cards, upper-right like reference) ──
        if (showQualityPanel && !isLocked) {
            Box(modifier = Modifier.fillMaxSize().clickable { showQualityPanel = false })
        }
        AnimatedVisibility(
            visible = showQualityPanel && !isLocked,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 4 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 }),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(top = 48.dp, end = 16.dp, bottom = 80.dp)
                    .fillMaxWidth(0.42f)
                    .verticalScroll(rememberScrollState())
            ) {
                QualityPanel(
                    player = vm.currentExoPlayer,
                    onDismiss = { showQualityPanel = false }
                )
            }
        }

        // ── AUDIO PANEL (floating cards, upper-right like reference) ──
        if (showAudioPanel && !isLocked) {
            Box(modifier = Modifier.fillMaxSize().clickable { showAudioPanel = false })
        }
        AnimatedVisibility(
            visible = showAudioPanel && !isLocked,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 4 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 }),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(top = 48.dp, end = 16.dp, bottom = 80.dp)
                    .fillMaxWidth(0.42f)
                    .verticalScroll(rememberScrollState())
            ) {
                AudioPanel(
                    player = vm.currentExoPlayer,
                    onDismiss = { showAudioPanel = false }
                )
            }
        }

        // ── CHANNEL PANEL (right side slide-in) - stays open until dismissed ──
        if (channelPanelOpen && !isLocked) {
            // Tap outside (video area) to close
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onChannelPanelChange(false) }
            )
        }
        AnimatedVisibility(
            visible = channelPanelOpen && !isLocked,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            FullscreenChannelPanel(
                vm = vm,
                onChannelSelected = { index ->
                    vm.selectChannel(index)
                    onChannelPanelChange(false)
                },
                onDismiss = { onChannelPanelChange(false) }
            )
        }
    }
}

@Composable
private fun FullscreenBottomButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (active) AccentGold else TextSecondary,
        animationSpec = tween(300),
        label = "bottomBtnTint"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, color = tint, fontSize = 10.sp)
    }
}

// ═══════════════════════════════════════════════════
// FULLSCREEN CHANNEL PANEL (right side)
// ═══════════════════════════════════════════════════

@Composable
private fun FullscreenChannelPanel(
    vm: MainViewModel,
    onChannelSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val channelListState = rememberLazyListState()

    LaunchedEffect(vm.selectedChannelIndex) {
        if (vm.channels.isNotEmpty()) {
            channelListState.animateScrollToItem(vm.selectedChannelIndex.coerceIn(0, vm.channels.size - 1))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.58f)
            .background(SurfaceDark.copy(alpha = 0.96f))
            .padding(top = 8.dp, bottom = 8.dp, start = 2.dp, end = 2.dp)
    ) {
        // Language tabs at top
        if (vm.languages.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                item {
                    LanguageChip("All", vm.selectedLanguageId == null) {
                        vm.selectedLanguageId = null; vm.applyFilters()
                    }
                }
                items(vm.languages) { lang ->
                    LanguageChip(
                        lang.name.lowercase().replaceFirstChar { it.uppercase() },
                        vm.selectedLanguageId == lang.id
                    ) {
                        vm.selectedLanguageId = if (vm.selectedLanguageId == lang.id) null else lang.id
                        vm.applyFilters()
                    }
                }
            }
        }

        // Categories + Channels
        Row(modifier = Modifier.fillMaxSize()) {
            // Categories column with gold left border
            if (vm.categories.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    item {
                        PanelCategoryItem("All", vm.selectedCategoryId == null) {
                            vm.selectedCategoryId = null; vm.applyFilters()
                        }
                    }
                    items(vm.categories) { cat ->
                        PanelCategoryItem(
                            if (cat.name.all { it.isUpperCase() || !it.isLetter() } && cat.name.length <= 3) cat.name
                            else cat.name.lowercase().replaceFirstChar { it.uppercase() },
                            vm.selectedCategoryId == cat.id
                        ) {
                            vm.selectedCategoryId = if (vm.selectedCategoryId == cat.id) null else cat.id
                            vm.applyFilters()
                        }
                    }
                }
            }

            // Channels list with card rows
            LazyColumn(
                state = channelListState,
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .padding(start = 4.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                itemsIndexed(vm.channels) { index, channel ->
                    val isSelected = index == vm.selectedChannelIndex
                    val rowBg by animateColorAsState(
                        targetValue = if (isSelected) AccentGold.copy(alpha = 0.10f) else SurfaceCard,
                        animationSpec = tween(300), label = "panelRowBg"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(rowBg)
                            .then(
                                if (isSelected) Modifier.border(1.dp, AccentGold, RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .clickable { onChannelSelected(index) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .animateItem(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (channel.image.isNotEmpty()) {
                            AsyncImage(
                                model = channel.image,
                                contentDescription = channel.name,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceCard)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.LiveTv, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            channel.name,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            if (isSelected) Icons.Rounded.Equalizer else Icons.Rounded.Star,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelCategoryItem(name: String, selected: Boolean, onClick: () -> Unit) {
    val accentColor by animateColorAsState(
        targetValue = if (selected) AccentGold else Color.Transparent,
        animationSpec = tween(300), label = "panelCatAccent"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) TextPrimary else TextMuted,
        animationSpec = tween(300), label = "panelCatText"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gold left border indicator (3dp, rounded)
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(24.dp)
                .background(
                    accentColor,
                    RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )
        Text(
            name,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp)
        )
    }
}

// ═══════════════════════════════════════════════════
// QUALITY PANEL - emits cards directly into parent Column
// ═══════════════════════════════════════════════════

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private fun QualityPanel(
    player: androidx.media3.exoplayer.ExoPlayer?,
    onDismiss: () -> Unit
) {
    val tracks = player?.currentTracks ?: Tracks.EMPTY
    val videoTracks = mutableListOf<VideoTrackInfo>()

    for (group in tracks.groups) {
        if (group.type == C.TRACK_TYPE_VIDEO) {
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                videoTracks.add(VideoTrackInfo(
                    width = format.width, height = format.height,
                    bitrate = format.bitrate, isSelected = group.isTrackSelected(i),
                    groupIndex = tracks.groups.indexOf(group), trackIndex = i
                ))
            }
        }
    }

    val isAuto = player?.trackSelectionParameters?.overrides?.none {
        it.value.type == C.TRACK_TYPE_VIDEO
    } != false

    // Panel title
    Text("Quality", color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 4.dp))

    // Auto option
    TrackOptionCard(
        label = "Auto" + if (isAuto && videoTracks.isNotEmpty()) {
            videoTracks.firstOrNull { it.isSelected }?.let { " (${it.width}×${it.height})" } ?: ""
        } else "",
        isSelected = isAuto,
        onClick = {
            player?.trackSelectionParameters = player?.trackSelectionParameters
                ?.buildUpon()?.clearOverridesOfType(C.TRACK_TYPE_VIDEO)?.build() ?: return@TrackOptionCard
            onDismiss()
        }
    )

    // Each quality
    videoTracks.sortedByDescending { it.height }.forEach { track ->
        Spacer(Modifier.height(8.dp))
        TrackOptionCard(
            label = "${track.width}×${track.height}" + if (track.bitrate > 0) " | ${track.bitrate / 1000} kbps" else "",
            isSelected = track.isSelected && !isAuto,
            onClick = {
                val trackGroups = player?.currentTracks?.groups ?: return@TrackOptionCard
                if (track.groupIndex in trackGroups.indices) {
                    val group = trackGroups[track.groupIndex]
                    player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                        .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(track.trackIndex)))
                        .build()
                }
                onDismiss()
            }
        )
    }

    if (videoTracks.isEmpty()) {
        Spacer(Modifier.height(8.dp))
        TrackOptionCard(label = "No video tracks", onClick = {})
    }
}

// ═══════════════════════════════════════════════════
// AUDIO PANEL - emits cards directly into parent Column
// ═══════════════════════════════════════════════════

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private fun AudioPanel(
    player: androidx.media3.exoplayer.ExoPlayer?,
    onDismiss: () -> Unit
) {
    val tracks = player?.currentTracks ?: Tracks.EMPTY
    val audioTracks = mutableListOf<AudioTrackInfo>()
    val subtitleTracks = mutableListOf<AudioTrackInfo>()

    for (group in tracks.groups) {
        if (group.type == C.TRACK_TYPE_AUDIO) {
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                audioTracks.add(AudioTrackInfo(
                    label = format.label ?: "Track ${audioTracks.size + 1}",
                    language = format.language ?: "", channels = format.channelCount,
                    bitrate = format.bitrate, isSelected = group.isTrackSelected(i),
                    groupIndex = tracks.groups.indexOf(group), trackIndex = i
                ))
            }
        }
        if (group.type == C.TRACK_TYPE_TEXT) {
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                subtitleTracks.add(AudioTrackInfo(
                    label = format.label ?: format.language ?: "Subtitle ${subtitleTracks.size + 1}",
                    language = format.language ?: "", channels = 0, bitrate = 0,
                    isSelected = group.isTrackSelected(i),
                    groupIndex = tracks.groups.indexOf(group), trackIndex = i
                ))
            }
        }
    }

    // Audio title
    Text("Audio", color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 4.dp))

    audioTracks.forEachIndexed { i, track ->
        if (i > 0) Spacer(Modifier.height(8.dp))
        val label = (if (track.language.isNotEmpty()) "${track.label} (${track.language.uppercase()})" else track.label) +
                (if (track.channels > 0) " | ${track.channels}ch" else "") +
                (if (track.bitrate > 0) " | ${track.bitrate / 1000}kbps" else "")
        TrackOptionCard(label = label, isSelected = track.isSelected, onClick = {
            val trackGroups = player?.currentTracks?.groups ?: return@TrackOptionCard
            if (track.groupIndex in trackGroups.indices) {
                val group = trackGroups[track.groupIndex]
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(track.trackIndex)))
                    .build()
            }
            onDismiss()
        })
    }

    if (subtitleTracks.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text("Subtitles", color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp))
    }

    subtitleTracks.forEachIndexed { i, track ->
        if (i > 0) Spacer(Modifier.height(8.dp))
        TrackOptionCard(
            label = track.label + if (track.language.isNotEmpty()) " (${track.language.uppercase()})" else "",
            isSelected = track.isSelected,
            onClick = {
                val trackGroups = player?.currentTracks?.groups ?: return@TrackOptionCard
                if (track.groupIndex in trackGroups.indices) {
                    val group = trackGroups[track.groupIndex]
                    player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                        .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(track.trackIndex)))
                        .build()
                }
                onDismiss()
            }
        )
    }

    if (audioTracks.isEmpty() && subtitleTracks.isEmpty()) {
        TrackOptionCard(label = "No tracks available", onClick = {})
    }
}

// Track option card with radio-style selection, rounded 16dp, subtle border
@Composable
private fun TrackOptionCard(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val radioColor by animateColorAsState(
        targetValue = if (isSelected) AccentGold else TextMuted,
        animationSpec = tween(300),
        label = "radioColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) AccentGold else TextSecondary,
        animationSpec = tween(300),
        label = "trackTextColor"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PlayerControlBg)
            .border(0.5.dp, SurfaceSeparator.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radio circle
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(1.5.dp, radioColor, CircleShape),
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
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class VideoTrackInfo(
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val isSelected: Boolean,
    val groupIndex: Int,
    val trackIndex: Int
)

private data class AudioTrackInfo(
    val label: String,
    val language: String,
    val channels: Int,
    val bitrate: Int,
    val isSelected: Boolean,
    val groupIndex: Int,
    val trackIndex: Int
)
