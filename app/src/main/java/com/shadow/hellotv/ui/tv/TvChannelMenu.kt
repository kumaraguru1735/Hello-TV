package com.shadow.hellotv.ui.tv

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
                        TvSurface.copy(alpha = 0.92f),
                        TvSurface.copy(alpha = 0.70f),
                        Color.Transparent
                    )
                )
            )
            .padding(top = 12.dp, start = 12.dp, bottom = 12.dp, end = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Language tabs at top ──
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        .width(140.dp)
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
                                    .padding(start = 52.dp)
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

    val bgColor by animateColorAsState(
        targetValue = when {
            selected -> AccentGold
            isFocused -> AccentGoldSoft
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            selected -> Color.Black
            isFocused -> AccentGold
            else -> TextSecondary
        },
        animationSpec = tween(200),
        label = "tabText"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            selected -> Color.Transparent
            isFocused -> AccentGold
            else -> SurfaceSeparator
        },
        animationSpec = tween(200),
        label = "tabBorder"
    )

    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
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

    val textColor by animateColorAsState(
        targetValue = when {
            selected -> TextPrimary
            isFocused -> AccentGold
            else -> TextMuted
        },
        animationSpec = tween(200),
        label = "catText"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            selected -> AccentGoldSoft
            isFocused -> AccentGold.copy(alpha = 0.08f)
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "catBg"
    )

    Text(
        name,
        color = textColor,
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    )
}

@Composable
private fun TvChannelItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> AccentGoldSoft
            isFocused -> Color.White.copy(alpha = 0.06f)
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "chBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> AccentGold
            isFocused -> Color.White.copy(alpha = 0.3f)
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "chBorder"
    )
    val borderWidth by animateFloatAsState(
        targetValue = when {
            isSelected -> 1.5f
            isFocused -> 0.5f
            else -> 0f
        },
        animationSpec = tween(200),
        label = "chBorderWidth"
    )
    val nameColor by animateColorAsState(
        targetValue = when {
            isSelected -> TextPrimary
            isFocused -> TextPrimary
            else -> TextSecondary
        },
        animationSpec = tween(200),
        label = "chName"
    )

    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .then(
                if (borderWidth > 0f) Modifier.border(borderWidth.dp, borderColor, shape)
                else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel image
        if (channel.image.isNotEmpty()) {
            AsyncImage(
                model = channel.image,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LiveTv, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            channel.name,
            color = nameColor,
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
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
