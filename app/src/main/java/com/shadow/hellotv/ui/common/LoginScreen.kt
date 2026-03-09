package com.shadow.hellotv.ui.common

import android.view.KeyEvent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadow.hellotv.LocalIsTv
import com.shadow.hellotv.ui.theme.*

enum class ActiveField { PHONE, PIN }

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    savedPhone: String? = null,
    savedPin: String? = null,
    savedRememberMe: Boolean = false,
    onLogin: (phone: String, pin: String, rememberMe: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var phone by remember { mutableStateOf(savedPhone ?: "") }
    var pin by remember { mutableStateOf(savedPin ?: "") }
    var rememberMe by remember { mutableStateOf(savedRememberMe) }
    var activeField by remember { mutableStateOf(ActiveField.PHONE) }
    var showPin by remember { mutableStateOf(false) }
    val isTv = LocalIsTv.current

    // Focus requesters for TV navigation
    val phoneFocus = remember { FocusRequester() }
    val pinFocus = remember { FocusRequester() }
    val rememberFocus = remember { FocusRequester() }
    val loginFocus = remember { FocusRequester() }

    fun onDigit(digit: Char) {
        when (activeField) {
            ActiveField.PHONE -> if (phone.length < 15) phone += digit
            ActiveField.PIN -> if (pin.length < 10) pin += digit
        }
    }
    fun onBackspace() {
        when (activeField) {
            ActiveField.PHONE -> if (phone.isNotEmpty()) phone = phone.dropLast(1)
            ActiveField.PIN -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
        }
    }
    fun onClear() {
        when (activeField) {
            ActiveField.PHONE -> phone = ""
            ActiveField.PIN -> pin = ""
        }
    }

    val config = LocalConfiguration.current
    val screenH = config.screenHeightDp
    val screenW = config.screenWidthDp
    val isPortrait = screenH > screenW

    // Auto-focus phone field on TV
    LaunchedEffect(Unit) {
        if (isTv) phoneFocus.requestFocus()
    }

    // ── Shared composable content for form ──
    @Composable
    fun LoginFormContent() {
        // ── Brand header ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hello", color = AccentGold, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
            Text("TV", color = AccentGoldLight, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AccentGoldSoft)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("LOGIN", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }
        }
        Spacer(Modifier.height(14.dp))

        // ── Phone input ──
        GlassInput(
            value = phone, placeholder = "Phone Number", isActive = activeField == ActiveField.PHONE,
            onClick = { activeField = ActiveField.PHONE }, leadingIcon = Icons.Default.Phone,
            focusRequester = phoneFocus, onFocusGained = { activeField = ActiveField.PHONE },
            onDpadDown = { pinFocus.requestFocus() }
        )
        Spacer(Modifier.height(8.dp))

        // ── PIN input ──
        GlassInput(
            value = if (!showPin && pin.isNotEmpty()) "\u25CF".repeat(pin.length) else pin,
            placeholder = "PIN", isActive = activeField == ActiveField.PIN,
            onClick = { activeField = ActiveField.PIN }, leadingIcon = Icons.Default.Lock,
            focusRequester = pinFocus, onFocusGained = { activeField = ActiveField.PIN },
            onDpadUp = { phoneFocus.requestFocus() }, onDpadDown = { rememberFocus.requestFocus() },
            trailingContent = {
                Icon(
                    if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility, null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp).clickable { showPin = !showPin }
                )
            }
        )
        Spacer(Modifier.height(8.dp))

        // ── Remember Me ──
        var rememberFocused by remember { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .then(if (rememberFocused) Modifier.border(1.dp, AccentGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp)) else Modifier)
                .clickable { rememberMe = !rememberMe }
                .focusRequester(rememberFocus).onFocusChanged { rememberFocused = it.isFocused }.focusable()
                .onKeyEvent { event ->
                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_UP -> { pinFocus.requestFocus(); true }
                            KeyEvent.KEYCODE_DPAD_DOWN -> { loginFocus.requestFocus(); true }
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { rememberMe = !rememberMe; true }
                            else -> false
                        }
                    } else false
                }
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier.size(18.dp).clip(RoundedCornerShape(5.dp))
                    .background(if (rememberMe) Brush.horizontalGradient(listOf(GradientGoldStart, GradientGoldEnd)) else Brush.horizontalGradient(listOf(SurfaceInput, SurfaceInput)))
                    .border(1.dp, if (rememberMe) Color.Transparent else SurfaceSeparator, RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (rememberMe) Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(13.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text("Remember Me", color = if (rememberFocused) TextSecondary else TextMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))

        // ── Login button ──
        val canLogin = phone.isNotEmpty() && pin.isNotEmpty() && !isLoading
        var loginBtnFocused by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxWidth().height(44.dp)
                .shadow(if (loginBtnFocused || canLogin) 16.dp else 0.dp, RoundedCornerShape(12.dp), spotColor = AccentGold.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(12.dp))
                .background(if (canLogin) Brush.horizontalGradient(listOf(GradientGoldStart, GradientGoldEnd)) else Brush.horizontalGradient(listOf(SurfaceInput, SurfaceCard)))
                .then(if (loginBtnFocused) Modifier.border(2.dp, AccentGoldLight, RoundedCornerShape(12.dp)) else Modifier)
                .focusRequester(loginFocus).onFocusChanged { loginBtnFocused = it.isFocused }.focusable()
                .onKeyEvent { event ->
                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_UP -> { rememberFocus.requestFocus(); true }
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { if (canLogin) onLogin(phone, pin, rememberMe); true }
                            else -> false
                        }
                    } else false
                }
                .clickable(enabled = canLogin) { onLogin(phone, pin, rememberMe) }
                .scale(if (loginBtnFocused) 1.02f else 1f),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Continue", color = if (canLogin) Color.Black else TextMuted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp)
                    if (canLogin) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, null, tint = Color.Black.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ── Error ──
        AnimatedVisibility(visible = errorMessage != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            errorMessage?.let {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Default.ErrorOutline, null, tint = StatusLive, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(it, color = StatusLive, fontSize = 12.sp)
                }
            }
        }
    }

    // ── Background ──
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceDark)
    ) {
        // Decorative ambient glow
        Box(
            modifier = Modifier.size(500.dp).offset(x = (-150).dp, y = (-150).dp)
                .background(Brush.radialGradient(colors = listOf(AccentGold.copy(alpha = 0.06f), Color.Transparent)))
        )
        Box(
            modifier = Modifier.size(400.dp).align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp)
                .background(Brush.radialGradient(colors = listOf(GradientGoldEnd.copy(alpha = 0.04f), Color.Transparent)))
        )

        if (isPortrait && !isTv) {
            // ═══════════════════════════════════
            // PORTRAIT MOBILE: Vertical stacked layout
            // ═══════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glass card with form
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(SurfaceElevated, SurfacePrimary)))
                        .border(1.dp, Brush.verticalGradient(listOf(AccentGold.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f))), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoginFormContent()
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Active field indicator
                Text(
                    when (activeField) {
                        ActiveField.PHONE -> "Entering Phone"
                        ActiveField.PIN -> "Entering PIN"
                    },
                    color = AccentGold.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(6.dp))

                // Number pad
                NumberPad(
                    onDigit = ::onDigit,
                    onBackspace = ::onBackspace,
                    onClear = ::onClear,
                    screenHeight = screenH
                )
            }
        } else {
            // ═══════════════════════════════════
            // LANDSCAPE / TV: Horizontal layout
            // ═══════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = if (screenH < 400) 12.dp else 24.dp, vertical = if (screenH < 400) 6.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (screenH < 400) 12.dp else 20.dp)
            ) {
                // LEFT: Login form in glass card
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = AccentGold.copy(alpha = 0.10f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(SurfaceElevated, SurfacePrimary)))
                            .border(1.dp, Brush.verticalGradient(listOf(AccentGold.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f))), RoundedCornerShape(24.dp))
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LoginFormContent()
                        }
                    }
                }

                // RIGHT: Number Pad
                Column(
                    modifier = Modifier.weight(if (screenH < 400) 0.65f else 0.75f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        when (activeField) {
                            ActiveField.PHONE -> "Entering Phone"
                            ActiveField.PIN -> "Entering PIN"
                        },
                        color = AccentGold.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    NumberPad(
                        onDigit = ::onDigit,
                        onBackspace = ::onBackspace,
                        onClear = ::onClear,
                        screenHeight = screenH
                    )
                }
            }
        }
    }
}

