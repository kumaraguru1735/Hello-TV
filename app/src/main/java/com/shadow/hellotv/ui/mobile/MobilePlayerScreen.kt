package com.shadow.hellotv.ui.mobile

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

    // Auto-hide controls (only in fullscreen)
    LaunchedEffect(showControls, vm.isFullscreen) {
        if (showControls && vm.isFullscreen) {
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
            onSettings = { showSettingsSheet = true }
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
    val channelListState = rememberLazyListState()
    var showVideoOverlay by remember { mutableStateOf(false) }

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
            .background(Color(0xFF080C18))
            .statusBarsPadding()
    ) {
        // ── Video player area (16:9 aspect) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .clickable { showVideoOverlay = !showVideoOverlay }
        ) {
            vm.channels.getOrNull(vm.selectedChannelIndex)?.let { channel ->
                ExoPlayerView(
                    channel = channel,
                    onPlayerReady = { vm.currentExoPlayer = it },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Animated overlay controls
            androidx.compose.animation.AnimatedVisibility(
                visible = showVideoOverlay,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top gradient with channel info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        vm.channels.getOrNull(vm.selectedChannelIndex)?.let { channel ->
                            if (channel.image.isNotEmpty()) {
                                AsyncImage(
                                    model = channel.image,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    channel.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(StatusLive)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text("LIVE", color = StatusLive, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(6.dp))
                                    Text("CH ${channel.channelNo}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                }
                            }
                        }
                        IconButton(onClick = { vm.isFullscreen = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Fullscreen, "Fullscreen", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = onSettings, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Tune, "Settings", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Bottom: prev/next
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { vm.previousChannel() }) {
                            Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StatusLive)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { vm.nextChannel() }) {
                            Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

        }

        // ── Language chips (horizontal scroll) ──
        if (vm.languages.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                item {
                    FilterChip("All", vm.selectedLanguageId == null) {
                        vm.selectedLanguageId = null; vm.applyFilters()
                    }
                }
                items(vm.languages) { lang ->
                    FilterChip(
                        lang.name.lowercase().replaceFirstChar { it.uppercase() },
                        vm.selectedLanguageId == lang.id
                    ) {
                        vm.selectedLanguageId = if (vm.selectedLanguageId == lang.id) null else lang.id
                        vm.applyFilters()
                    }
                }
            }
        }

        // Thin separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.06f))
        )

        // ── Categories (left) + Channels (right) ──
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: categories
            if (vm.categories.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .width(110.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF0A0F1C)),
                    contentPadding = PaddingValues(vertical = 4.dp)
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

                // Vertical separator
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.06f))
                )
            }

            // Right: channels
            LazyColumn(
                state = channelListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    Text(
                        "${vm.channels.size} channels",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(vm.channels) { index, channel ->
                    PortraitChannelRow(
                        channel = channel,
                        isSelected = index == vm.selectedChannelIndex,
                        onClick = { vm.selectChannel(index) }
                    )
                    if (index < vm.channels.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 52.dp)
                                .height(0.5.dp)
                                .background(Color.White.copy(alpha = 0.04f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) Brush.horizontalGradient(listOf(HotstarBlue, Color(0xFF6366F1)))
                else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.06f)))
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CategoryItem(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) HotstarBlue.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 11.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(HotstarBlue)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            if (name.all { it.isUpperCase() || !it.isLetter() } && name.length <= 3) name
            else name.lowercase().replaceFirstChar { it.uppercase() },
            color = if (selected) HotstarBlueLight else Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Brush.horizontalGradient(
                    listOf(HotstarBlue.copy(alpha = 0.15f), HotstarBlue.copy(alpha = 0.05f))
                )
                else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel logo
        if (channel.image.isNotEmpty()) {
            AsyncImage(
                model = channel.image,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LiveTv, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                channel.name,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "CH ${channel.channelNo}",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 10.sp
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(StatusLive)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text("LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
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
    onSettings: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Video player
        vm.channels.getOrNull(vm.selectedChannelIndex)?.let { channel ->
            ExoPlayerView(
                channel = channel,
                onPlayerReady = { vm.currentExoPlayer = it },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onToggleControls() },
                            onDoubleTap = { offset ->
                                if (offset.x < size.width / 2) vm.previousChannel()
                                else vm.nextChannel()
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> onDragStartYChange(offset.y) },
                            onDragEnd = {}
                        ) { change, dragAmount ->
                            val deltaY = change.position.y - dragStartY
                            val deltaX = change.position.x - change.previousPosition.x

                            if (abs(deltaY) > 80 && abs(deltaY) > abs(dragAmount.x) * 2) {
                                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val newVol = if (deltaY < 0) (currentVolume + 1).coerceAtMost(maxVol)
                                else (currentVolume - 1).coerceAtLeast(0)
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                onVolumeChange(newVol)
                                onShowVolumeFeedback()
                                onDragStartYChange(change.position.y)
                            }

                            if (abs(deltaX) > 150 && abs(deltaX) > abs(change.position.y - dragStartY)) {
                                if (deltaX > 0) vm.previousChannel() else vm.nextChannel()
                                onDragStartYChange(change.position.y)
                            }
                        }
                    }
            )
        }

        // Top gradient + controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                vm.channels.getOrNull(vm.selectedChannelIndex)?.let { channel ->
                    if (channel.image.isNotEmpty()) {
                        AsyncImage(
                            model = channel.image,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(channel.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StatusLive))
                            Spacer(Modifier.width(4.dp))
                            Text("LIVE", color = StatusLive, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("CH ${channel.channelNo}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
                IconButton(onClick = { vm.isFullscreen = false }) {
                    Icon(Icons.Default.FullscreenExit, "Exit Fullscreen", tint = Color.White)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Tune, null, tint = Color.White)
                }
            }
        }

        // Bottom controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.previousChannel() }) {
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusLive)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("LIVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { vm.nextChannel() }) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

        // Volume feedback
        AnimatedVisibility(
            visible = showVolumeFeedback,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
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
    }
}
