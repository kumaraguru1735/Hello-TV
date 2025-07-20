package com.shadow.hellotv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val screenHeight = configuration.screenHeightDp.dp

    // Responsive sizing based on screen type
    val isTablet = screenWidth >= 600.dp
    val isTV = screenWidth >= 1000.dp
    val isMobile = screenWidth < 600.dp

    val logoSize = when {
        isTV -> 56.dp
        isTablet -> 48.dp
        else -> 40.dp
    }

    val fontSize = when {
        isTV -> 20.sp
        isTablet -> 18.sp
        else -> 16.sp
    }

    val categoryFontSize = when {
        isTV -> 14.sp
        isTablet -> 12.sp
        else -> 11.sp
    }

    val containerPadding = when {
        isTV -> 24.dp
        isTablet -> 20.dp
        else -> 16.dp
    }

    val bottomPadding = when {
        isTV -> 32.dp
        isTablet -> 24.dp
        else -> 16.dp
    }

    AnimatedVisibility(
        visible = show && !showChannelList,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        )
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(
                        max = when {
                            isTV -> 500.dp
                            isTablet -> 400.dp
                            else -> screenWidth * 0.9f
                        }
                    )
                    .padding(
                        start = when {
                            isTV -> 48.dp
                            isTablet -> 32.dp
                            else -> 16.dp
                        },
                        end = when {
                            isTV -> 48.dp
                            isTablet -> 32.dp
                            else -> 16.dp
                        },
                        bottom = bottomPadding
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(
                    when {
                        isTV -> 16.dp
                        isTablet -> 14.dp
                        else -> 12.dp
                    }
                )
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.92f),
                                    Color.Black.copy(alpha = 0.96f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(
                                when {
                                    isTV -> 16.dp
                                    isTablet -> 14.dp
                                    else -> 12.dp
                                }
                            )
                        )
                        .padding(containerPadding)
                ) {
                    channels.getOrNull(selectedChannelIndex)?.let { channel ->
                        if (isMobile) {
                            // Mobile Layout - Compact horizontal
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Channel Number Badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${selectedChannelIndex + 1}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Logo
                                Box(
                                    modifier = Modifier
                                        .size(logoSize)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = channel.logo,
                                        contentDescription = "Channel Logo",
                                        modifier = Modifier
                                            .size(logoSize - 8.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        onError = {}
                                    )

                                    if (channel.logo.isEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "TV Icon",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Channel Info - Flexible width
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = channel.name,
                                        color = Color.White,
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (channel.category.isNotEmpty()) {
                                        Text(
                                            text = channel.category,
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = categoryFontSize,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else {
                            // Tablet & TV Layout - More spacious
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Channel Number Badge
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${selectedChannelIndex + 1}",
                                        color = Color.White,
                                        fontSize = when {
                                            isTV -> 16.sp
                                            else -> 14.sp
                                        },
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = when {
                                                isTV -> 16.dp
                                                else -> 12.dp
                                            },
                                            vertical = when {
                                                isTV -> 8.dp
                                                else -> 6.dp
                                            }
                                        )
                                    )
                                }

                                // Logo
                                Box(
                                    modifier = Modifier
                                        .size(logoSize + 8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .border(
                                            width = 1.5.dp,
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = channel.logo,
                                        contentDescription = "Channel Logo",
                                        modifier = Modifier
                                            .size(logoSize)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        onError = {}
                                    )

                                    if (channel.logo.isEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "TV Icon",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(
                                                when {
                                                    isTV -> 24.dp
                                                    else -> 20.dp
                                                }
                                            )
                                        )
                                    }
                                }

                                // Channel Info
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = channel.name,
                                        color = Color.White,
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = if (isTV) 2 else 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Start
                                    )

                                    if (channel.category.isNotEmpty()) {
                                        Text(
                                            text = channel.category.uppercase(),
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = categoryFontSize,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}