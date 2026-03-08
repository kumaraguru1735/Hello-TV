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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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

    Card(
        modifier = modifier
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                spotColor = HotstarBlue.copy(alpha = 0.4f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SurfaceDark,
                            SurfaceCard,
                            SurfaceDark
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            HotstarBlue.copy(alpha = 0.3f),
                            HotstarPink.copy(alpha = 0.2f),
                            HotstarBlue.copy(alpha = 0.3f)
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
                Column(modifier = Modifier.fillMaxWidth()) {
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

                // Search box (decorative)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTV) 54.dp else 48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
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
                            tint = TextDisabled,
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

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                Spacer(modifier = Modifier.height(if (isTV) 16.dp else 12.dp))

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
                                .clickable { onChannelSelected(index) }
                        )
                    }
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
                if (isSelected) Brush.horizontalGradient(listOf(HotstarBlue, GradientBlueEnd))
                else Brush.horizontalGradient(listOf(Color.White.copy(0.08f), Color.White.copy(0.06f)))
            )
            .then(
                if (!isSelected) Modifier.border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = if (isTV) 14.dp else 12.dp, vertical = if (isTV) 7.dp else 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextMuted,
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
    Card(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 12.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isSelected) HotstarBlue.copy(alpha = 0.5f) else Color.Transparent
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.04f)
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
                                HotstarBlue.copy(alpha = 0.25f),
                                HotstarPink.copy(alpha = 0.15f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .border(
                    width = if (isSelected) 1.5.dp else 0.dp,
                    color = if (isSelected) HotstarBlue.copy(alpha = 0.5f) else Color.Transparent,
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
                                Brush.linearGradient(listOf(HotstarBlue, HotstarPink))
                            } else {
                                Brush.linearGradient(
                                    listOf(Color.White.copy(0.12f), Color.White.copy(0.08f))
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
                        .background(SurfaceElevated)
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) HotstarBlue.copy(0.5f)
                            else Color.White.copy(0.08f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = channel.image,
                        contentDescription = "Channel Logo",
                        modifier = Modifier
                            .size(if (isTV) 48.dp else 44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    if (channel.image.isEmpty()) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = TextDisabled,
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

                    if (channel.description.isNotEmpty()) {
                        Text(
                            text = channel.description.uppercase(),
                            color = if (isSelected) TextSecondary else TextMuted,
                            fontSize = if (isTV) 12.sp else 11.sp,
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
                            .size(if (isTV) 32.dp else 30.dp)
                            .clip(CircleShape)
                            .background(StatusWarning.copy(alpha = 0.15f))
                            .border(1.dp, StatusWarning.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "DRM Protected",
                            tint = StatusWarning,
                            modifier = Modifier.size(if (isTV) 16.dp else 15.dp)
                        )
                    }
                }
            }
        }
    }
}
