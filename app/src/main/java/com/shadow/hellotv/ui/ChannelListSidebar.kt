package com.shadow.hellotv.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shadow.hellotv.model.Category
import com.shadow.hellotv.model.Channel
import com.shadow.hellotv.model.Language
import com.shadow.hellotv.ui.theme.*

@Composable
fun ChannelListSidebar(
    channels: List<Channel>,
    selectedChannelIndex: Int,
    onChannelSelected: (Int) -> Unit,
    listState: LazyListState,
    categories: List<Category> = emptyList(),
    languages: List<Language> = emptyList(),
    selectedCategoryId: Int? = null,
    selectedLanguageId: Int? = null,
    onCategorySelected: (Int?) -> Unit = {},
    onLanguageSelected: (Int?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTV = configuration.screenWidthDp.dp >= 1000.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        SurfacePrimary,
                        SurfacePrimary,
                        SurfacePrimary.copy(alpha = 0.95f),
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTV) 28.dp else 20.dp)
        ) {
            // Header
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Channels",
                    color = TextPrimary,
                    fontSize = if (isTV) 32.sp else 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${channels.size} available",
                    color = TextMuted,
                    fontSize = if (isTV) 15.sp else 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(if (isTV) 20.dp else 16.dp))

            // Category filter chips
            if (categories.isNotEmpty()) {
                FilterChipsRow(
                    items = categories.map { it.id to it.name },
                    selectedId = selectedCategoryId,
                    onSelected = onCategorySelected,
                    isTV = isTV
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Language filter chips
            if (languages.isNotEmpty()) {
                FilterChipsRow(
                    items = languages.map { it.id to it.name },
                    selectedId = selectedLanguageId,
                    onSelected = onLanguageSelected,
                    isTV = isTV
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Search box - AccentGold cursor/focus border, rounded 12dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTV) 54.dp else 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceInput)
                    .border(1.dp, AccentGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
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
                        tint = AccentGold.copy(alpha = 0.6f),
                        modifier = Modifier.size(if (isTV) 24.dp else 20.dp)
                    )
                    Text(
                        text = "Search channels...",
                        color = TextDisabled,
                        fontSize = if (isTV) 16.sp else 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isTV) 16.dp else 12.dp))

            HorizontalDivider(color = SurfaceSeparator, thickness = 1.dp)

            Spacer(modifier = Modifier.height(if (isTV) 16.dp else 12.dp))

            // Channel list
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(if (isTV) 8.dp else 6.dp)
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
                            .clickable { onChannelSelected(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    items: List<Pair<Int, String>>,
    selectedId: Int?,
    onSelected: (Int?) -> Unit,
    isTV: Boolean
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All chip
        FilterChip(
            text = "All",
            isSelected = selectedId == null,
            onClick = { onSelected(null) },
            isTV = isTV
        )

        items.forEach { (id, name) ->
            FilterChip(
                text = name,
                isSelected = selectedId == id,
                onClick = { onSelected(if (selectedId == id) null else id) },
                isTV = isTV
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isTV: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Brush.horizontalGradient(listOf(GradientGoldStart, GradientGoldEnd))
                else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .then(
                if (!isSelected) Modifier.border(1.dp, SurfaceSeparator, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = if (isTV) 14.dp else 12.dp, vertical = if (isTV) 7.dp else 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else TextMuted,
            fontSize = if (isTV) 12.sp else 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun ChannelListItemComposable(
    channel: Channel,
    channelNumber: Int,
    isSelected: Boolean,
    isTV: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) AccentGold.copy(alpha = 0.1f) else Color.Transparent
            )
            .then(
                if (isSelected) Modifier.border(1.dp, AccentGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(if (isTV) 12.dp else 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isTV) 14.dp else 12.dp)
        ) {
            // Channel number - AccentGold circle badge on selected
            Box(
                modifier = Modifier
                    .size(if (isTV) 36.dp else 32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(listOf(GradientGoldStart, GradientGoldEnd))
                        } else {
                            Brush.linearGradient(
                                listOf(Color.White.copy(0.08f), Color.White.copy(0.05f))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$channelNumber",
                    color = if (isSelected) Color.Black else TextMuted,
                    fontSize = if (isTV) 14.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Channel logo
            Box(
                modifier = Modifier
                    .size(if (isTV) 44.dp else 40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .border(
                        width = if (isSelected) 1.dp else 0.5.dp,
                        color = if (isSelected) AccentGold.copy(0.5f)
                        else Color.White.copy(0.06f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = channel.image,
                    contentDescription = "Channel Logo",
                    modifier = Modifier
                        .size(if (isTV) 38.dp else 34.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                if (channel.image.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = TextDisabled,
                        modifier = Modifier.size(if (isTV) 22.dp else 20.dp)
                    )
                }
            }

            // Channel info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = channel.name,
                    color = if (isSelected) AccentGold else TextPrimary,
                    fontSize = if (isTV) 15.sp else 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (channel.description.isNotEmpty()) {
                    Text(
                        text = channel.description.uppercase(),
                        color = if (isSelected) TextSecondary else TextMuted,
                        fontSize = if (isTV) 11.sp else 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // DRM indicator
            if (!channel.drmLicenceUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(if (isTV) 28.dp else 26.dp)
                        .clip(CircleShape)
                        .background(StatusWarning.copy(alpha = 0.12f))
                        .border(0.5.dp, StatusWarning.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "DRM Protected",
                        tint = StatusWarning,
                        modifier = Modifier.size(if (isTV) 14.dp else 13.dp)
                    )
                }
            }
        }
    }
}
