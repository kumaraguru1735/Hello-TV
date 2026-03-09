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
import androidx.compose.material.icons.filled.Equalizer
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
                        TvSurface.copy(alpha = 0.97f),
                        TvSurface.copy(alpha = 0.95f),
                        TvSurface.copy(alpha = 0.85f),
                        Color.Transparent
                    )
                )
            )
            .padding(top = 12.dp, start = 12.dp, bottom = 12.dp, end = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Language tabs at top ──
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                item {
                    TvLanguageTab(
                        label = "All",
                        selected = selectedLanguageId == null,
                        onClick = { onLanguageSelected(null) }
                    )
                }
                items(languages) { lang ->
                    TvLanguageTab(
                        label = lang.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = selectedLanguageId == lang.id,
                        onClick = { onLanguageSelected(if (selectedLanguageId == lang.id) null else lang.id) }
                    )
                }
            }

            // ── Categories (left) + Channels (right) ──
            Row(modifier = Modifier.fillMaxSize()) {
                // Category column
                LazyColumn(
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight()
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item {
                        TvCategoryItem(
                            name = "All",
                            selected = selectedCategoryId == null,
                            onClick = { onCategorySelected(null) }
                        )
                    }
                    items(categories) { cat ->
                        TvCategoryItem(
                            name = if (cat.name.all { it.isUpperCase() || !it.isLetter() } && cat.name.length <= 3) cat.name
                                else cat.name.lowercase().replaceFirstChar { it.uppercase() },
                            selected = selectedCategoryId == cat.id,
                            onClick = { onCategorySelected(if (selectedCategoryId == cat.id) null else cat.id) }
                        )
                    }
                }

                // Vertical separator
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .fillMaxHeight()
                        .background(TvSeparator)
                )

                Spacer(Modifier.width(6.dp))

                // Channel list
                LazyColumn(
                    state = channelListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    itemsIndexed(channels) { index, channel ->
                        TvChannelItem(
                            channel = channel,
                            isSelected = index == selectedChannelIndex,
                            onClick = { onChannelSelected(index) }
                        )
                        // Separator
                        if (index < channels.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 48.dp)
                                    .height(0.5.dp)
                                    .background(TvSeparator.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvLanguageTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .then(
                when {
                    selected -> Modifier.background(AccentGold)
                    isFocused -> Modifier
                        .background(AccentGold.copy(alpha = 0.2f))
                        .border(1.dp, AccentGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    else -> Modifier
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                }
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = when {
                selected -> Color.Black
                isFocused -> AccentGold
                else -> Color.White.copy(alpha = 0.7f)
            },
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun TvCategoryItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Text(
        name,
        color = when {
            selected -> AccentGold
            isFocused -> AccentGoldLight
            else -> Color.White.copy(alpha = 0.4f)
        },
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .then(
                when {
                    isFocused -> Modifier.background(AccentGold.copy(alpha = 0.08f))
                    else -> Modifier
                }
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp)
    )
}

@Composable
private fun TvChannelItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .then(
                when {
                    isSelected -> Modifier
                        .border(1.dp, AccentGold, RoundedCornerShape(6.dp))
                        .background(AccentGold.copy(alpha = 0.08f))
                    isFocused -> Modifier
                        .background(AccentGold.copy(alpha = 0.06f))
                        .border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    else -> Modifier
                }
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel image
        if (channel.image.isNotEmpty()) {
            AsyncImage(
                model = channel.image,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LiveTv, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.width(10.dp))

        Text(
            channel.name,
            color = if (isSelected) Color.White else if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Equalizer icon for playing channel
        if (isSelected) {
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Default.Equalizer,
                contentDescription = "Playing",
                tint = AccentGold,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
