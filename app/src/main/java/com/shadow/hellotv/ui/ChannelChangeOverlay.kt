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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shadow.hellotv.model.ChannelItem

@Composable
fun ChannelChangeOverlay(
    show: Boolean,
    showChannelList: Boolean,
    channels: List<ChannelItem>,
    selectedChannelIndex: Int,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTV = screenWidth >= 1000.dp
    val isTablet = screenWidth >= 600.dp

    AnimatedVisibility(
        visible = show && !showChannelList,
        enter = fadeIn(animationSpec = tween(400)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) +
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
        exit = fadeOut(animationSpec = tween(300)) +
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) +
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(300)
                )
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            channels.getOrNull(selectedChannelIndex)?.let { channel ->
                // Glass morphism card
                Card(
                    modifier = Modifier
                        .widthIn(max = if (isTV) 600.dp else if (isTablet) 500.dp else screenWidth * 0.92f)
                        .padding(
                            start = if (isTV) 48.dp else if (isTablet) 32.dp else 16.dp,
                            end = if (isTV) 48.dp else if (isTablet) 32.dp else 16.dp,
                            bottom = if (isTV) 48.dp else if (isTablet) 36.dp else 24.dp
                        )
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color(0xFF6366F1).copy(alpha = 0.3f)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1E1E2E).copy(alpha = 0.95f),
                                        Color(0xFF2A2A3E).copy(alpha = 0.95f)
                                    )
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.5f),
                                        Color(0xFF8B5CF6).copy(alpha = 0.5f),
                                        Color(0xFFEC4899).copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isTV) 28.dp else if (isTablet) 24.dp else 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(if (isTV) 24.dp else 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Channel number badge with gradient - Changed to rounded rectangle
                            Box(
                                modifier = Modifier
                                    .size(if (isTV) 72.dp else if (isTablet) 64.dp else 56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF6366F1),
                                                Color(0xFF8B5CF6)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = Color.White.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${selectedChannelIndex + 1}",
                                        color = Color.White,
                                        fontSize = if (isTV) 24.sp else if (isTablet) 22.sp else 20.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "CH",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = if (isTV) 10.sp else 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            // Channel logo with glow effect - Changed to rounded rectangle
                            Box(
                                modifier = Modifier
                                    .size(if (isTV) 80.dp else if (isTablet) 70.dp else 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Glow background
                                Box(
                                    modifier = Modifier
                                        .size(if (isTV) 88.dp else if (isTablet) 78.dp else 72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF6366F1).copy(alpha = 0.3f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                // Logo
                                Box(
                                    modifier = Modifier
                                        .size(if (isTV) 80.dp else if (isTablet) 70.dp else 64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF2A2A3E))
                                        .border(
                                            width = 2.dp,
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF6366F1).copy(alpha = 0.5f),
                                                    Color(0xFF8B5CF6).copy(alpha = 0.5f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = channel.logo,
                                        contentDescription = "Channel Logo",
                                        modifier = Modifier
                                            .size(if (isTV) 72.dp else if (isTablet) 62.dp else 56.dp)
                                            .clip(RoundedCornerShape(14.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    if (channel.logo.isEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.5f),
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
                                // Top row: Channel name with LIVE badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Channel name
                                    Text(
                                        text = channel.name,
                                        color = Color.White,
                                        fontSize = if (isTV) 26.sp else if (isTablet) 22.sp else 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Live badge (always visible on the right)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFFEF4444).copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
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
                                                    .background(Color(0xFFEF4444))
                                            )
                                            Text(
                                                text = "LIVE",
                                                color = Color(0xFFEF4444),
                                                fontSize = if (isTV) 10.sp else 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }

                                // Bottom row: Category and DRM badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Category badge (can take available space)
                                    if (channel.category.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color(0xFF8B5CF6).copy(alpha = 0.3f),
                                                            Color(0xFFEC4899).copy(alpha = 0.3f)
                                                        )
                                                    )
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(
                                                    horizontal = if (isTV) 12.dp else 10.dp,
                                                    vertical = if (isTV) 5.dp else 4.dp
                                                )
                                        ) {
                                            Text(
                                                text = channel.category.uppercase(),
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = if (isTV) 12.sp else if (isTablet) 11.sp else 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.8.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // DRM badge (fixed size)
                                    if (!channel.drmUrl.isNullOrEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFBBF24).copy(alpha = 0.2f))
                                                .border(
                                                    width = 1.dp,
                                                    color = Color(0xFFFBBF24).copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
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
                                                    tint = Color(0xFFFBBF24),
                                                    modifier = Modifier.size(if (isTV) 12.dp else 11.dp)
                                                )
                                                Text(
                                                    text = "DRM",
                                                    color = Color(0xFFFBBF24),
                                                    fontSize = if (isTV) 10.sp else 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // Progress indicator (decorative)
                                Spacer(modifier = Modifier.height(if (isTV) 2.dp else 1.dp))
                                LinearProgressIndicator(
                                    progress = 0.7f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isTV) 4.dp else 3.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = Color(0xFF6366F1),
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}