package com.shadow.hellotv.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadow.hellotv.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

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
    val configuration = LocalConfiguration.current
    val isTV = configuration.screenWidthDp >= 1000
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val showBranding = (isTV || isTablet) && isLandscape
    val focusManager = LocalFocusManager.current

    var phone by remember { mutableStateOf(savedPhone ?: "") }
    var pin by remember { mutableStateOf(savedPin ?: "") }
    var showPin by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(savedRememberMe) }
    var phoneFieldFocused by remember { mutableStateOf(false) }
    var pinFieldFocused by remember { mutableStateOf(false) }

    val phoneFocusRequester = remember { FocusRequester() }
    val pinFocusRequester = remember { FocusRequester() }
    val loginButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        phoneFocusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark,
                        HotstarNavy,
                        SurfaceDark
                    )
                )
            )
    ) {
        // Animated background
        LoginBackgroundAnimation()

        // Content - Row for landscape TV/tablet, Column for portrait mobile
        if (showBranding) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandingAndForm(
                    isTV = isTV,
                    isTablet = isTablet,
                    phone = phone,
                    onPhoneChange = { if (it.length <= 15) phone = it },
                    pin = pin,
                    onPinChange = { if (it.length <= 4) pin = it },
                    showPin = showPin,
                    onShowPinChange = { showPin = it },
                    rememberMe = rememberMe,
                    onRememberMeChange = { rememberMe = it },
                    phoneFieldFocused = phoneFieldFocused,
                    onPhoneFieldFocusChange = { phoneFieldFocused = it },
                    pinFieldFocused = pinFieldFocused,
                    onPinFieldFocusChange = { pinFieldFocused = it },
                    phoneFocusRequester = phoneFocusRequester,
                    pinFocusRequester = pinFocusRequester,
                    loginButtonFocusRequester = loginButtonFocusRequester,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onLogin = onLogin
                )
            }
        } else {
            // Portrait mobile - centered form
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                LoginFormCard(
                    isTV = false,
                    isTablet = false,
                    showCompactBranding = true,
                    phone = phone,
                    onPhoneChange = { if (it.length <= 15) phone = it },
                    pin = pin,
                    onPinChange = { if (it.length <= 4) pin = it },
                    showPin = showPin,
                    onShowPinChange = { showPin = it },
                    rememberMe = rememberMe,
                    onRememberMeChange = { rememberMe = it },
                    phoneFieldFocused = phoneFieldFocused,
                    onPhoneFieldFocusChange = { phoneFieldFocused = it },
                    pinFieldFocused = pinFieldFocused,
                    onPinFieldFocusChange = { pinFieldFocused = it },
                    phoneFocusRequester = phoneFocusRequester,
                    pinFocusRequester = pinFocusRequester,
                    loginButtonFocusRequester = loginButtonFocusRequester,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onLogin = onLogin
                )
            }
        }

        // Version
        Text(
            text = "v1.0.0",
            fontSize = 12.sp,
            color = TextDisabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun RowScope.BrandingAndForm(
    isTV: Boolean,
    isTablet: Boolean,
    phone: String,
    onPhoneChange: (String) -> Unit,
    pin: String,
    onPinChange: (String) -> Unit,
    showPin: Boolean,
    onShowPinChange: (Boolean) -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    phoneFieldFocused: Boolean,
    onPhoneFieldFocusChange: (Boolean) -> Unit,
    pinFieldFocused: Boolean,
    onPinFieldFocusChange: (Boolean) -> Unit,
    phoneFocusRequester: FocusRequester,
    pinFocusRequester: FocusRequester,
    loginButtonFocusRequester: FocusRequester,
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (phone: String, pin: String, rememberMe: Boolean) -> Unit
) {
    // Left side - Branding
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoginAnimatedLogo()
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "HelloTV",
                fontSize = if (isTV) 56.sp else 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Premium IPTV Experience",
                fontSize = if (isTV) 18.sp else 16.sp,
                fontWeight = FontWeight.Light,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeaturePill("Live TV")
                FeaturePill("HD Quality")
                FeaturePill("Multi-Device")
            }
        }
    }

    // Right side - Login form
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(if (isTV) 48.dp else 32.dp),
        contentAlignment = Alignment.Center
    ) {
        LoginFormCard(
            isTV = isTV,
            isTablet = isTablet,
            showCompactBranding = false,
            phone = phone,
            onPhoneChange = onPhoneChange,
            pin = pin,
            onPinChange = onPinChange,
            showPin = showPin,
            onShowPinChange = onShowPinChange,
            rememberMe = rememberMe,
            onRememberMeChange = onRememberMeChange,
            phoneFieldFocused = phoneFieldFocused,
            onPhoneFieldFocusChange = onPhoneFieldFocusChange,
            pinFieldFocused = pinFieldFocused,
            onPinFieldFocusChange = onPinFieldFocusChange,
            phoneFocusRequester = phoneFocusRequester,
            pinFocusRequester = pinFocusRequester,
            loginButtonFocusRequester = loginButtonFocusRequester,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onLogin = onLogin
        )
    }
}