// ── Glass Input Field ──

@Composable
private fun GlassInput(
    value: String,
    placeholder: String,
    isActive: Boolean,
    onClick: () -> Unit,
    leadingIcon: ImageVector,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onFocusGained: () -> Unit = {},
    onDpadUp: (() -> Unit)? = null,
    onDpadDown: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    var focused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) SurfaceInput else SurfaceCard.copy(alpha = 0.6f))
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                brush = if (isActive)
                    Brush.horizontalGradient(listOf(AccentGold, GradientGoldEnd))
                else
                    Brush.horizontalGradient(listOf(SurfaceSeparator, SurfaceSeparator)),
                shape = RoundedCornerShape(12.dp)
            )
            .focusRequester(focusRequester)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocusGained()
            }
            .focusable()
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_UP -> { onDpadUp?.invoke(); onDpadUp != null }
                        KeyEvent.KEYCODE_DPAD_DOWN -> { onDpadDown?.invoke(); onDpadDown != null }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { onClick(); true }
                        else -> false
                    }
                } else false
            }
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            leadingIcon, null,
            tint = if (isActive) AccentGold else TextMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
            Row {
                Text(
                    value,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    letterSpacing = if (placeholder == "PIN") 2.sp else 0.sp
                )
                if (isActive) {
                    Text(
                        "\u2502",
                        color = AccentGold.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
        trailingContent?.invoke()
    }
}

// ── Number Pad ──

@Composable
private fun NumberPad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    screenHeight: Int
) {
    val keys = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('C', '0', '\u232B')
    )

    val availableH = (screenHeight * 0.72f).toInt()
    val gap = 5
    val btnSize = ((availableH - gap * 3) / 4).coerceIn(38, 56)

    Column(
        verticalArrangement = Arrangement.spacedBy(gap.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap.dp)) {
                row.forEach { key ->
                    PadKey(key = key, size = btnSize.dp) {
                        when (key) {
                            'C' -> onClear()
                            '\u232B' -> onBackspace()
                            else -> onDigit(key)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PadKey(
    key: Char,
    size: Dp,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                if (isFocused) 8.dp else 0.dp,
                CircleShape,
                spotColor = AccentGold.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(
                when {
                    isFocused && key == 'C' -> Brush.radialGradient(listOf(StatusLive, StatusLive.copy(alpha = 0.8f)))
                    isFocused && key == '\u232B' -> Brush.radialGradient(listOf(AccentGold, AccentGoldDark))
                    isFocused -> Brush.radialGradient(listOf(GradientGoldStart, GradientGoldEnd))
                    key == 'C' -> Brush.radialGradient(listOf(StatusLive.copy(alpha = 0.08f), StatusLive.copy(alpha = 0.04f)))
                    key == '\u232B' -> Brush.radialGradient(listOf(AccentGold.copy(alpha = 0.08f), AccentGold.copy(alpha = 0.04f)))
                    else -> Brush.radialGradient(listOf(SurfaceCard, SurfacePrimary))
                }
            )
            .border(
                1.dp,
                when {
                    isFocused -> Color.Transparent
                    key == 'C' -> StatusLive.copy(alpha = 0.12f)
                    key == '\u232B' -> AccentGold.copy(alpha = 0.12f)
                    else -> SurfaceSeparator.copy(alpha = 0.5f)
                },
                CircleShape
            )
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            '\u232B' -> Icon(
                Icons.AutoMirrored.Filled.Backspace, "Backspace",
                tint = if (isFocused) Color.Black else AccentGold,
                modifier = Modifier.size(if (isFocused) 22.dp else 20.dp)
            )
            'C' -> Text(
                "CLR",
                color = if (isFocused) TextPrimary else StatusLive,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            else -> Text(
                key.toString(),
                color = if (isFocused) Color.Black else TextPrimary,
                fontSize = 18.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
