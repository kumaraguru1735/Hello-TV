package com.shadow.hellotv.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(HotstarNavy, SurfaceDark, Color(0xFF0A0F1C))
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Left Side: Form ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                AsyncImage(
                    model = "https://iptv.helloiptv.in/assets/images/logo.png",
                    contentDescription = "HelloTV",
                    modifier = Modifier.height(48.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Welcome Back",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Sign in to continue watching",
                    color = TextMuted,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(24.dp))

                // Phone field
                InputField(
                    label = "Phone Number",
                    value = phone,
                    isActive = activeField == ActiveField.PHONE,
                    onClick = { activeField = ActiveField.PHONE },
                    icon = { Icon(Icons.Default.Phone, null, tint = if (activeField == ActiveField.PHONE) HotstarBlue else TextMuted) },
                    masked = false
                )

                Spacer(Modifier.height(12.dp))

                // PIN field
                InputField(
                    label = "PIN",
                    value = pin,
                    isActive = activeField == ActiveField.PIN,
                    onClick = { activeField = ActiveField.PIN },
                    icon = { Icon(Icons.Default.Lock, null, tint = if (activeField == ActiveField.PIN) HotstarBlue else TextMuted) },
                    masked = !showPin,
                    trailing = {
                        IconButton(onClick = { showPin = !showPin }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = TextMuted, modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Remember Me
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { rememberMe = !rememberMe }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = HotstarBlue,
                            uncheckedColor = TextMuted
                        )
                    )
                    Text("Remember Me", color = TextSecondary, fontSize = 14.sp)
                }

                Spacer(Modifier.height(16.dp))

                // Login button
                Button(
                    onClick = { onLogin(phone, pin, rememberMe) },
                    enabled = phone.isNotEmpty() && pin.isNotEmpty() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HotstarBlue,
                        disabledContainerColor = HotstarBlue.copy(alpha = 0.4f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Error message
                AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    errorMessage?.let {
                        Text(
                            it,
                            color = StatusLive,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Right Side: Number Pad ──
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumberPad(
                    onDigit = ::onDigit,
                    onBackspace = ::onBackspace,
                    onClear = ::onClear
                )
            }
        }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    masked: Boolean,
    trailing: (@Composable () -> Unit)? = null
) {
    val borderColor = if (isActive) HotstarBlue else SurfaceElevated

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (masked) "●".repeat(value.length) else value,
                color = if (value.isEmpty()) TextDisabled else TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            // Blinking cursor
            if (isActive) {
                Text("|", color = HotstarBlue, fontSize = 18.sp, fontWeight = FontWeight.Light)
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun NumberPad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    val buttons = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('C', '0', '⌫')
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        buttons.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    NumberPadButton(
                        key = key,
                        onClick = {
                            when (key) {
                                'C' -> onClear()
                                '⌫' -> onBackspace()
                                else -> onDigit(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberPadButton(
    key: Char,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    val bgColor = when {
        isFocused -> HotstarBlue
        key == 'C' -> StatusLive.copy(alpha = 0.15f)
        key == '⌫' -> StatusWarning.copy(alpha = 0.15f)
        else -> SurfaceElevated
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            '⌫' -> Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = if (isFocused) Color.White else StatusWarning,
                modifier = Modifier.size(22.dp)
            )
            'C' -> Icon(
                Icons.Default.Clear,
                contentDescription = "Clear",
                tint = if (isFocused) Color.White else StatusLive,
                modifier = Modifier.size(22.dp)
            )
            else -> Text(
                key.toString(),
                color = if (isFocused) Color.White else TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
