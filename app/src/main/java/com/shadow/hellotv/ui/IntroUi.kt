package com.shadow.hellotv.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IntroUi(
    isLoading: Boolean = true,
    message: String = "Loading...",
    errorMessage: String? = null,
    onRetry: () -> Unit = {}
) {
    // Smooth fade-in for entire screen
    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { screenVisible = true }
    val screenAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "screenAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark,
                        Color(0xFF141018), // Warm dark mid-tone
                        SurfacePrimary,
                        SurfaceDark
                    )
                )
            )
            .graphicsLayer { alpha = screenAlpha }
    ) {
        // Warm gold glow at top center
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-150).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentGold.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        if (isLoading) {
            AnimatedBackgroundParticles()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (errorMessage != null) {
                ErrorContent(errorMessage = errorMessage, onRetry = onRetry)
            } else {
                LoadingContent(message = message)
            }
        }

        Text(
            text = "v1.0.0",
            fontSize = 12.sp,
            color = TextDisabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun LoadingContent(message: String) {
    AnimatedTVLogo()

    Spacer(modifier = Modifier.height(48.dp))

    // Brand text with gold colors
    Row {
        Text(
            text = "Hello",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )
        Text(
            text = "TV",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGoldLight,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Your Premium IPTV Experience",
        fontSize = 16.sp,
        fontWeight = FontWeight.Light,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(64.dp))

    AnimatedCircularProgress()

    Spacer(modifier = Modifier.height(32.dp))

    AnimatedLoadingText(text = message)
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    // Error icon in clean card
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        StatusLive.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(12.dp, CircleShape, spotColor = StatusLive.copy(alpha = 0.3f))
                .background(
                    color = SurfaceElevated,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = StatusLive,
                modifier = Modifier.size(36.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = "Something Went Wrong",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Error message in card
    Box(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .background(
                StatusLive.copy(alpha = 0.08f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = errorMessage,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = StatusLive,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Please check your internet connection\nand try again",
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = TextMuted,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
    )

    Spacer(modifier = Modifier.height(40.dp))

    // Gold gradient retry button
    Button(
        onClick = onRetry,
        modifier = Modifier
            .width(200.dp)
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentGold
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Retry",
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Try Again",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Press OK or tap to retry",
        fontSize = 13.sp,
        color = TextDisabled,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AnimatedTVLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            // Gold ring
            rotate(rotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            AccentGold,
                            GradientGoldEnd,
                            AccentGoldLight,
                            AccentGold
                        )
                    ),
                    radius = radius * scale,
                    center = center,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )
            }

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

            // Gold play icon
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
                    colors = listOf(AccentGold, GradientGoldEnd)
                )
            )
        }
    }
}

@Composable
private fun AnimatedCircularProgress() {
    val infiniteTransition = rememberInfiniteTransition(label = "progress")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.size(60.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 8f

        drawCircle(
            color = SurfaceElevated,
            radius = radius,
            center = center,
            style = Stroke(width = 6f)
        )

        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    AccentGold,
                    GradientGoldEnd,
                    AccentGoldLight,
                    AccentGold
                )
            ),
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun AnimatedLoadingText(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val dotProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    val dots = ".".repeat(dotProgress.toInt().coerceIn(0, 3))

    Text(
        text = "$text$dots",
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(250.dp)
    )
}

@Composable
private fun AnimatedBackgroundParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // AccentGold particles
        for (i in 0..8) {
            val angle = (offset1 + i * 40f) * Math.PI / 180f
            val radius = size.minDimension / 3 + (i * 20f)
            val x = centerX + (cos(angle) * radius).toFloat()
            val y = centerY + (sin(angle) * radius).toFloat()

            drawCircle(
                color = AccentGold.copy(alpha = 0.12f),
                radius = 4f,
                center = Offset(x, y)
            )
        }

        // AccentGoldLight particles
        for (i in 0..6) {
            val angle = (offset2 + i * 60f) * Math.PI / 180f
            val radius = size.minDimension / 4 + (i * 30f)
            val x = centerX + (cos(angle) * radius).toFloat()
            val y = centerY + (sin(angle) * radius).toFloat()

            drawCircle(
                color = AccentGoldLight.copy(alpha = 0.08f),
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}
