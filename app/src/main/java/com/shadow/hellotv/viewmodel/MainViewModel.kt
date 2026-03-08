package com.shadow.hellotv.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.shadow.hellotv.data.local.AppDatabase
import com.shadow.hellotv.data.repository.ChannelRepository
import com.shadow.hellotv.model.*
import com.shadow.hellotv.network.ApiResult
import com.shadow.hellotv.network.ApiService
import com.shadow.hellotv.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "HelloTV"
private fun log(msg: String) { Log.e(TAG, msg); println("[$TAG] $msg") }

enum class AppScreen { SPLASH, LOGIN, SESSION_KICKOUT, PLAYER }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val sessionManager = SessionManager(application)
    val repository = ChannelRepository(AppDatabase.getInstance(application))

    // ── Navigation ──
    var currentScreen by mutableStateOf(
        if (sessionManager.isLoggedIn) AppScreen.SPLASH else AppScreen.LOGIN
    )

    // ── Loading ──
    var isLoading by mutableStateOf(true)
    var loadingMessage by mutableStateOf("Initializing...")
    var errorMessage by mutableStateOf<String?>(null)

    // ── Auth ──
    var loginError by mutableStateOf<String?>(null)
    var loginLoading by mutableStateOf(false)
    var kickoutSessions by mutableStateOf<List<SessionInfo>>(emptyList())
    var kickoutLoading by mutableStateOf(false)
    var kickoutError by mutableStateOf<String?>(null)

    // ── Data ──
    var subscriber by mutableStateOf<Subscriber?>(null)
    var subscriptionInfo by mutableStateOf<SubscriptionInfo?>(null)
    var allChannels by mutableStateOf<List<Channel>>(emptyList())
    var channels by mutableStateOf<List<Channel>>(emptyList())
    var categories by mutableStateOf<List<Category>>(emptyList())
    var languages by mutableStateOf<List<Language>>(emptyList())

    // ── Filters ──
    var selectedCategoryId by mutableStateOf<Int?>(null)
    var selectedLanguageId by mutableStateOf<Int?>(null)

    // ── Player ──
    var selectedChannelIndex by mutableIntStateOf(0)
    var showChannelList by mutableStateOf(false)
    var showPlayerInfoOverlay by mutableStateOf(false)
    var showChannelChangeOverlay by mutableStateOf(false)
    var showControlsHint by mutableStateOf(true)
    var showExitDialog by mutableStateOf(false)
    var showSettingsPanel by mutableStateOf(false)
    var showVolumeOverlay by mutableStateOf(false)
    var showSessionManager by mutableStateOf(false)
    var isFullscreen by mutableStateOf(false)
    var currentExoPlayer by mutableStateOf<ExoPlayer?>(null)

    private var isContentLoading = false

    init {
        if (currentScreen == AppScreen.SPLASH) {
            loadContent()
        }
    }

    fun applyFilters() {
        channels = allChannels.filter { ch ->
            (selectedCategoryId == null || ch.categoryId == selectedCategoryId) &&
                    (selectedLanguageId == null || ch.languageId == selectedLanguageId)
        }
        if (selectedChannelIndex >= channels.size) {
            selectedChannelIndex = 0
        }
    }

    fun loadContent() {
        if (isContentLoading) return
        isContentLoading = true
        viewModelScope.launch {
            if (!sessionManager.isLoggedIn || sessionManager.sessionToken == null) {
                isContentLoading = false
                currentScreen = AppScreen.LOGIN
                return@launch
            }

            var token = sessionManager.sessionToken!!
            log("Loading content with token: ${token.take(8)}...")

            // Try loading from local DB first for instant display
            if (repository.hasChannels()) {
                log("Loading cached data from DB...")
                allChannels = repository.getChannels()
                categories = repository.getCategories()
                languages = repository.getLanguages()
                applyFilters()
                if (allChannels.isNotEmpty()) {
                    isLoading = false
                    currentScreen = AppScreen.PLAYER
                }
            }

            // Then sync from API
            loadingMessage = "Loading channels..."
            when (val result = ApiService.getChannels(token)) {
                is ApiResult.Success -> {
                    allChannels = result.data.channels
                    if (result.data.subscriber != null) subscriber = result.data.subscriber
                    subscriptionInfo = result.data.subscriptionInfo
                    repository.saveChannels(result.data.channels)
                    log("Loaded ${result.data.channels.size} channels")
                }
                is ApiResult.Error -> {
                    log("Channel load failed: ${result.errorCode} - ${result.message}")
                    if (result.errorCode == "INVALID_SESSION" || result.errorCode == "SESSION_EXPIRED" ||
                        result.errorCode == "ACCOUNT_INACTIVE" || result.errorCode == "NO_SESSION"
                    ) {
                        // Try auto re-login
                        if (sessionManager.rememberMe && !sessionManager.savedPhone.isNullOrEmpty() && !sessionManager.savedPin.isNullOrEmpty()) {
                            log("Session lost, attempting auto re-login...")
                            loadingMessage = "Re-authenticating..."
                            ApiService.clearCookies()
                            val loginResult = ApiService.login(
                                LoginRequest(
                                    phone = sessionManager.savedPhone!!,
                                    pin = sessionManager.savedPin!!,
                                    deviceId = sessionManager.getDeviceId(),
                                    deviceType = sessionManager.getDeviceType(),
                                    deviceModel = sessionManager.getDeviceModel(),
                                    deviceName = sessionManager.getDeviceName()
                                )
                            )
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
                                    val retryResult = ApiService.getChannels(token)
                                    when (retryResult) {
                                        is ApiResult.Success -> {
                                            allChannels = retryResult.data.channels
                                            if (retryResult.data.subscriber != null) subscriber = retryResult.data.subscriber
                                            subscriptionInfo = retryResult.data.subscriptionInfo
                                            repository.saveChannels(retryResult.data.channels)
                                            log("Re-login success, loaded ${retryResult.data.channels.size} channels")
                                        }
                                        is ApiResult.Error -> {
                                            if (allChannels.isEmpty()) {
                                                sessionManager.clearSession()
                                                isContentLoading = false
                                                currentScreen = AppScreen.LOGIN
                                                return@launch
                                            }
                                        }
                                    }
                                }
                                is ApiResult.Error -> {
                                    if (allChannels.isEmpty()) {
                                        sessionManager.clearSession()
                                        isContentLoading = false
                                        currentScreen = AppScreen.LOGIN
                                        return@launch
                                    }
                                }
                            }
                        } else if (allChannels.isEmpty()) {
                            sessionManager.clearSession()
                            isContentLoading = false
                            currentScreen = AppScreen.LOGIN
                            return@launch
                        }
                    } else if (allChannels.isEmpty()) {
                        errorMessage = "Failed to load channels: ${result.message}"
                    }
                }
            }

            loadingMessage = "Loading categories..."
            when (val catResult = ApiService.getCategories(token)) {
                is ApiResult.Success -> {
                    categories = catResult.data.categories
                    repository.saveCategories(catResult.data.categories)
                    log("Loaded ${catResult.data.categories.size} categories")
                }
                is ApiResult.Error -> log("Categories failed: ${catResult.message}")
            }

            loadingMessage = "Loading languages..."
            when (val langResult = ApiService.getLanguages(token)) {
                is ApiResult.Success -> {
                    languages = langResult.data.languages
                    repository.saveLanguages(langResult.data.languages)
                    log("Loaded ${langResult.data.languages.size} languages")
                }
                is ApiResult.Error -> log("Languages failed: ${langResult.message}")
            }

            applyFilters()

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

    fun doLogin(phone: String, pin: String, rememberMe: Boolean) {
        viewModelScope.launch {
            loginLoading = true
            loginError = null
            ApiService.clearCookies()

            if (rememberMe) {
                sessionManager.saveCredentials(phone, pin)
            } else {
                sessionManager.clearCredentials()
            }

            log("Attempting login for phone: $phone")
            val request = LoginRequest(
                phone = phone, pin = pin,
                deviceId = sessionManager.getDeviceId(),
                deviceType = sessionManager.getDeviceType(),
                deviceModel = sessionManager.getDeviceModel(),
                deviceName = sessionManager.getDeviceName()
            )

            when (val result = ApiService.login(request)) {
                is ApiResult.Success -> {
                    log("Login successful, session type: ${result.data.sessionType}")
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
                        kickoutSessions = data.currentSessions
                        currentScreen = AppScreen.SESSION_KICKOUT
                    } else {
                        isLoading = true
                        loadingMessage = "Loading channels..."
                        errorMessage = null
                        currentScreen = AppScreen.SPLASH
                        loadContent()
                    }
                }
                is ApiResult.Error -> {
                    log("Login failed: ${result.errorCode} - ${result.message}")
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

    fun doReplaceDevice(targetSessionId: Int) {
        viewModelScope.launch {
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
                is ApiResult.Error -> kickoutError = result.message
            }
            kickoutLoading = false
        }
    }

    fun doLogout() {
        viewModelScope.launch {
            val token = sessionManager.sessionToken
            if (token != null) ApiService.logout(token)
            ApiService.clearCookies()
            sessionManager.clearSession()
            repository.clearAll()
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

    fun selectChannel(index: Int) {
        selectedChannelIndex = index.coerceIn(0, (channels.size - 1).coerceAtLeast(0))
    }

    fun nextChannel() {
        if (channels.isNotEmpty()) {
            selectedChannelIndex = if (selectedChannelIndex < channels.size - 1) selectedChannelIndex + 1 else 0
        }
    }

    fun previousChannel() {
        if (channels.isNotEmpty()) {
            selectedChannelIndex = if (selectedChannelIndex > 0) selectedChannelIndex - 1 else channels.size - 1
        }
    }

    fun jumpToChannelNumber(channelNo: Int) {
        val index = channels.indexOfFirst { it.channelNo == channelNo }
        if (index >= 0) selectedChannelIndex = index
    }
}
