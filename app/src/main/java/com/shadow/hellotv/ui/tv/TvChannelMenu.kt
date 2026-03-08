package com.shadow.hellotv.ui.tv

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun TvChannelMenu(
    channels: List<Channel>,
    categories: List<Category>,
    languages: List<Language>,
    selectedChannelIndex: Int,
    selectedCategoryId: Int?,
    selectedLanguageId: Int?,
    onChannelSelected: (Int) -> Unit,
    onCategorySelected: (Int?) -> Unit,
    onLanguageSelected: (Int?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val channelListState = rememberLazyListState()

    // Auto-scroll to selected channel
    LaunchedEffect(selectedChannelIndex) {
        if (channels.isNotEmpty()) {
            channelListState.animateScrollToItem(selectedChannelIndex)
        }
    }

    Box(
        modifier = modifier
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xF00B1622),
                        Color(0xE00B1622),
                        Color(0x800B1622),
                        Color.Transparent
                    )
                )
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Language filter chips ──
            Text("Language", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                item {
                    FilterChipItem(
                        label = "All",
                        selected = selectedLanguageId == null,
                        onClick = { onLanguageSelected(null) }
                    )
                }
                items(languages) { lang ->
                    FilterChipItem(
                        label = lang.name,
                        selected = selectedLanguageId == lang.id,
                        onClick = { onLanguageSelected(if (selectedLanguageId == lang.id) null else lang.id) }
                    )
                }
            }

            // ── Main content: Categories + Channels ──
            Row(modifier = Modifier.fillMaxSize()) {
                // Category column
                LazyColumn(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        CategoryItem(
                            name = "All",
                            selected = selectedCategoryId == null,
                            onClick = { onCategorySelected(null) }
                        )
                    }
                    items(categories) { cat ->
                        CategoryItem(
                            name = cat.name,
                            selected = selectedCategoryId == cat.id,
                            onClick = { onCategorySelected(if (selectedCategoryId == cat.id) null else cat.id) }
                        )
                    }
                }

                // Channel list
                LazyColumn(
                    state = channelListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(channels) { index, channel ->
                        ChannelItem(
                            channel = channel,
                            isSelected = index == selectedChannelIndex,
                            onClick = { onChannelSelected(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val bgColor = when {
        selected -> HotstarBlue
        isFocused -> HotstarBlue.copy(alpha = 0.3f)
        else -> SurfaceElevated
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected || isFocused) Color.White else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CategoryItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val bgColor = when {
        selected -> HotstarBlue.copy(alpha = 0.2f)
        isFocused -> SurfaceOverlay
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (selected) Modifier.border(1.dp, HotstarBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            name,
            color = if (selected) HotstarBlueLight else if (isFocused) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChannelItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val bgColor = when {
        isSelected -> HotstarBlue.copy(alpha = 0.2f)
        isFocused -> SurfaceOverlay
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (isSelected) Modifier.border(1.dp, HotstarBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel number
        Text(
            channel.channelNo.toString(),
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.width(28.dp)
        )

        // Channel image
        if (channel.image.isNotEmpty()) {
            AsyncImage(
                model = channel.image,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LiveTv, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                channel.name,
                color = if (isSelected) HotstarBlueLight else TextPrimary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Live indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StatusLive)
            )
        }
    }
}
