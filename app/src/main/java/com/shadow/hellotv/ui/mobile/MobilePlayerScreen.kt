package com.shadow.hellotv.ui.mobile

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.shadow.hellotv.ui.ExitDialog
import com.shadow.hellotv.ui.ExoPlayerView
import com.shadow.hellotv.ui.theme.*
import com.shadow.hellotv.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MobilePlayerScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var showControls by remember { mutableStateOf(true) }
    var showChannelSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var currentVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    var dragStartY by remember { mutableFloatStateOf(0f) }

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4000)
            showControls = false
        }
    }

    // Channel change overlay
    LaunchedEffect(vm.selectedChannelIndex) {
        showControls = true
    }

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
                            onTap = { showControls = !showControls },
                            onDoubleTap = { showChannelSheet = true }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> dragStartY = offset.y },
                            onDragEnd = {}
                        ) { change, _ ->
                            val deltaY = change.position.y - dragStartY
                            val deltaX = change.position.x - change.previousPosition.x

                            // Vertical swipe on left half = volume
                            if (change.position.x < size.width * 0.5f && abs(deltaY) > 100) {
                                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                if (deltaY < 0) {
                                    val newVol = (currentVolume + 1).coerceAtMost(maxVol)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    currentVolume = newVol
                                } else {
                                    val newVol = (currentVolume - 1).coerceAtLeast(0)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    currentVolume = newVol
                                }
                                dragStartY = change.position.y
                            }

                            // Horizontal swipe = channel change
                            if (abs(deltaX) > 150 && abs(deltaX) > abs(change.position.y - dragStartY)) {
                                if (deltaX > 0) vm.previousChannel() else vm.nextChannel()
                                dragStartY = change.position.y
                            }
                        }
                    }
            )
        }

        // Top controls bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "CH ${channel.channelNo}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Settings button
                IconButton(onClick = { showSettingsSheet = true }) {
                    Icon(Icons.Default.Settings, null, tint = Color.White)
                }
            }
        }

        // Bottom controls bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.previousChannel() }) {
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = { showChannelSheet = true }) {
                    Icon(Icons.Default.List, "Channels", tint = Color.White, modifier = Modifier.size(28.dp))
                }

                // Live badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StatusLive)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = { vm.nextChannel() }) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }

        // Exit dialog
        ExitDialog(
            showExitDialog = vm.showExitDialog,
            onDismiss = { vm.showExitDialog = false }
        )
    }

    // Channel bottom sheet
    if (showChannelSheet) {
        MobileChannelSheet(
            vm = vm,
            onDismiss = { showChannelSheet = false },
            onChannelSelected = { index ->
                vm.selectChannel(index)
                showChannelSheet = false
            }
        )
    }

    // Settings bottom sheet
    if (showSettingsSheet) {
        MobileSettingsSheet(
            vm = vm,
            onDismiss = { showSettingsSheet = false }
        )
    }
}
