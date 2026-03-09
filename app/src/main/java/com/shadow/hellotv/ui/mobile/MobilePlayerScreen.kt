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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// Accent color matching reference design
private val AccentGold = Color(0xFFFFB800)
private val AccentGoldDark = Color(0xFFE5A500)
private val SurfaceDark = Color(0xFF121212)
private val SurfaceCard = Color(0xFF1A1A2E)
private val SurfaceSeparator = Color(0xFF2A2A3E)

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
            .background(SurfaceDark)
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
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.5f),
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
                                    color = Color.White.copy(alpha = 0.4f),
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
                .background(Color.Black)
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
                        .background(Color.Black.copy(alpha = 0.2f))
                ) {
                    // Center: play/pause
                    IconButton(
                        onClick = {
                            vm.currentExoPlayer?.let { player ->
                                player.playWhenReady = !player.playWhenReady
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                    ) {
                        Icon(
                            if (vm.currentExoPlayer?.playWhenReady == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Bottom-left: mute/unmute
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
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Bottom-right: fullscreen
                    IconButton(
                        onClick = { vm.isFullscreen = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
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
                        .background(SurfaceDark),
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .then(
                if (selected) Modifier
                    .background(AccentGold)
                else Modifier
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .background(Color.Transparent)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun CategoryItem(name: String, selected: Boolean, onClick: () -> Unit) {
    val displayName = if (name.all { it.isUpperCase() || !it.isLetter() } && name.length <= 3) name
        else name.lowercase().replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gold left border indicator
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .background(
                    if (selected) AccentGold else Color.Transparent,
                    RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )
        Text(
            displayName,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) AccentGold.copy(alpha = 0.10f)
                else Color(0xFF1C1C2A)
            )
            .then(
                if (isSelected) Modifier.border(1.dp, AccentGold, RoundedCornerShape(6.dp))
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
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.06f))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LiveTv, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(22.dp))
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
        // Gold star icon on every row
        Icon(
            if (isSelected) Icons.Default.Equalizer else Icons.Default.Star,
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
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
                            onTap = { if (!isLocked) onToggleControls() },
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

        // Lock overlay - show unlock button only
        if (isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable { /* absorb taps */ }
            ) {
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = { isLocked = false },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.LockOpen, "Unlock", tint = AccentGold, modifier = Modifier.size(28.dp))
                    }
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
                    IconButton(onClick = { vm.isFullscreen = false }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    vm.channels.getOrNull(vm.selectedChannelIndex)?.let { channel ->
                        Text(
                            channel.name,
                            color = Color.White,
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
                            if (isFitMode) Icons.Default.FitScreen else Icons.Default.Crop,
                            if (isFitMode) "Zoom to Fill" else "Fit to Screen",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { vm.isFullscreen = false }) {
                        Icon(Icons.Default.FullscreenExit, "Exit Fullscreen", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { /* cast placeholder */ }) {
                        Icon(Icons.Default.Cast, "Cast", tint = Color.White, modifier = Modifier.size(22.dp))
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
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                vm.currentExoPlayer?.let { p ->
                                    p.seekTo((p.currentPosition - 10000).coerceAtLeast(0))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Replay, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("10s", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Play/Pause - large, clean
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable {
                                vm.currentExoPlayer?.let { player ->
                                    player.playWhenReady = !player.playWhenReady
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (vm.currentExoPlayer?.playWhenReady == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                            "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    // 10s Forward
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                vm.currentExoPlayer?.let { p ->
                                    p.seekTo(p.currentPosition + 10000)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Forward10, null, tint = Color.White, modifier = Modifier.size(18.dp))
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
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                        // Gold progress bar (full = LIVE)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(AccentGold)
                            )
                            // Thumb circle
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(AccentGold)
                            )
                        }
                        Text(
                            currentTime.format(Date()),
                            color = AccentGold,
                            fontSize = 11.sp
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
                        FullscreenBottomButton(Icons.Default.HighQuality, "Quality") {
                            showQualityPanel = !showQualityPanel; showAudioPanel = false
                        }
                        FullscreenBottomButton(Icons.Default.Lock, "Lock") { isLocked = true; onToggleControls() }
                        FullscreenBottomButton(Icons.Default.Subtitles, "Audio & Subtitles") {
                            showAudioPanel = !showAudioPanel; showQualityPanel = false
                        }
                        FullscreenBottomButton(Icons.Default.LiveTv, "More Channels") {
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
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(12.dp)
            ) {
                Icon(Icons.Default.BrightnessHigh, null, tint = AccentGold, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(4.dp))
                Text("${(currentBrightness * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(12.dp)
            ) {
                Icon(
                    if (currentVolume == 0) Icons.Default.VolumeOff
                    else if (currentVolume < maxVol / 2) Icons.Default.VolumeDown
                    else Icons.Default.VolumeUp,
                    null, tint = Color.White, modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text("${(currentVolume * 100f / maxVol).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── QUALITY PANEL (floating cards, upper-right like reference) ──
        if (showQualityPanel && !isLocked) {
            Box(modifier = Modifier.fillMaxSize().clickable { showQualityPanel = false })
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
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
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
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
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, label, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
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
            .background(Color(0xF5101018))
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) AccentGold.copy(alpha = 0.10f)
                                else Color(0xFF1C1C2A)
                            )
                            .then(
                                if (isSelected) Modifier.border(1.dp, AccentGold, RoundedCornerShape(6.dp))
                                else Modifier
                            )
                            .clickable { onChannelSelected(index) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (channel.image.isNotEmpty()) {
                            AsyncImage(
                                model = channel.image,
                                contentDescription = channel.name,
                                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.06f))
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.06f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LiveTv, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
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
                            if (isSelected) Icons.Default.Equalizer else Icons.Default.Star,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gold left border indicator
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(24.dp)
                .background(
                    if (selected) AccentGold else Color.Transparent,
                    RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )
        Text(
            name,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.45f),
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

    // Auto option
    TrackOptionCard(
        label = "Auto" + if (isAuto && videoTracks.isNotEmpty()) {
            videoTracks.firstOrNull { it.isSelected }?.let { " (${it.width}×${it.height})" } ?: ""
        } else "",
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

    audioTracks.forEachIndexed { i, track ->
        if (i > 0) Spacer(Modifier.height(8.dp))
        val label = (if (track.language.isNotEmpty()) "${track.label} (${track.language.uppercase()})" else track.label) +
                (if (track.channels > 0) " | ${track.channels}ch" else "") +
                (if (track.bitrate > 0) " | ${track.bitrate / 1000}kbps" else "")
        TrackOptionCard(label = label, onClick = {
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

    subtitleTracks.forEachIndexed { i, track ->
        Spacer(Modifier.height(8.dp))
        TrackOptionCard(
            label = track.label + if (track.language.isNotEmpty()) " (${track.language.uppercase()})" else "",
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

// Exact match of reference: wide rounded rect, semi-transparent dark bg, play icon + text
@Composable
private fun TrackOptionCard(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC303040))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.PlayCircle,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            color = Color.White,
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
