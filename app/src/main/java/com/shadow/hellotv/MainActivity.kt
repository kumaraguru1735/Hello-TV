package com.shadow.hellotv

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import com.shadow.hellotv.model.ChannelItem
import com.shadow.hellotv.ui.ChannelChangeOverlay
import com.shadow.hellotv.ui.ChannelListSidebar
import com.shadow.hellotv.ui.ExitDialog
import com.shadow.hellotv.ui.ExoPlayerView
import com.shadow.hellotv.ui.TvControlsHint
import com.shadow.hellotv.ui.VolumeOverlay
import com.shadow.hellotv.ui.theme.HelloTVTheme
import com.shadow.hellotv.ui.theme.IntroUi
import com.shadow.hellotv.utils.KeepScreenOn
import com.shadow.hellotv.utils.loadPlaylist
import kotlin.math.abs
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set black background before ANYTHING else - this prevents white flash
        window.decorView.setBackgroundColor(android.graphics.Color.BLACK)
        window.setBackgroundDrawableResource(android.R.color.black)

        // Full screen setup
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Hide system bars immediately
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()

        setContent {
            HelloTVTheme {
                KeepScreenOn()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    TVPlayerApp()
                }
            }
        }
    }
}

const val PLAYLIST_URL = "https://livetv.ipcloud.live/channels/jiostar.json"
const val MIN_DRAG_DISTANCE = 100f
const val LEFT_DRAG_ZONE = 0.3f
const val SWIPE_THRESHOLD = 150f
const val AUTO_RETRY_DELAY = 5000L // Auto retry every 5 seconds on error

