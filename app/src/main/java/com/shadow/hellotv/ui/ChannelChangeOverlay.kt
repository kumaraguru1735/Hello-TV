package com.shadow.hellotv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shadow.hellotv.model.Channel
import com.shadow.hellotv.ui.theme.*

@Composable
fun ChannelChangeOverlay(
    show: Boolean,
    showChannelList: Boolean,
    channels: List<Channel>,
    selectedChannelIndex: Int,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTV = screenWidth >= 1000.dp
    val isTablet = screenWidth >= 600.dp

    AnimatedVisibility(
        visible = show && !showChannelList,
        enter = fadeIn(tween(350)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
        exit = fadeOut(tween(250)) +
                slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(250)
                )
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Gradient background from bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                PlayerOverlay.copy(alpha = 0.6f),
                                PlayerOverlay
                            )
                        )
                    )
            )

            channels.getOrNull(selectedChannelIndex)?.let { channel ->
                Column(
                    modifier = Modifier
                        .widthIn(max = if (isTV) 600.dp else if (isTablet) 500.dp else screenWidth * 0.92f)
                        .padding(
                            start = if (isTV) 48.dp else if (isTablet) 32.dp else 16.dp,
                            end = if (isTV) 48.dp else if (isTablet) 32.dp else 16.dp,
                            bottom = if (isTV) 48.dp else if (isTablet) 36.dp else 24.dp
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (isTV) 20.dp else 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Channel number badge - pill shape
                        Box(
                            modifier = Modifier
                                .height(if (isTV) 32.dp else 28.dp)
                                .widthIn(min = if (isTV) 48.dp else 40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentGold)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${selectedChannelIndex + 1}",
                                color = Color.Black,
                                fontSize = if (isTV) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Channel logo with subtle glow
                        val logoSize = if (isTV) 56.dp else 48.dp
                        Box(
                            modifier = Modifier.size(logoSize + 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Glow effect
                            Box(
                                modifier = Modifier
                                    .size(logoSize + 8.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.radialGradient(
                                            listOf(AccentGold.copy(0.15f), Color.Transparent)
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(logoSize)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceElevated)
                                    .border(
                                        0.5.dp,
                                        AccentGold.copy(0.5f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = channel.image,
                                    contentDescription = "Channel Logo",
                                    modifier = Modifier
                                        .size(logoSize - 4.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (channel.image.isEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = TextDisabled,
                                        modifier = Modifier.size(if (isTV) 28.dp else 24.dp)
                                    )
                                }
                            }
                        }

                        // Channel info
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Channel name
                            Text(
                                text = channel.name,
                                color = TextPrimary,
                                fontSize = if (isTV) 22.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Badges row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // LIVE badge with pulsing dot
                                LiveBadge(isTV = isTV)

                                // HD badge
                                if (channel.description.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, AccentGold.copy(0.6f), RoundedCornerShape(6.dp))
                                            .padding(
                                                horizontal = if (isTV) 8.dp else 6.dp,
                                                vertical = if (isTV) 3.dp else 2.dp
                                            )
                                    ) {
                                        Text(
                                            text = channel.description.uppercase(),
                                            color = AccentGold,
                                            fontSize = if (isTV) 10.sp else 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Premium badge
                                if (channel.premium == 1) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(GradientGoldStart, GradientGoldEnd)
                                                )
                                            )
                                            .padding(
                                                horizontal = if (isTV) 8.dp else 6.dp,
                                                vertical = if (isTV) 3.dp else 2.dp
                                            )
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(if (isTV) 10.dp else 9.dp)
                                            )
                                            Text(
                                                text = "PREMIUM",
                                                color = Color.Black,
                                                fontSize = if (isTV) 9.sp else 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }

                                // DRM badge
                                if (!channel.drmLicenceUrl.isNullOrEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StatusWarning.copy(0.2f))
                                            .border(1.dp, StatusWarning.copy(0.5f), RoundedCornerShape(6.dp))
                                            .padding(
                                                horizontal = if (isTV) 8.dp else 6.dp,
                                                vertical = if (isTV) 3.dp else 2.dp
                                            )
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "DRM",
                                                tint = StatusWarning,
                                                modifier = Modifier.size(if (isTV) 10.dp else 9.dp)
                                            )
                                            Text(
                                                text = "DRM",
                                                color = StatusWarning,
                                                fontSize = if (isTV) 9.sp else 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress indicator at bottom - thin 2dp line
                    LinearProgressIndicator(
                        progress = { 0.7f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp)),
                        color = AccentGold,
                        trackColor = Color.White.copy(0.1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveBadge(isTV: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StatusLive)
            .padding(
                horizontal = if (isTV) 8.dp else 6.dp,
                vertical = if (isTV) 3.dp else 2.dp
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isTV) 6.dp else 5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = pulseAlpha))
            )
            Text(
                text = "LIVE",
                color = Color.White,
                fontSize = if (isTV) 9.sp else 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
