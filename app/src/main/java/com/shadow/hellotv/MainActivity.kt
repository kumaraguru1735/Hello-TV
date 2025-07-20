package com.shadow.hellotv

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shadow.hellotv.ui.ChannelItem
import com.shadow.hellotv.ui.ErrorMessage
import com.shadow.hellotv.ui.ExitDialog
import com.shadow.hellotv.ui.ExoPlayerView
import com.shadow.hellotv.ui.TvControlsHint
import com.shadow.hellotv.ui.theme.HelloTVTheme
import com.shadow.hellotv.ui.theme.IntroUi
import com.shadow.hellotv.utils.KeepScreenOn
import com.shadow.hellotv.utils.calculateDistance
import com.shadow.hellotv.utils.loadPlaylist
import kotlinx.coroutines.delay
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            HelloTVTheme {
                KeepScreenOn()
                TVPlayerApp()
            }
        }
    }
}

data class TVChannel(
    val id: String,
    val name: String,
    val logo: String,
    val group: String,
    val url: String,
    val licenseKey: String? = null
)

const val PLAYLIST_URL = "https://livetv.ipcloud.live/channels/playlist.m3u"
const val MIN_DRAG_DISTANCE = 100f
const val LEFT_DRAG_ZONE = 0.3f // Left 30% of screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TVPlayerApp() {
    var channels by remember { mutableStateOf<List<TVChannel>>(emptyList()) }
    var selectedChannelIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showChannelList by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPlayerInfoOverlay by remember { mutableStateOf(false) }
    var showChannelChangeOverlay by remember { mutableStateOf(false) }
    var showControlsHint by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showSettingsOverlay by remember { mutableStateOf(false) } // New state for settings
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
            delay(2000)
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

    LaunchedEffect(selectedChannelIndex) {
        if (channels.isNotEmpty()) {
            showChannelChangeOverlay = true
        }
    }

    // Load playlist
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = null
            channels = loadPlaylist(PLAYLIST_URL)
            if (channels.isEmpty()) {
                errorMessage = "No channels found in playlist"
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load playlist: ${e.message}"
            e.printStackTrace()
        } finally {
            isLoading = false
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
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (showExitDialog) {
                                false // Let exit dialog handle it
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
                            if (showExitDialog) {
                                false // Let exit dialog handle it
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
                                false // Let exit dialog handle it
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
                                false // Let exit dialog handle it
                            } else if (showChannelList || showSettingsOverlay) {
                                showChannelList = false
                                showSettingsOverlay = false
                                true
                            } else {
                                showExitDialog = true
                                true
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (showExitDialog) {
                                false // Let exit dialog handle it
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
                                false // Let exit dialog handle it
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
            isLoading -> {
                IntroUi()
            }

            errorMessage != null -> {
                ErrorMessage(errorMessage!!)
            }

            channels.isNotEmpty() -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Channel List Sidebar
                    if (showChannelList) {
                        Card(
                            modifier = Modifier
                                .width(400.dp)
                                .fillMaxHeight()
                                .padding(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.95f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.95f),
                                                Color.DarkGray.copy(alpha = 0.95f)
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Channels (${channels.size})",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Text(
                                    text = "Use ↑↓ to navigate, OK to select, ← to hide",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                LazyColumn(
                                    state = listState,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(channels) { index, channel ->
                                        ChannelItem(
                                            channel = channel,
                                            channelNumber = index + 1,
                                            isSelected = index == selectedChannelIndex,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedChannelIndex = index
                                                    showChannelList = false
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Video Player
                    Box(modifier = Modifier.weight(1f)) {
                        channels.getOrNull(selectedChannelIndex)?.let { channel ->
                            ExoPlayerView(
                                channel = channel,
                                modifier = Modifier
                                    .fillMaxSize()
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
                                            val dragDistance = calculateDistance(dragStartPosition, change.position)
                                            if (dragDistance > MIN_DRAG_DISTANCE) {
                                                val deltaY = change.position.y - dragStartPosition.y
                                                if (abs(deltaY) > abs(change.position.x - dragStartPosition.x)) {
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
                                            }
                                        }
                                    }
                                    .clickable {
                                        showPlayerInfoOverlay = true
                                        showControlsHint = true
                                    }
                            )
                        }

                        // Player Info Overlay - Top Right
                        if (showPlayerInfoOverlay && !showChannelList) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.85f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    channels.getOrNull(selectedChannelIndex)?.let { channel ->
                                        Text(
                                            text = "${selectedChannelIndex + 1}/${channels.size}",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = channel.name,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (channel.group.isNotEmpty()) {
                                            Text(
                                                text = channel.group,
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Channel Change Overlay - Bottom Center
                        if (showChannelChangeOverlay && !showChannelList) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    channels.getOrNull(selectedChannelIndex)?.let { channel ->
                                        AsyncImage(
                                            model = channel.logo,
                                            contentDescription = "Channel Logo",
                                            modifier = Modifier
                                                .size(56.dp)
                                                .padding(end = 16.dp),
                                            onError = {}
                                        )
                                        Column {
                                            Text(
                                                text = "${selectedChannelIndex + 1}. ${channel.name}",
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (channel.group.isNotEmpty()) {
                                                Text(
                                                    text = channel.group,
                                                    color = Color.Gray,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Audio/Video Settings Overlay - Right Side
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
                                    Text(
                                        text = "Audio Track",
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { /* TODO: Switch audio track */ },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Gray.copy(alpha = 0.8f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Track 1", color = Color.White, fontSize = 14.sp)
                                        }
                                        Button(
                                            onClick = { /* TODO: Switch audio track */ },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Gray.copy(alpha = 0.8f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Track 2", color = Color.White, fontSize = 14.sp)
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
                                            onClick = { /* TODO: Switch video quality */ },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Gray.copy(alpha = 0.8f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Auto", color = Color.White, fontSize = 14.sp)
                                        }
                                        Button(
                                            onClick = { /* TODO: Switch video quality */ },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Gray.copy(alpha = 0.8f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("HD", color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                    Text(
                                        text = "Use ↑↓ to navigate, OK to select, ← to hide",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Controls Hint - Bottom Left
                        if (showControlsHint && !showChannelList && !showPlayerInfoOverlay && !showSettingsOverlay) {
                            TvControlsHint(modifier = Modifier)
                        }
                    }
                }
            }
        }
    }

    // Exit Dialog - OUTSIDE the main UI
    ExitDialog(
        showExitDialog = showExitDialog,
        onDismiss = { showExitDialog = false }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}