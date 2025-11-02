package com.shadow.hellotv.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF1A1F3A),
                        Color(0xFF0A0E27)
                    )
                )
            )
    ) {
        // Animated background particles
        if (isLoading) {
            AnimatedBackgroundParticles()
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (errorMessage != null) {
                // Error State
                ErrorContent(
                    errorMessage = errorMessage,
                    onRetry = onRetry
                )
            } else {
                // Loading State
                LoadingContent(message = message)
            }
        }

        // Version info at bottom
        Text(
            text = "v1.0.0",
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun LoadingContent(message: String) {
    // App logo with animation
    AnimatedTVLogo()

    Spacer(modifier = Modifier.height(48.dp))

    // App name
    Text(
        text = "HelloTV",
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center,
        letterSpacing = 2.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Tagline
    Text(
        text = "Your Premium IPTV Experience",
        fontSize = 16.sp,
        fontWeight = FontWeight.Light,
        color = Color(0xFFB0B0B0),
        textAlign = TextAlign.Center,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(64.dp))

    // Custom circular progress
    AnimatedCircularProgress()

    Spacer(modifier = Modifier.height(32.dp))

    // Loading text with animation
    AnimatedLoadingText(text = message)
}

@Composable
fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    // Error icon
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B),
                        Color(0xFFFF4757)
                    )
                ),
                shape = androidx.compose.foundation.shape.CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = Color.White,
            modifier = Modifier.size(56.dp)
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Error title
    Text(
        text = "Oops!",
        fontSize = 42.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Error message
    Text(
        text = errorMessage,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFFF6B6B),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 48.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Helper text
    Text(
        text = "Please check your internet connection\nand try again",
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFF999999),
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
    )

    Spacer(modifier = Modifier.height(48.dp))

    // Retry button
    Button(
        onClick = onRetry,
        modifier = Modifier
            .width(200.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6366F1)
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Retry",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Try Again",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Instructions
    Text(
        text = "Press OK or tap to retry",
        fontSize = 13.sp,
        color = Color(0xFF666666),
        textAlign = TextAlign.Center
    )
}

@Composable
fun AnimatedTVLogo() {
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

            // Outer rotating ring
            rotate(rotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF6366F1),
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899),
                            Color(0xFF6366F1)
                        )
                    ),
                    radius = radius * scale,
                    center = center,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )
            }

            // Inner TV icon
            val tvWidth = radius * 0.8f
            val tvHeight = radius * 0.6f
            val left = center.x - tvWidth / 2
            val top = center.y - tvHeight / 2

            // TV screen
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(tvWidth, tvHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )

            // TV stand
            val standWidth = tvWidth * 0.6f
            val standHeight = 8f
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(center.x - standWidth / 2, top + tvHeight + 4f),
                size = androidx.compose.ui.geometry.Size(standWidth, standHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Play icon on screen
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
                    colors = listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6)
                    )
                )
            )
        }
    }
}

@Composable
fun AnimatedCircularProgress() {
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

        // Background circle
        drawCircle(
            color = Color(0xFF2A2F4A),
            radius = radius,
            center = center,
            style = Stroke(width = 6f)
        )

        // Progress arc
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF6366F1),
                    Color(0xFF8B5CF6),
                    Color(0xFFEC4899),
                    Color(0xFF6366F1)
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
fun AnimatedLoadingText(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val dotCount by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 3,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    val dots = ".".repeat(dotCount + 1)

    Text(
        text = "$text$dots",
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFFB0B0B0),
        textAlign = TextAlign.Center,
        modifier = Modifier.width(250.dp)
    )
}

@Composable
fun AnimatedBackgroundParticles() {
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

        // Draw multiple animated particles
        for (i in 0..8) {
            val angle = (offset1 + i * 40f) * Math.PI / 180f
            val radius = size.minDimension / 3 + (i * 20f)
            val x = centerX + (cos(angle) * radius).toFloat()
            val y = centerY + (sin(angle) * radius).toFloat()

            drawCircle(
                color = Color(0xFF6366F1).copy(alpha = 0.1f),
                radius = 4f,
                center = Offset(x, y)
            )
        }

        for (i in 0..6) {
            val angle = (offset2 + i * 60f) * Math.PI / 180f
            val radius = size.minDimension / 4 + (i * 30f)
            val x = centerX + (cos(angle) * radius).toFloat()
            val y = centerY + (sin(angle) * radius).toFloat()

            drawCircle(
                color = Color(0xFFEC4899).copy(alpha = 0.08f),
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}