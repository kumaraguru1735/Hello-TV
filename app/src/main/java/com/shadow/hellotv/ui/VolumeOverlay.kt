package com.shadow.hellotv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VolumeOverlay(
    show: Boolean,
    currentVolume: Int,
    maxVolume: Int,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    val cfg = LocalConfiguration.current
    val isTV = cfg.screenWidthDp.dp >= 1000.dp
    val isTablet = cfg.screenWidthDp.dp >= 600.dp

    val animatedVolume by animateFloatAsState(
        targetValue = if (maxVolume > 0) currentVolume.toFloat() / maxVolume else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "volume"
    )

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,  // Fixed: Pass initialScale before animationSpec
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.8f,  // Fixed: Pass targetScale before animationSpec
            animationSpec = tween(200)
        ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .padding(if (isTV) 24.dp else if (isTablet) 20.dp else 16.dp)
                .shadow(16.dp, RoundedCornerShape(if (isTV) 20.dp else 16.dp), spotColor = Color(0xFF6366F1).copy(0.4f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(if (isTV) 20.dp else 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E1E2E).copy(0.96f), Color(0xFF2A2A3E).copy(0.96f))
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.linearGradient(listOf(Color(0xFF6366F1).copy(0.5f), Color(0xFF8B5CF6).copy(0.5f))),
                        RoundedCornerShape(if (isTV) 20.dp else 16.dp)
                    )
                    .padding(if (isTV) 20.dp else if (isTablet) 18.dp else 16.dp)
            ) {
                if (isTV || isTablet) {
                    // ---------- TV / Tablet (horizontal) ----------
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isTV) 16.dp else 12.dp)
                    ) {
                        VolumeIconWithGlow(
                            currentVolume = currentVolume,
                            maxVolume = maxVolume,
                            isMuted = isMuted,
                            size = if (isTV) 48.dp else 40.dp,
                            iconSize = if (isTV) 24.dp else 20.dp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(if (isTV) 8.dp else 6.dp)) {
                            Text(
                                text = if (isMuted || currentVolume == 0) "MUTED" else "VOLUME",
                                color = Color.White.copy(0.7f),
                                fontSize = if (isTV) 12.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )

                            Text(
                                text = "${(animatedVolume * 100).toInt()}%",
                                color = Color.White,
                                fontSize = if (isTV) 28.sp else 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.4.sp
                            )

                            // progress bar
                            Box(
                                modifier = Modifier
                                    .width(if (isTV) 140.dp else 120.dp)
                                    .height(if (isTV) 6.dp else 5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(0.15f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedVolume)
                                        .background(
                                            Brush.horizontalGradient(
                                                if (isMuted || currentVolume == 0)
                                                    listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                                                else
                                                    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                            )
                                        )
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                } else {
                    // ---------- Mobile (vertical) ----------
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isMuted || currentVolume == 0) "MUTED" else "VOLUME",
                            color = Color.White.copy(0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        CircularVolumeIndicator(
                            progress = animatedVolume,
                            isMuted = isMuted || currentVolume == 0,
                            size = 96.dp
                        )

                        Text(
                            text = "${(animatedVolume * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

/* ---------- Icon with glow (smaller) ---------- */
@Composable
fun VolumeIconWithGlow(
    currentVolume: Int,
    maxVolume: Int,
    isMuted: Boolean,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp
) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        // glow
        Box(
            modifier = Modifier
                .size(size + 6.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        if (isMuted || currentVolume == 0)
                            listOf(Color(0xFFEF4444).copy(0.3f), Color.Transparent)
                        else
                            listOf(Color(0xFF6366F1).copy(0.3f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        if (isMuted || currentVolume == 0)
                            listOf(Color(0xFFEF4444).copy(0.3f), Color(0xFFDC2626).copy(0.3f))
                        else
                            listOf(Color(0xFF6366F1).copy(0.3f), Color(0xFF8B5CF6).copy(0.3f))
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        if (isMuted || currentVolume == 0)
                            listOf(Color(0xFFEF4444).copy(0.6f), Color(0xFFDC2626).copy(0.6f))
                        else
                            listOf(Color(0xFF6366F1).copy(0.6f), Color(0xFF8B5CF6).copy(0.6f))
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isMuted || currentVolume == 0 -> Icons.Default.VolumeOff
                    currentVolume < maxVolume / 3 -> Icons.Default.VolumeDown
                    else -> Icons.Default.VolumeUp
                },
                contentDescription = "Volume",
                tint = if (isMuted || currentVolume == 0) Color(0xFFEF4444) else Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/* ---------- Circular indicator (smaller) ---------- */
@Composable
fun CircularVolumeIndicator(
    progress: Float,
    isMuted: Boolean,
    size: androidx.compose.ui.unit.Dp
) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = 10f
            val radius = (size.toPx() - stroke) / 2f

            drawCircle(Color.White.copy(0.15f), radius, style = Stroke(stroke))

            val sweep = 270f * progress
            drawArc(
                brush = Brush.sweepGradient(
                    if (isMuted)
                        listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFFEF4444))
                    else
                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF6366F1))
                ),
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )

            if (progress > 0f) {
                val angle = (135f + sweep) * PI / 180f
                val cx = center.x + radius * cos(angle).toFloat()
                val cy = center.y + radius * sin(angle).toFloat()
                drawCircle(if (isMuted) Color(0xFFEF4444) else Color(0xFF8B5CF6), stroke / 2, Offset(cx, cy))
            }
        }

        Box(
            modifier = Modifier
                .size(size * 0.5f)
                .clip(CircleShape)
                .background(Color(0xFF2A2A3E))
                .border(
                    1.5.dp,
                    if (isMuted) Color(0xFFEF4444).copy(0.5f) else Color(0xFF6366F1).copy(0.5f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = "Volume",
                tint = if (isMuted) Color(0xFFEF4444) else Color.White,
                modifier = Modifier.size(size * 0.25f)
            )
        }
    }
}