@Composable
private fun LoginFormCard(
    isTV: Boolean,
    isTablet: Boolean,
    showCompactBranding: Boolean,
    phone: String,
    onPhoneChange: (String) -> Unit,
    pin: String,
    onPinChange: (String) -> Unit,
    showPin: Boolean,
    onShowPinChange: (Boolean) -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    phoneFieldFocused: Boolean,
    onPhoneFieldFocusChange: (Boolean) -> Unit,
    pinFieldFocused: Boolean,
    onPinFieldFocusChange: (Boolean) -> Unit,
    phoneFocusRequester: FocusRequester,
    pinFocusRequester: FocusRequester,
    loginButtonFocusRequester: FocusRequester,
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (phone: String, pin: String, rememberMe: Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = if (isTV) 480.dp else 420.dp)
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = HotstarBlue.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SurfaceCard.copy(alpha = 0.95f),
                            SurfaceOverlay.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            HotstarBlue.copy(alpha = 0.4f),
                            HotstarPink.copy(alpha = 0.4f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isTV) 40.dp else if (showCompactBranding) 28.dp else 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mobile portrait: compact branding
                if (showCompactBranding) {
                    LoginAnimatedLogo()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "HelloTV",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = "Welcome Back",
                    fontSize = if (isTV) 32.sp else if (showCompactBranding) 24.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sign in to continue watching",
                    fontSize = if (isTV) 16.sp else 14.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(if (isTV) 36.dp else if (showCompactBranding) 20.dp else 28.dp))

                // Phone field
                LoginTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = "Phone Number",
                    icon = Icons.Default.Phone,
                    isFocused = phoneFieldFocused,
                    focusRequester = phoneFocusRequester,
                    onFocusChanged = onPhoneFieldFocusChange,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { pinFocusRequester.requestFocus() }
                    ),
                    isTV = isTV
                )

                Spacer(modifier = Modifier.height(if (isTV) 20.dp else 16.dp))

                // PIN field
                LoginTextField(
                    value = pin,
                    onValueChange = onPinChange,
                    label = "4-Digit PIN",
                    icon = Icons.Default.Lock,
                    isFocused = pinFieldFocused,
                    focusRequester = pinFocusRequester,
                    onFocusChanged = onPinFieldFocusChange,
                    isPassword = !showPin,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (phone.isNotBlank() && pin.length == 4) {
                                onLogin(phone, pin, rememberMe)
                            }
                        }
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = if (showPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle PIN visibility",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onShowPinChange(!showPin) }
                        )
                    },
                    isTV = isTV
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusLive.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                StatusLive.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusLive,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = errorMessage,
                                color = StatusLive,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isTV) 16.dp else 12.dp))

                // Remember Me
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onRememberMeChange(!rememberMe) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = onRememberMeChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = HotstarBlue,
                            uncheckedColor = TextMuted,
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.size(if (isTV) 24.dp else 20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Remember me",
                        fontSize = if (isTV) 14.sp else 13.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(if (isTV) 20.dp else 16.dp))

                // Login button
                Button(
                    onClick = {
                        if (phone.isNotBlank() && pin.length == 4) {
                            onLogin(phone, pin, rememberMe)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTV) 60.dp else 54.dp)
                        .focusRequester(loginButtonFocusRequester)
                        .shadow(
                            8.dp,
                            RoundedCornerShape(16.dp),
                            spotColor = HotstarBlue.copy(alpha = 0.5f)
                        ),
                    enabled = phone.isNotBlank() && pin.length == 4 && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (phone.isNotBlank() && pin.length == 4 && !isLoading) {
                                        listOf(HotstarBlue, GradientBlueEnd)
                                    } else {
                                        listOf(
                                            HotstarBlue.copy(alpha = 0.3f),
                                            GradientBlueEnd.copy(alpha = 0.3f)
                                        )
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Sign In",
                                    fontSize = if (isTV) 18.sp else 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isTV) {
                    Text(
                        text = "Use D-pad to navigate, OK to select",
                        fontSize = 12.sp,
                        color = TextDisabled,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    isTV: Boolean = false
) {
    val borderColor = if (isFocused) HotstarBlue else Color.White.copy(alpha = 0.15f)

    Column {
        Text(
            text = label,
            fontSize = if (isTV) 14.sp else 13.sp,
            color = if (isFocused) HotstarBlue else TextMuted,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTV) 58.dp else 52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isFocused) HotstarBlue.copy(alpha = 0.08f)
                    else Color.White.copy(alpha = 0.06f)
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isFocused) HotstarBlue else TextMuted,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = if (isTV) 17.sp else 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = label,
                                    color = TextDisabled,
                                    fontSize = if (isTV) 16.sp else 15.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(HotstarBlue.copy(alpha = 0.15f))
            .border(1.dp, HotstarBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = HotstarBlueLight,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoginAnimatedLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "loginLogo")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            rotate(rotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            HotstarBlue,
                            HotstarPink,
                            HotstarBlueLight,
                            HotstarBlue
                        )
                    ),
                    radius = radius * scale,
                    center = center,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )
            }

            // TV icon
            val tvWidth = radius * 0.8f
            val tvHeight = radius * 0.6f
            val left = center.x - tvWidth / 2
            val top = center.y - tvHeight / 2

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(tvWidth, tvHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )

            val standWidth = tvWidth * 0.6f
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(center.x - standWidth / 2, top + tvHeight + 4f),
                size = androidx.compose.ui.geometry.Size(standWidth, 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            val playSize = radius * 0.3f
            val playPath = Path().apply {
                moveTo(center.x - playSize * 0.3f, center.y - playSize * 0.4f)
                lineTo(center.x + playSize * 0.5f, center.y)
                lineTo(center.x - playSize * 0.3f, center.y + playSize * 0.4f)
                close()
            }
            drawPath(
                path = playPath,
                brush = Brush.linearGradient(
                    colors = listOf(HotstarBlue, HotstarPink)
                )
            )
        }
    }
}

@Composable
private fun LoginBackgroundAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        for (i in 0..10) {
            val angle = (offset1 + i * 36f) * Math.PI / 180f
            val radius = size.minDimension / 3 + (i * 25f)
            val x = centerX + (cos(angle) * radius).toFloat()
            val y = centerY + (sin(angle) * radius).toFloat()

            drawCircle(
                color = HotstarBlue.copy(alpha = 0.06f),
                radius = 6f,
                center = Offset(x, y)
            )
        }

        for (i in 0..8) {
            val angle = (360f - offset1 + i * 45f) * Math.PI / 180f
            val radius = size.minDimension / 4 + (i * 30f)
            val x = centerX + (cos(angle) * radius).toFloat()
            val y = centerY + (sin(angle) * radius).toFloat()

            drawCircle(
                color = HotstarPink.copy(alpha = 0.04f),
                radius = 5f,
                center = Offset(x, y)
            )
        }
    }
}
