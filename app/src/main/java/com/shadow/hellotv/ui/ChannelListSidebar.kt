package com.shadow.hellotv.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.shadow.hellotv.model.ChannelItem

@Composable
fun ChannelListSidebar(
    channels: List<ChannelItem>,
    selectedChannelIndex: Int,
    onChannelSelected: (Int) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTV = configuration.screenWidthDp.dp >= 1000.dp

    Card(
        modifier = modifier
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                spotColor = Color(0xFF6366F1).copy(alpha = 0.4f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F0F1E),
                            Color(0xFF1A1A2E),
                            Color(0xFF0F0F1E)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = 0.3f),
                            Color(0xFF8B5CF6).copy(alpha = 0.3f),
                            Color(0xFFEC4899).copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isTV) 28.dp else 20.dp)
            ) {
                // Header
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Channels",
                        color = Color.White,
                        fontSize = if (isTV) 32.sp else 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${channels.size} available",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = if (isTV) 15.sp else 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(if (isTV) 24.dp else 20.dp))

                // Search box (decorative)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTV) 54.dp else 48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(if (isTV) 24.dp else 20.dp)
                        )
                        Text(
                            text = "Search channels...",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = if (isTV) 16.sp else 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isTV) 20.dp else 16.dp))

                // Divider
                Divider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(if (isTV) 20.dp else 16.dp))

                // Channel list
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(if (isTV) 12.dp else 10.dp)
                ) {
                    itemsIndexed(channels) { index, channel ->
                        ChannelListItemComposable(
                            channel = channel,
                            channelNumber = index + 1,
                            isSelected = index == selectedChannelIndex,
                            isTV = isTV,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable {
                                    onChannelSelected(index)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelListItemComposable(
    channel: ChannelItem,
    channelNumber: Int,
    isSelected: Boolean,
    isTV: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 12.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.5f) else Color.Transparent
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color.Transparent
            } else {
                Color.White.copy(alpha = 0.05f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.3f),
                                Color(0xFF8B5CF6).copy(alpha = 0.3f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    }
                )
                .border(
                    width = if (isSelected) 1.5.dp else 0.dp,
                    color = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.6f) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(if (isTV) 16.dp else 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isTV) 16.dp else 14.dp)
            ) {
                // Channel number
                Box(
                    modifier = Modifier
                        .size(if (isTV) 44.dp else 40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1),
                                        Color(0xFF8B5CF6)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.15f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$channelNumber",
                        color = Color.White,
                        fontSize = if (isTV) 16.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Channel logo
                Box(
                    modifier = Modifier
                        .size(if (isTV) 56.dp else 52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A3E))
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected)
                                Color(0xFF6366F1).copy(alpha = 0.5f)
                            else
                                Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = "Channel Logo",
                        modifier = Modifier
                            .size(if (isTV) 48.dp else 44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    if (channel.logo.isEmpty()) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(if (isTV) 24.dp else 22.dp)
                        )
                    }
                }

                // Channel info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontSize = if (isTV) 17.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (channel.category.isNotEmpty()) {
                        Text(
                            text = channel.category.uppercase(),
                            color = if (isSelected)
                                Color.White.copy(alpha = 0.8f)
                            else
                                Color.White.copy(alpha = 0.5f),
                            fontSize = if (isTV) 12.sp else 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // DRM indicator
                if (!channel.drmUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(if (isTV) 32.dp else 30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBBF24).copy(alpha = 0.2f))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFFBBF24).copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "DRM Protected",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(if (isTV) 16.dp else 15.dp)
                        )
                    }
                }
            }
        }
    }
}