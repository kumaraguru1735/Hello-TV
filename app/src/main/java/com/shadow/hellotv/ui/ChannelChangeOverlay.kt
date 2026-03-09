package com.shadow.hellotv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
        enter = fadeIn(tween(400)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
                ) +
                scaleIn(initialScale = 0.8f, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)),
        exit = fadeOut(tween(300)) +
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) +
                scaleOut(targetScale = 0.8f, animationSpec = tween(300))
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            channels.getOrNull(selectedChannelIndex)?.let { channel ->
                Card(
                    modifier = Modifier
                        .widthIn(max = if (isTV) 600.dp else if (isTablet) 500.dp else screenWidth * 0.92f)
                        .padding(
                            start = if (isTV) 48.dp else if (isTablet) 32.dp else 16.dp,
                            end = if (isTV) 48.dp else if (isTablet) 32.dp else 16.dp,
                            bottom = if (isTV) 48.dp else if (isTablet) 36.dp else 24.dp
                        )
                        .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = AccentGold.copy(0.3f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SurfaceDark.copy(0.95f), SurfaceCard.copy(0.95f))
                                )
                            )
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(AccentGold.copy(0.4f), GradientGoldEnd.copy(0.3f))
                                ),
                                RoundedCornerShape(24.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isTV) 28.dp else if (isTablet) 24.dp else 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(if (isTV) 24.dp else 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Channel number badge
                            Box(
                                modifier = Modifier
                                    .size(if (isTV) 72.dp else if (isTablet) 64.dp else 56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(listOf(GradientGoldStart, GradientGoldEnd))
                                    )
                                    .border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${selectedChannelIndex + 1}",
                                        color = Color.Black,
                                        fontSize = if (isTV) 24.sp else if (isTablet) 22.sp else 20.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "CH",
                                        color = Color.Black.copy(0.7f),
                                        fontSize = if (isTV) 10.sp else 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            // Channel logo
                            Box(
                                modifier = Modifier.size(if (isTV) 80.dp else if (isTablet) 70.dp else 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isTV) 88.dp else if (isTablet) 78.dp else 72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.radialGradient(
                                                listOf(AccentGold.copy(0.2f), Color.Transparent)
                                            )
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(if (isTV) 80.dp else if (isTablet) 70.dp else 64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(SurfaceElevated)
                                        .border(
                                            2.dp,
                                            Brush.linearGradient(
                                                listOf(AccentGold.copy(0.5f), GradientGoldEnd.copy(0.3f))
                                            ),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = channel.image,
                                        contentDescription = "Channel Logo",
                                        modifier = Modifier
                                            .size(if (isTV) 72.dp else if (isTablet) 62.dp else 56.dp)
                                            .clip(RoundedCornerShape(14.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (channel.image.isEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = TextDisabled,
                                            modifier = Modifier.size(if (isTV) 32.dp else 28.dp)
                                        )
                                    }
                                }
                            }

                            // Channel info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(if (isTV) 6.dp else 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = channel.name,
                                        color = TextPrimary,
                                        fontSize = if (isTV) 26.sp else if (isTablet) 22.sp else 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Live badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(StatusLive.copy(0.2f))
                                            .border(1.dp, StatusLive.copy(0.5f), RoundedCornerShape(8.dp))
                                            .padding(
                                                horizontal = if (isTV) 10.dp else 8.dp,
                                                vertical = if (isTV) 5.dp else 4.dp
                                            )
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isTV) 7.dp else 6.dp)
                                                    .clip(CircleShape)
                                                    .background(StatusLive)
                                            )
                                            Text(
                                                text = "LIVE",
                                                color = StatusLive,
                                                fontSize = if (isTV) 10.sp else 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }

                                // Badges row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (channel.description.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(AccentGold.copy(0.15f), GradientGoldEnd.copy(0.1f))
                                                    )
                                                )
                                                .border(1.dp, AccentGold.copy(0.3f), RoundedCornerShape(8.dp))
                                                .padding(
                                                    horizontal = if (isTV) 12.dp else 10.dp,
                                                    vertical = if (isTV) 5.dp else 4.dp
                                                )
                                        ) {
                                            Text(
                                                text = channel.description.uppercase(),
                                                color = Color.White.copy(0.9f),
                                                fontSize = if (isTV) 12.sp else if (isTablet) 11.sp else 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.8.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (!channel.drmLicenceUrl.isNullOrEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StatusWarning.copy(0.2f))
                                                .border(1.dp, StatusWarning.copy(0.5f), RoundedCornerShape(8.dp))
                                                .padding(
                                                    horizontal = if (isTV) 10.dp else 8.dp,
                                                    vertical = if (isTV) 5.dp else 4.dp
                                                )
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "DRM",
                                                    tint = StatusWarning,
                                                    modifier = Modifier.size(if (isTV) 12.dp else 11.dp)
                                                )
                                                Text(
                                                    text = "DRM",
                                                    color = StatusWarning,
                                                    fontSize = if (isTV) 10.sp else 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (isTV) 2.dp else 1.dp))
                                LinearProgressIndicator(
                                    progress = { 0.7f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isTV) 4.dp else 3.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = AccentGold,
                                    trackColor = Color.White.copy(0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
