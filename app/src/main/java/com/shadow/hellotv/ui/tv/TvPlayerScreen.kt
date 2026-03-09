package com.shadow.hellotv.ui.tv

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import com.shadow.hellotv.model.*
import com.shadow.hellotv.ui.ExoPlayerView
import com.shadow.hellotv.ui.ExitDialog
import com.shadow.hellotv.ui.VolumeOverlay
import com.shadow.hellotv.ui.ChannelChangeOverlay
import com.shadow.hellotv.ui.theme.*
import com.shadow.hellotv.utils.SessionManager
import com.shadow.hellotv.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun TvPlayerScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    var currentVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }

    // Channel number input accumulator
    var channelNumberInput by remember { mutableStateOf("") }
    var showChannelNumberOverlay by remember { mutableStateOf(false) }

    // Auto-hide overlays
    LaunchedEffect(vm.showPlayerInfoOverlay) {
        if (vm.showPlayerInfoOverlay) { delay(3000); vm.showPlayerInfoOverlay = false }
    }
    LaunchedEffect(vm.showChannelChangeOverlay) {
        if (vm.showChannelChangeOverlay) { delay(4000); vm.showChannelChangeOverlay = false }
    }
    LaunchedEffect(vm.showControlsHint) {
        if (vm.showControlsHint) { delay(5000); vm.showControlsHint = false }
    }
    LaunchedEffect(vm.showVolumeOverlay) {
        if (vm.showVolumeOverlay) { delay(3000); vm.showVolumeOverlay = false }
    }
    LaunchedEffect(vm.selectedChannelIndex) {
        if (vm.channels.isNotEmpty()) vm.showChannelChangeOverlay = true
    }

    // Channel number input timeout
    LaunchedEffect(channelNumberInput) {
        if (channelNumberInput.isNotEmpty()) {
            showChannelNumberOverlay = true
            delay(2000)
            if (channelNumberInput.isNotEmpty()) {
                val num = channelNumberInput.toIntOrNull()
                if (num != null) vm.jumpToChannelNumber(num)
                channelNumberInput = ""
                showChannelNumberOverlay = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerBackground)
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    val keyCode = keyEvent.nativeKeyEvent.keyCode

                    // Channel number input (digit keys)
                    if (keyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 &&
                        !vm.showChannelList && !vm.showSettingsPanel && !vm.showExitDialog
                    ) {
                        val digit = keyCode - KeyEvent.KEYCODE_0
                        if (channelNumberInput.length < 4) {
                            channelNumberInput += digit.toString()
                        }
                        return@onKeyEvent true
                    }

                    when (keyCode) {
                        KeyEvent.KEYCODE_VOLUME_UP -> {
                            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val newVol = (currentVolume + 1).coerceAtMost(max)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                            currentVolume = newVol
                            vm.showVolumeOverlay = true
                            true
                        }
                        KeyEvent.KEYCODE_VOLUME_DOWN -> {
                            val newVol = (currentVolume - 1).coerceAtLeast(0)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                            currentVolume = newVol
                            vm.showVolumeOverlay = true
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (!vm.showExitDialog && !vm.showChannelList && !vm.showSettingsPanel) {
                                vm.previousChannel()
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (!vm.showExitDialog && !vm.showChannelList && !vm.showSettingsPanel) {
                                vm.nextChannel()
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            when {
                                vm.showExitDialog -> false
                                vm.showSettingsPanel -> { vm.showSettingsPanel = false; true }
                                else -> { vm.showChannelList = !vm.showChannelList; true }
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            when {
                                vm.showExitDialog -> false
                                vm.showChannelList -> { vm.showChannelList = false; true }
                                else -> { vm.showSettingsPanel = !vm.showSettingsPanel; true }
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (!vm.showExitDialog) {
                                vm.showPlayerInfoOverlay = true
                                vm.showControlsHint = true
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            when {
                                vm.showExitDialog -> false
                                vm.showSessionManager -> { vm.showSessionManager = false; true }
                                vm.showChannelList -> { vm.showChannelList = false; true }
                                vm.showSettingsPanel -> { vm.showSettingsPanel = false; true }
                                else -> { vm.showExitDialog = true; true }
                            }
                        }
                        else -> false
                    }
                } else false
            }
            .focusRequester(focusRequester)
            .focusable()
    ) {
        // Video player
        vm.channels.getOrNull(vm.selectedChannelIndex)?.let { channel ->
            ExoPlayerView(
                channel = channel,
                onPlayerReady = { vm.currentExoPlayer = it },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Left: Channel Menu
        if (vm.showChannelList) {
            TvChannelMenu(
                channels = vm.channels,
                categories = vm.categories,
                languages = vm.languages,
                selectedChannelIndex = vm.selectedChannelIndex,
                selectedCategoryId = vm.selectedCategoryId,
                selectedLanguageId = vm.selectedLanguageId,
                onChannelSelected = { index ->
                    vm.selectChannel(index)
                    vm.showChannelList = false
                },
                onCategorySelected = { id ->
                    vm.selectedCategoryId = id
                    vm.applyFilters()
                },
                onLanguageSelected = { id ->
                    vm.selectedLanguageId = id
                    vm.applyFilters()
                },
                onDismiss = { vm.showChannelList = false },
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.45f)
                    .align(Alignment.CenterStart)
            )
        }

        // Right: Settings Panel
        if (vm.showSettingsPanel) {
            TvSettingsPanel(
                subscriber = vm.subscriber,
                channel = vm.channels.getOrNull(vm.selectedChannelIndex),
                exoPlayer = vm.currentExoPlayer,
                subscriptionInfo = vm.subscriptionInfo,
                sessionManager = vm.sessionManager,
                onManageSessions = {
                    vm.showSessionManager = true
                    vm.showSettingsPanel = false
                },
                onLogout = { vm.doLogout() },
                onDismiss = { vm.showSettingsPanel = false },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(360.dp)
                    .align(Alignment.CenterEnd)
            )
        }

        // Volume overlay
        VolumeOverlay(
            show = vm.showVolumeOverlay,
            currentVolume = currentVolume,
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            isMuted = currentVolume == 0,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // Channel change overlay
        ChannelChangeOverlay(
            show = vm.showChannelChangeOverlay && !vm.showChannelList,
            showChannelList = vm.showChannelList,
            channels = vm.channels,
            selectedChannelIndex = vm.selectedChannelIndex,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Channel number input overlay
        if (showChannelNumberOverlay) {
            TvChannelNumberOverlay(
                input = channelNumberInput,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Exit dialog
        ExitDialog(
            showExitDialog = vm.showExitDialog,
            onDismiss = { vm.showExitDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
