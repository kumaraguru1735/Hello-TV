package com.shadow.hellotv

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.shadow.hellotv.model.*
import com.shadow.hellotv.network.ApiResult
import com.shadow.hellotv.network.ApiService
import com.shadow.hellotv.ui.*
import com.shadow.hellotv.ui.theme.HelloTVTheme
import com.shadow.hellotv.ui.theme.IntroUi
import com.shadow.hellotv.utils.KeepScreenOn
import com.shadow.hellotv.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val TAG = "HelloTV"
private fun log(msg: String) { Log.e(TAG, msg); println("[$TAG] $msg") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.setBackgroundColor(android.graphics.Color.BLACK)
        window.setBackgroundDrawableResource(android.R.color.black)

        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Allow both portrait for login and landscape for player
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        enableEdgeToEdge()

        setContent {
            HelloTVTheme {
                KeepScreenOn()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    HelloTVApp()
                }
            }
        }
    }
}

const val MIN_DRAG_DISTANCE = 100f
const val LEFT_DRAG_ZONE = 0.3f
const val SWIPE_THRESHOLD = 150f

enum class AppScreen {
    SPLASH,
    LOGIN,
    SESSION_KICKOUT,
    PLAYER
}

@OptIn(UnstableApi::class)
@Composable
fun HelloTVApp() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    // ── App State ──
    var currentScreen by remember {
        mutableStateOf(if (sessionManager.isLoggedIn) AppScreen.SPLASH else AppScreen.LOGIN)
    }
    var isLoading by remember { mutableStateOf(true) }
    var loadingMessage by remember { mutableStateOf("Initializing...") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── Auth State ──
    var loginError by remember { mutableStateOf<String?>(null) }
    var loginLoading by remember { mutableStateOf(false) }
    var kickoutSessions by remember { mutableStateOf<List<SessionInfo>>(emptyList()) }
    var kickoutLoading by remember { mutableStateOf(false) }
    var kickoutError by remember { mutableStateOf<String?>(null) }

    // ── Data State ──
    var subscriber by remember { mutableStateOf<Subscriber?>(null) }
    var subscriptionInfo by remember { mutableStateOf<SubscriptionInfo?>(null) }
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var languages by remember { mutableStateOf<List<Language>>(emptyList()) }

    // ── Filter State ──
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedLanguageId by remember { mutableStateOf<Int?>(null) }
    var allChannels by remember { mutableStateOf<List<Channel>>(emptyList()) }

    // ── Player State ──
    var selectedChannelIndex by remember { mutableIntStateOf(0) }
    var showChannelList by remember { mutableStateOf(false) }
    var showPlayerInfoOverlay by remember { mutableStateOf(false) }
    var showChannelChangeOverlay by remember { mutableStateOf(false) }
    var showControlsHint by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showSettingsPanel by remember { mutableStateOf(false) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var showSessionManager by remember { mutableStateOf(false) }
    var currentExoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    var currentVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStartPosition by remember { mutableStateOf(Offset.Zero) }

    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    // ── Set orientation based on screen (use SideEffect to avoid recomposition loop) ──
    val activity = context as? ComponentActivity
    androidx.compose.runtime.SideEffect {
        val orient = when (currentScreen) {
            AppScreen.LOGIN -> ActivityInfo.SCREEN_ORIENTATION_USER
            AppScreen.SESSION_KICKOUT -> ActivityInfo.SCREEN_ORIENTATION_USER
            AppScreen.SPLASH -> ActivityInfo.SCREEN_ORIENTATION_USER
            AppScreen.PLAYER -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        if (activity?.requestedOrientation != orient) {
            activity?.requestedOrientation = orient
        }
    }

    // ── Filter channels when selection changes ──
    LaunchedEffect(selectedCategoryId, selectedLanguageId, allChannels) {
        channels = allChannels.filter { ch ->
            (selectedCategoryId == null || ch.categoryId == selectedCategoryId) &&
                    (selectedLanguageId == null || ch.languageId == selectedLanguageId)
        }
        if (selectedChannelIndex >= channels.size) {
            selectedChannelIndex = 0
        }
    }

    // ── Load content - uses scope.launch to survive recomposition ──
    var isContentLoading by remember { mutableStateOf(false) }

    fun loadContent() {
        if (isContentLoading) return
        isContentLoading = true
        scope.launch {
            if (!sessionManager.isLoggedIn || sessionManager.sessionToken == null) {
                isContentLoading = false
                currentScreen = AppScreen.LOGIN
                return@launch
            }

            var token = sessionManager.sessionToken!!
            log( "Loading content with token: ${token.take(8)}...")

            loadingMessage = "Loading channels..."
            when (val result = ApiService.getChannels(token)) {
                is ApiResult.Success -> {
                    allChannels = result.data.channels
                    if (result.data.subscriber != null) subscriber = result.data.subscriber
                    subscriptionInfo = result.data.subscriptionInfo
                    log( "Loaded ${result.data.channels.size} channels")
                }
                is ApiResult.Error -> {
                    log( "Channel load failed: ${result.errorCode} - ${result.message}")
                    if (result.errorCode == "INVALID_SESSION" || result.errorCode == "SESSION_EXPIRED" ||
                        result.errorCode == "ACCOUNT_INACTIVE" || result.errorCode == "NO_SESSION") {
                        // Try auto re-login with saved credentials if available
                        if (sessionManager.rememberMe && !sessionManager.savedPhone.isNullOrEmpty() && !sessionManager.savedPin.isNullOrEmpty()) {
                            log( "Session lost, attempting auto re-login...")
                            loadingMessage = "Re-authenticating..."
                            ApiService.clearCookies()
                            val loginResult = ApiService.login(LoginRequest(
                                phone = sessionManager.savedPhone!!,
                                pin = sessionManager.savedPin!!,
                                deviceId = sessionManager.getDeviceId(),
                                deviceType = sessionManager.getDeviceType(),
                                deviceModel = sessionManager.getDeviceModel(),
                                deviceName = sessionManager.getDeviceName()
                            ))
                            when (loginResult) {
                                is ApiResult.Success -> {
                                    val data = loginResult.data
                                    sessionManager.saveLoginData(
                                        sessionToken = data.sessionToken,
                                        sessionType = data.sessionType,
                                        userId = data.subscriber.id,
                                        userName = data.subscriber.name,
                                        userPhone = data.subscriber.phone,
                                        userEmail = data.subscriber.email,
                                        expiredAt = data.subscriber.expiredAt
                                    )
                                    subscriber = data.subscriber
                                    token = data.sessionToken
                                    // Retry channels with new token
                                    val retryResult = ApiService.getChannels(token)
                                    when (retryResult) {
                                        is ApiResult.Success -> {
                                            allChannels = retryResult.data.channels
                                            if (retryResult.data.subscriber != null) subscriber = retryResult.data.subscriber
                                            subscriptionInfo = retryResult.data.subscriptionInfo
                                            log( "Re-login success, loaded ${retryResult.data.channels.size} channels")
                                        }
                                        is ApiResult.Error -> {
                                            sessionManager.clearSession()
                                            isContentLoading = false
                                            currentScreen = AppScreen.LOGIN
                                            return@launch
                                        }
                                    }
                                }
                                is ApiResult.Error -> {
                                    sessionManager.clearSession()
                                    isContentLoading = false
                                    currentScreen = AppScreen.LOGIN
                                    return@launch
                                }
                            }
                        } else {
                            sessionManager.clearSession()
                            isContentLoading = false
                            currentScreen = AppScreen.LOGIN
                            return@launch
                        }
                    } else {
                        errorMessage = "Failed to load channels: ${result.message}"
                    }
                }
            }

            loadingMessage = "Loading categories..."
            when (val catResult = ApiService.getCategories(token)) {
                is ApiResult.Success -> {
                    categories = catResult.data.categories
                    log( "Loaded ${catResult.data.categories.size} categories")
                }
                is ApiResult.Error -> log( "Categories failed: ${catResult.message}")
            }

            loadingMessage = "Loading languages..."
            when (val langResult = ApiService.getLanguages(token)) {
                is ApiResult.Success -> {
                    languages = langResult.data.languages
                    log( "Loaded ${langResult.data.languages.size} languages")
                }
                is ApiResult.Error -> log( "Languages failed: ${langResult.message}")
            }

            if (allChannels.isNotEmpty()) {
                loadingMessage = "Ready!"
                delay(300)
                isLoading = false
                isContentLoading = false
                currentScreen = AppScreen.PLAYER
            } else if (errorMessage == null) {
                errorMessage = "No channels available"
                isContentLoading = false
            }
        }
    }

    // ── Login handler ──
    fun doLogin(phone: String, pin: String) {
        scope.launch {
            loginLoading = true
            loginError = null
            ApiService.clearCookies() // Reset cookies for fresh login
            log( "Attempting login for phone: $phone")

            val request = LoginRequest(
                phone = phone,
                pin = pin,
                deviceId = sessionManager.getDeviceId(),
                deviceType = sessionManager.getDeviceType(),
                deviceModel = sessionManager.getDeviceModel(),
                deviceName = sessionManager.getDeviceName()
            )

            val result = ApiService.login(request)
            log( "Login result type: ${result::class.simpleName}")
            when (result) {
                is ApiResult.Success -> {
                    log( "Login successful, session type: ${result.data.sessionType}")
                    val data = result.data
                    sessionManager.saveLoginData(
                        sessionToken = data.sessionToken,
                        sessionType = data.sessionType,
                        userId = data.subscriber.id,
                        userName = data.subscriber.name,
                        userPhone = data.subscriber.phone,
                        userEmail = data.subscriber.email,
                        expiredAt = data.subscriber.expiredAt
                    )
                    subscriber = data.subscriber

                    if (data.sessionType == "temporary" && !data.currentSessions.isNullOrEmpty()) {
                        log( "Session temporary with ${data.currentSessions.size} sessions, going to KICKOUT")
                        kickoutSessions = data.currentSessions
                        currentScreen = AppScreen.SESSION_KICKOUT
                    } else {
                        log( "Login OK, loading content...")
                        isLoading = true
                        loadingMessage = "Loading channels..."
                        errorMessage = null
                        currentScreen = AppScreen.SPLASH
                        loadContent()
                    }
                }
                is ApiResult.Error -> {
                    log( "Login failed: ${result.errorCode} - ${result.message}, data: ${result.data?.take(200)}")
                    loginError = when (result.errorCode) {
                        "INVALID_CREDENTIALS" -> "Invalid phone number or PIN"
                        "SUBSCRIPTION_EXPIRED" -> "Your subscription has expired"
                        "ISP_NOT_ALLOWED" -> "Your ISP is not allowed"
                        "MISSING_FIELDS" -> "Please enter phone and PIN"
                        else -> result.message
                    }
                }
            }
            loginLoading = false
        }
    }

    // ── Replace device handler ──
    fun doReplaceDevice(targetSessionId: Int) {
        scope.launch {
            kickoutLoading = true
            kickoutError = null
            val token = sessionManager.sessionToken ?: return@launch

            when (val result = ApiService.replaceDevice(token, targetSessionId)) {
                is ApiResult.Success -> {
                    val data = result.data
                    sessionManager.sessionToken = data.sessionToken
                    sessionManager.sessionType = data.sessionType
                    if (data.subscriber != null) subscriber = data.subscriber

                    isLoading = true
                    loadingMessage = "Loading channels..."
                    errorMessage = null
                    currentScreen = AppScreen.SPLASH
                    loadContent()
                }
                is ApiResult.Error -> {
                    kickoutError = result.message
                }
            }
            kickoutLoading = false
        }
    }

    // ── Logout handler ──
    fun doLogout() {
        scope.launch {
            val token = sessionManager.sessionToken
            if (token != null) {
                ApiService.logout(token)
            }
            ApiService.clearCookies()
            sessionManager.clearSession()
            subscriber = null
            subscriptionInfo = null
            allChannels = emptyList()
            channels = emptyList()
            categories = emptyList()
            languages = emptyList()
            selectedChannelIndex = 0
            showSettingsPanel = false
            showSessionManager = false
            currentScreen = AppScreen.LOGIN
        }
    }

    // Trigger content loading when on SPLASH screen
    LaunchedEffect(Unit) {
        if (currentScreen == AppScreen.SPLASH) {
            loadContent()
        }
    }

    // ── Overlay auto-hide ──
    LaunchedEffect(showPlayerInfoOverlay) {
        if (showPlayerInfoOverlay) { delay(3000); showPlayerInfoOverlay = false }
    }
    LaunchedEffect(showChannelChangeOverlay) {
        if (showChannelChangeOverlay) { delay(4000); showChannelChangeOverlay = false }
    }
    LaunchedEffect(showControlsHint) {
        if (showControlsHint) { delay(5000); showControlsHint = false }
    }
    LaunchedEffect(showVolumeOverlay) {
        if (showVolumeOverlay) { delay(3000); showVolumeOverlay = false }
    }
    LaunchedEffect(selectedChannelIndex) {
        if (channels.isNotEmpty()) showChannelChangeOverlay = true
    }
    LaunchedEffect(selectedChannelIndex, showChannelList) {
        if (channels.isNotEmpty() && showChannelList) {
            listState.animateScrollToItem(selectedChannelIndex)
        }
    }

    // ── Render current screen ──
    when (currentScreen) {
        AppScreen.SPLASH -> {
            IntroUi(
                isLoading = isLoading,
                message = loadingMessage,
                errorMessage = errorMessage,
                onRetry = {
                    errorMessage = null
                    isLoading = true
                    loadingMessage = "Retrying..."
                    isContentLoading = false
                    loadContent()
                }
            )
        }

        AppScreen.LOGIN -> {
            LoginScreen(
                isLoading = loginLoading,
                errorMessage = loginError,
                savedPhone = sessionManager.savedPhone,
                savedPin = sessionManager.savedPin,
                savedRememberMe = sessionManager.rememberMe,
                onLogin = { phone, pin, rememberMe ->
                    if (rememberMe) {
                        sessionManager.saveCredentials(phone, pin)
                    } else {
                        sessionManager.clearCredentials()
                    }
                    doLogin(phone, pin)
                }
            )
        }

        AppScreen.SESSION_KICKOUT -> {
            SessionKickoutScreen(
                sessions = kickoutSessions,
                isLoading = kickoutLoading,
                errorMessage = kickoutError,
                onReplaceDevice = { sessionId -> doReplaceDevice(sessionId) },
                onLogout = { doLogout() }
            )
        }

        AppScreen.PLAYER -> {
            PlayerScreen(
                channels = channels,
                selectedChannelIndex = selectedChannelIndex,
                onChannelIndexChange = { selectedChannelIndex = it },
                showChannelList = showChannelList,
                onShowChannelListChange = { showChannelList = it },
                showPlayerInfoOverlay = showPlayerInfoOverlay,
                onShowPlayerInfoOverlayChange = { showPlayerInfoOverlay = it },
                showChannelChangeOverlay = showChannelChangeOverlay,
                showControlsHint = showControlsHint,
                onShowControlsHintChange = { showControlsHint = it },
                showExitDialog = showExitDialog,
                onShowExitDialogChange = { showExitDialog = it },
                showSettingsPanel = showSettingsPanel,
                onShowSettingsPanelChange = { showSettingsPanel = it },
                showVolumeOverlay = showVolumeOverlay,
                onShowVolumeOverlayChange = { showVolumeOverlay = it },
                showSessionManager = showSessionManager,
                onShowSessionManagerChange = { showSessionManager = it },
                currentExoPlayer = currentExoPlayer,
                onExoPlayerReady = { currentExoPlayer = it },
                audioManager = audioManager,
                currentVolume = currentVolume,
                onVolumeChange = { currentVolume = it },
                subscriber = subscriber,
                subscriptionInfo = subscriptionInfo,
                categories = categories,
                languages = languages,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it },
                selectedLanguageId = selectedLanguageId,
                onLanguageSelected = { selectedLanguageId = it },
                listState = listState,
                sessionManager = sessionManager,
                onLogout = { doLogout() },
                focusRequester = focusRequester
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    channels: List<Channel>,
    selectedChannelIndex: Int,
    onChannelIndexChange: (Int) -> Unit,
    showChannelList: Boolean,
    onShowChannelListChange: (Boolean) -> Unit,
    showPlayerInfoOverlay: Boolean,
    onShowPlayerInfoOverlayChange: (Boolean) -> Unit,
    showChannelChangeOverlay: Boolean,
    showControlsHint: Boolean,
    onShowControlsHintChange: (Boolean) -> Unit,
    showExitDialog: Boolean,
    onShowExitDialogChange: (Boolean) -> Unit,
    showSettingsPanel: Boolean,
    onShowSettingsPanelChange: (Boolean) -> Unit,
    showVolumeOverlay: Boolean,
    onShowVolumeOverlayChange: (Boolean) -> Unit,
    showSessionManager: Boolean,
    onShowSessionManagerChange: (Boolean) -> Unit,
    currentExoPlayer: ExoPlayer?,
    onExoPlayerReady: (ExoPlayer) -> Unit,
    audioManager: AudioManager,
    currentVolume: Int,
    onVolumeChange: (Int) -> Unit,
    subscriber: Subscriber?,
    subscriptionInfo: SubscriptionInfo?,
    categories: List<Category>,
    languages: List<Language>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    selectedLanguageId: Int?,
    onLanguageSelected: (Int?) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    focusRequester: FocusRequester
) {
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var dragStartPosition by remember { mutableStateOf(Offset.Zero) }

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
                            onVolumeChange(newVolume)
                            onShowVolumeOverlayChange(true)
                            true
                        }
                        KeyEvent.KEYCODE_VOLUME_DOWN -> {
                            val newVolume = (currentVolume - 1).coerceAtLeast(0)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                            onVolumeChange(newVolume)
                            onShowVolumeOverlayChange(true)
                            true
                        }
                        KeyEvent.KEYCODE_VOLUME_MUTE -> {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, 0)
                            onVolumeChange(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
                            onShowVolumeOverlayChange(true)
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (showExitDialog) false
                            else if (channels.isNotEmpty()) {
                                onChannelIndexChange(if (selectedChannelIndex > 0) selectedChannelIndex - 1 else channels.size - 1)
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (showExitDialog) false
                            else if (channels.isNotEmpty()) {
                                onChannelIndexChange(if (selectedChannelIndex < channels.size - 1) selectedChannelIndex + 1 else 0)
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (showExitDialog) false
                            else if (showChannelList || showSettingsPanel) {
                                onShowChannelListChange(false)
                                onShowSettingsPanelChange(false)
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            when {
                                showExitDialog -> false
                                showSessionManager -> { onShowSessionManagerChange(false); true }
                                showChannelList || showSettingsPanel -> {
                                    onShowChannelListChange(false)
                                    onShowSettingsPanelChange(false)
                                    true
                                }
                                else -> { onShowExitDialogChange(true); true }
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (showExitDialog) false
                            else {
                                onShowChannelListChange(!showChannelList)
                                if (!showChannelList) onShowControlsHintChange(true)
                                true
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (showExitDialog) false
                            else if (!showChannelList) {
                                onShowSettingsPanelChange(!showSettingsPanel)
                                if (!showSettingsPanel) onShowControlsHintChange(true)
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            }
            .focusRequester(focusRequester)
            .focusable()
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: Channel List Sidebar
            if (showChannelList) {
                ChannelListSidebar(
                    channels = channels,
                    selectedChannelIndex = selectedChannelIndex,
                    onChannelSelected = { index ->
                        onChannelIndexChange(index)
                        onShowChannelListChange(false)
                    },
                    listState = listState,
                    categories = categories,
                    languages = languages,
                    selectedCategoryId = selectedCategoryId,
                    selectedLanguageId = selectedLanguageId,
                    onCategorySelected = onCategorySelected,
                    onLanguageSelected = onLanguageSelected,
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight()
                        .padding(12.dp)
                )
            }

            // Center: Video Player
            Box(modifier = Modifier.weight(1f)) {
                channels.getOrNull(selectedChannelIndex)?.let { channel ->
                    ExoPlayerView(
                        channel = channel,
                        onPlayerReady = onExoPlayerReady,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { offset ->
                                        if (offset.x < size.width * 0.3f) {
                                            onShowChannelListChange(!showChannelList)
                                        } else if (offset.x > size.width * 0.7f) {
                                            onShowSettingsPanelChange(!showSettingsPanel)
                                        } else {
                                            onShowPlayerInfoOverlayChange(true)
                                            onShowControlsHintChange(true)
                                        }
                                    },
                                    onDoubleTap = { onShowChannelListChange(!showChannelList) }
                                )
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        isDragging = true
                                        dragStartPosition = offset
                                    },
                                    onDragEnd = { isDragging = false }
                                ) { change, _ ->
                                    val deltaY = change.position.y - dragStartPosition.y
                                    val deltaX = change.position.x - dragStartPosition.x

                                    if (abs(deltaY) > SWIPE_THRESHOLD && abs(deltaY) > abs(deltaX)) {
                                        if (deltaY > 0) {
                                            onChannelIndexChange(if (selectedChannelIndex > 0) selectedChannelIndex - 1 else channels.size - 1)
                                        } else {
                                            onChannelIndexChange(if (selectedChannelIndex < channels.size - 1) selectedChannelIndex + 1 else 0)
                                        }
                                        dragStartPosition = change.position
                                    }

                                    if (dragStartPosition.x < size.width * LEFT_DRAG_ZONE &&
                                        deltaX > SWIPE_THRESHOLD &&
                                        abs(deltaX) > abs(deltaY)
                                    ) {
                                        onShowChannelListChange(true)
                                        isDragging = false
                                    }
                                }
                            }
                    )
                }

                // Overlays
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

                // Right: Settings Panel
                PlayerSettingsPanel(
                    show = showSettingsPanel && !showChannelList,
                    subscriber = subscriber,
                    channel = channels.getOrNull(selectedChannelIndex),
                    exoPlayer = currentExoPlayer,
                    subscriptionStatus = subscriptionInfo?.status,
                    expiredIn = subscriptionInfo?.expiredIn,
                    onLogout = onLogout,
                    onManageSessions = {
                        onShowSessionManagerChange(true)
                        onShowSettingsPanelChange(false)
                    },
                    onDismiss = { onShowSettingsPanelChange(false) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )

                if (showControlsHint && !showChannelList && !showPlayerInfoOverlay && !showSettingsPanel) {
                    TvControlsHint(modifier = Modifier.align(Alignment.TopEnd))
                }

                ExitDialog(
                    showExitDialog = showExitDialog,
                    onDismiss = { onShowExitDialogChange(false) }
                )
            }
        }

        // Session manager overlay
        if (showSessionManager) {
            var sessionsList by remember { mutableStateOf<List<SessionInfo>>(emptyList()) }
            var sessionsLoading by remember { mutableStateOf(true) }
            var sessionsError by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(showSessionManager) {
                if (showSessionManager) {
                    sessionsLoading = true
                    val token = sessionManager.sessionToken ?: return@LaunchedEffect
                    when (val result = ApiService.getSessions(token)) {
                        is ApiResult.Success -> {
                            sessionsList = result.data.sessions
                            sessionsLoading = false
                        }
                        is ApiResult.Error -> {
                            sessionsError = result.message
                            sessionsLoading = false
                        }
                    }
                }
            }

            SessionKickoutScreen(
                sessions = sessionsList,
                isLoading = sessionsLoading,
                errorMessage = sessionsError,
                onReplaceDevice = { sessionId ->
                    scope.launch {
                        val token = sessionManager.sessionToken ?: return@launch
                        when (ApiService.kickDevice(token, sessionId)) {
                            is ApiResult.Success -> {
                                sessionsList = sessionsList.filter { it.id != sessionId }
                            }
                            is ApiResult.Error -> {
                                sessionsError = "Failed to kick device"
                            }
                        }
                    }
                },
                onLogout = { onShowSessionManagerChange(false) }
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