@OptIn(UnstableApi::class)
@Composable
fun TVPlayerApp() {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var channels by remember { mutableStateOf<List<ChannelItem>>(emptyList()) }
    var selectedChannelIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var loadingMessage by remember { mutableStateOf("Initializing...") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableIntStateOf(0) } // Changed to Int for trigger
    var showChannelList by remember { mutableStateOf(false) }
    var showPlayerInfoOverlay by remember { mutableStateOf(false) }
    var showChannelChangeOverlay by remember { mutableStateOf(false) }
    var showControlsHint by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showSettingsOverlay by remember { mutableStateOf(false) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var currentVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStartPosition by remember { mutableStateOf(Offset.Zero) }

    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    // Hide overlays after specified durations
    LaunchedEffect(showPlayerInfoOverlay) {
        if (showPlayerInfoOverlay) {
            delay(3000)
            showPlayerInfoOverlay = false
        }
    }

    LaunchedEffect(showChannelChangeOverlay) {
        if (showChannelChangeOverlay) {
            delay(4000)
            showChannelChangeOverlay = false
        }
    }

    LaunchedEffect(showControlsHint) {
        if (showControlsHint) {
            delay(5000)
            showControlsHint = false
        }
    }

    LaunchedEffect(showSettingsOverlay) {
        if (showSettingsOverlay) {
            delay(5000)
            showSettingsOverlay = false
        }
    }

    LaunchedEffect(showVolumeOverlay) {
        if (showVolumeOverlay) {
            delay(3000)
            showVolumeOverlay = false
        }
    }

    LaunchedEffect(selectedChannelIndex) {
        if (channels.isNotEmpty()) {
            showChannelChangeOverlay = true
        }
    }

    // Load playlist with automatic retry on error
    LaunchedEffect(retryTrigger) {
        while (channels.isEmpty()) {
            try {
                isLoading = true
                errorMessage = null

                loadingMessage = "Connecting to server..."
                delay(500)

                loadingMessage = "Loading channels..."
                channels = loadPlaylist(PLAYLIST_URL)

                if (channels.isEmpty()) {
                    errorMessage = "No channels available"
                    loadingMessage = "Unable to load channels"
                    delay(AUTO_RETRY_DELAY)
                    loadingMessage = "Retrying..."
                    continue // Retry loop
                } else {
                    loadingMessage = "Loading complete!"
                    delay(500)
                    isLoading = false
                    break // Success, exit loop
                }
            } catch (e: Exception) {
                errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "No internet connection"
                    e.message?.contains("timeout") == true ->
                        "Connection timeout"
                    e.message?.contains("404") == true ->
                        "Playlist not found"
                    else ->
                        "Unable to load data"
                }
                loadingMessage = errorMessage ?: "Error occurred"
                e.printStackTrace()

                // Auto retry after delay
                delay(AUTO_RETRY_DELAY)
                loadingMessage = "Retrying..."
                // Loop continues to retry
            }
        }
    }

    // Auto-scroll to selected channel
    LaunchedEffect(selectedChannelIndex, showChannelList) {
        if (channels.isNotEmpty() && showChannelList) {
            listState.animateScrollToItem(selectedChannelIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_VOLUME_UP -> {
                            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val newVolume = (currentVolume + 1).coerceAtMost(maxVolume)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                            currentVolume = newVolume
                            showVolumeOverlay = true
                            true
                        }
                        KeyEvent.KEYCODE_VOLUME_DOWN -> {
                            val newVolume = (currentVolume - 1).coerceAtLeast(0)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                            currentVolume = newVolume
                            showVolumeOverlay = true
                            true
                        }
                        KeyEvent.KEYCODE_VOLUME_MUTE -> {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, 0)
                            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            showVolumeOverlay = true
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (showExitDialog || errorMessage != null) {
                                false
                            } else if (channels.isNotEmpty()) {
                                selectedChannelIndex = if (selectedChannelIndex > 0) {
                                    selectedChannelIndex - 1
                                } else {
                                    channels.size - 1
                                }
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (showExitDialog || errorMessage != null) {
                                false
                            } else if (channels.isNotEmpty()) {
                                selectedChannelIndex = if (selectedChannelIndex < channels.size - 1) {
                                    selectedChannelIndex + 1
                                } else {
                                    0
                                }
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (showExitDialog) {
                                false
                            } else if (showChannelList || showSettingsOverlay) {
                                showChannelList = false
                                showSettingsOverlay = false
                                true
                            } else {
                                false
                            }
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            if (showExitDialog) {
                                false
                            } else if (showChannelList || showSettingsOverlay) {
                                showChannelList = false
                                showSettingsOverlay = false
                                true
                            } else if (errorMessage != null) {
                                // Manual retry on back press when error
                                retryTrigger++
                                true
                            } else {
                                showExitDialog = true
                                true
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (showExitDialog) {
                                false
                            } else if (errorMessage != null) {
                                // Manual retry on enter/ok when error
                                retryTrigger++
                                true
                            } else {
                                showChannelList = !showChannelList
                                if (showChannelList) {
                                    showControlsHint = true
                                }
                                true
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (showExitDialog) {
                                false
                            } else if (!showChannelList) {
                                showSettingsOverlay = !showSettingsOverlay
                                if (showSettingsOverlay) {
                                    showControlsHint = true
                                }
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
                } else false
            }
            .focusRequester(focusRequester)
            .focusable()
    ) {
        when {
            channels.isEmpty() -> {
                // Show IntroUI for both loading and error states
                IntroUi(
                    isLoading = isLoading,
                    message = loadingMessage,
                    errorMessage = errorMessage,
                    onRetry = {
                        retryTrigger++
                    }
                )
            }

            else -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Channel List Sidebar
                    if (showChannelList) {
                        ChannelListSidebar(
                            channels = channels,
                            selectedChannelIndex = selectedChannelIndex,
                            onChannelSelected = { index ->
                                selectedChannelIndex = index
                                showChannelList = false
                            },
                            listState = listState,
                            modifier = Modifier
                                .width(400.dp)
                                .fillMaxHeight()
                                .padding(12.dp)
                        )
                    }

                    // Video Player
                    Box(modifier = Modifier.weight(1f)) {
                        channels.getOrNull(selectedChannelIndex)?.let { channel ->
                            ExoPlayerView(
                                channel = channel,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = { offset ->
                                                if (offset.x < size.width * 0.3f) {
                                                    showChannelList = !showChannelList
                                                } else {
                                                    showPlayerInfoOverlay = true
                                                    showControlsHint = true
                                                }
                                            },
                                            onDoubleTap = {
                                                showChannelList = !showChannelList
                                            }
                                        )
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                isDragging = true
                                                dragStartPosition = offset
                                            },
                                            onDragEnd = {
                                                isDragging = false
                                            }
                                        ) { change, _ ->
                                            val deltaY = change.position.y - dragStartPosition.y
                                            val deltaX = change.position.x - dragStartPosition.x

                                            if (abs(deltaY) > SWIPE_THRESHOLD && abs(deltaY) > abs(deltaX)) {
                                                if (deltaY > 0) {
                                                    selectedChannelIndex = if (selectedChannelIndex > 0) {
                                                        selectedChannelIndex - 1
                                                    } else {
                                                        channels.size - 1
                                                    }
                                                } else {
                                                    selectedChannelIndex = if (selectedChannelIndex < channels.size - 1) {
                                                        selectedChannelIndex + 1
                                                    } else {
                                                        0
                                                    }
                                                }
                                                dragStartPosition = change.position
                                            }

                                            if (dragStartPosition.x < size.width * LEFT_DRAG_ZONE &&
                                                deltaX > SWIPE_THRESHOLD &&
                                                abs(deltaX) > abs(deltaY)) {
                                                showChannelList = true
                                                isDragging = false
                                            }
                                        }
                                    }
                            )
                        }

                        VolumeOverlay(
                            show = showVolumeOverlay,
                            currentVolume = currentVolume,
                            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                            isMuted = currentVolume == 0,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )

                        ChannelChangeOverlay(
                            show = showPlayerInfoOverlay && !showChannelList,
                            showChannelList = showChannelList,
                            channels = channels,
                            selectedChannelIndex = selectedChannelIndex,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )

                        ChannelChangeOverlay(
                            show = showChannelChangeOverlay,
                            showChannelList = showChannelList,
                            channels = channels,
                            selectedChannelIndex = selectedChannelIndex,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )

                        if (showSettingsOverlay && !showChannelList) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(300.dp)
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.95f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Audio/Video Settings",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    channels.getOrNull(selectedChannelIndex)?.let { channel ->
                                        Text(
                                            text = "Current Channel: ${channel.name}",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )

                                        if (!channel.drmUrl.isNullOrEmpty()) {
                                            Text(
                                                text = "DRM Protected Content",
                                                color = Color.Yellow,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Audio Track",
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Gray.copy(alpha = 0.8f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Auto", color = Color.White, fontSize = 14.sp)
                                        }
                                        Button(
                                            onClick = { },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Gray.copy(alpha = 0.8f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Track 1", color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                    Text(
                                        text = "Video Quality",
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Gray.copy(alpha = 0.8f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Auto", color = Color.White, fontSize = 14.sp)
                                        }
                                        Button(
                                            onClick = { },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Gray.copy(alpha = 0.8f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("HD", color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                    Text(
                                        text = "Use ↑↓ to navigate, OK to select, ← to hide, Vol+/- for volume",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (showControlsHint && !showChannelList && !showPlayerInfoOverlay && !showSettingsOverlay) {
                            TvControlsHint(modifier = Modifier.align(Alignment.TopEnd))
                        }

                        ExitDialog(
                            showExitDialog = showExitDialog,
                            onDismiss = { showExitDialog = false }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}