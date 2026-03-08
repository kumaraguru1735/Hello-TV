package com.shadow.hellotv

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.shadow.hellotv.ui.common.LoginScreen
import com.shadow.hellotv.ui.common.SessionKickoutScreen
import com.shadow.hellotv.ui.mobile.MobilePlayerScreen
import com.shadow.hellotv.ui.theme.HelloTVTheme
import com.shadow.hellotv.ui.theme.IntroUi
import com.shadow.hellotv.ui.tv.TvPlayerScreen
import com.shadow.hellotv.utils.KeepScreenOn
import com.shadow.hellotv.viewmodel.AppScreen
import com.shadow.hellotv.viewmodel.MainViewModel

val LocalIsTv = staticCompositionLocalOf { false }

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

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        enableEdgeToEdge()

        val isTv = packageManager.hasSystemFeature("android.software.leanback")

        setContent {
            CompositionLocalProvider(LocalIsTv provides isTv) {
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
}

@OptIn(UnstableApi::class)
@Composable
fun HelloTVApp() {
    val vm: MainViewModel = viewModel()
    val isTv = LocalIsTv.current
    val activity = LocalContext.current as? ComponentActivity

    // Set orientation based on screen and device
    val currentScreen = vm.currentScreen
    val isFullscreen = vm.isFullscreen
    LaunchedEffect(currentScreen, isFullscreen, isTv) {
        val orient = if (isTv) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            when {
                currentScreen == AppScreen.PLAYER && isFullscreen ->
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        if (activity?.requestedOrientation != orient) {
            activity?.requestedOrientation = orient
        }
    }

    // Back handler for player
    BackHandler(vm.currentScreen == AppScreen.PLAYER && !isTv) {
        when {
            vm.showSessionManager -> vm.showSessionManager = false
            vm.showChannelList -> vm.showChannelList = false
            vm.showSettingsPanel -> vm.showSettingsPanel = false
            vm.showExitDialog -> vm.showExitDialog = false
            vm.isFullscreen -> vm.isFullscreen = false
            else -> vm.showExitDialog = true
        }
    }

    // Filter channels when selection changes
    LaunchedEffect(vm.selectedCategoryId, vm.selectedLanguageId, vm.allChannels) {
        vm.applyFilters()
    }

    when (vm.currentScreen) {
        AppScreen.SPLASH -> {
            IntroUi(
                isLoading = vm.isLoading,
                message = vm.loadingMessage,
                errorMessage = vm.errorMessage,
                onRetry = {
                    vm.errorMessage = null
                    vm.isLoading = true
                    vm.loadingMessage = "Retrying..."
                    vm.loadContent()
                }
            )
        }

        AppScreen.LOGIN -> {
            LoginScreen(
                isLoading = vm.loginLoading,
                errorMessage = vm.loginError,
                savedPhone = vm.sessionManager.savedPhone,
                savedPin = vm.sessionManager.savedPin,
                savedRememberMe = vm.sessionManager.rememberMe,
                onLogin = { phone, pin, rememberMe -> vm.doLogin(phone, pin, rememberMe) }
            )
        }

        AppScreen.SESSION_KICKOUT -> {
            SessionKickoutScreen(
                sessions = vm.kickoutSessions,
                isLoading = vm.kickoutLoading,
                errorMessage = vm.kickoutError,
                onReplaceDevice = { vm.doReplaceDevice(it) },
                onLogout = { vm.doLogout() }
            )
        }

        AppScreen.PLAYER -> {
            if (isTv) {
                TvPlayerScreen(vm = vm)
            } else {
                MobilePlayerScreen(vm = vm)
            }
        }
    }
}
