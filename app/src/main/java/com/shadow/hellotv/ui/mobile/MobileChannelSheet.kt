package com.shadow.hellotv.ui.mobile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shadow.hellotv.model.Channel
import com.shadow.hellotv.ui.theme.*
import com.shadow.hellotv.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileChannelSheet(
    vm: MainViewModel,
    onDismiss: () -> Unit,
    onChannelSelected: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val channelListState = rememberLazyListState()

    // Filter channels by search
    val filteredChannels = remember(searchQuery, vm.channels) {
        if (searchQuery.isEmpty()) vm.channels
        else vm.channels.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.channelNo.toString().contains(searchQuery)
        }
    }

    LaunchedEffect(vm.selectedChannelIndex) {
        if (filteredChannels.isNotEmpty() && searchQuery.isEmpty()) {
            channelListState.animateScrollToItem(vm.selectedChannelIndex.coerceIn(0, filteredChannels.size - 1))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfacePrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AccentGold)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // -- Search bar --
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                cursorBrush = SolidColor(AccentGold),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceInput)
                    .border(0.5.dp, SurfaceSeparator, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text("Search channels...", color = TextMuted, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                Icons.Default.Clear, null,
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            )

            // -- Language chips (horizontal, top) --
            if (vm.languages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    item {
                        LanguageChip("All", vm.selectedLanguageId == null) {
                            vm.selectedLanguageId = null
                            vm.applyFilters()
                        }
                    }
                    items(vm.languages) { lang ->
                        LanguageChip(lang.name, vm.selectedLanguageId == lang.id) {
                            vm.selectedLanguageId = if (vm.selectedLanguageId == lang.id) null else lang.id
                            vm.applyFilters()
                        }
                    }
                }
            }

            // -- Main content: Categories (left) + Channels (right) --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // -- Left: Category column --
                if (vm.categories.isNotEmpty() && searchQuery.isEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .width(90.dp)
                            .fillMaxHeight()
                            .background(SurfaceDark),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            CategoryItem("All", vm.selectedCategoryId == null) {
                                vm.selectedCategoryId = null
                                vm.applyFilters()
                            }
                        }
                        items(vm.categories) { cat ->
                            CategoryItem(cat.name, vm.selectedCategoryId == cat.id) {
                                vm.selectedCategoryId = if (vm.selectedCategoryId == cat.id) null else cat.id
                                vm.applyFilters()
                            }
                        }
                    }
                }

                // -- Right: Channel list --
                LazyColumn(
                    state = channelListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    // Channel count header
                    item {
                        Text(
                            "${filteredChannels.size} channels",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp, top = 4.dp)
                        )
                    }

                    itemsIndexed(filteredChannels) { index, channel ->
                        val originalIndex = if (searchQuery.isEmpty()) index else vm.channels.indexOf(channel)
                        ChannelRow(
                            channel = channel,
                            isSelected = originalIndex == vm.selectedChannelIndex,
                            onClick = { onChannelSelected(originalIndex) }
                        )
                        // Separator
                        if (index < filteredChannels.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 54.dp),
                                thickness = 0.5.dp,
                                color = SurfaceSeparator
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) AccentGold else Color.Transparent,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.Black else TextSecondary,
        animationSpec = tween(200),
        label = "chipText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .then(
                if (!selected) Modifier.border(1.dp, SurfaceSeparator, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            color = textColor,
            fontSize = 13.sp,
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
    val bgColor by animateColorAsState(
        targetValue = if (selected) AccentGoldSoft else Color.Transparent,
        animationSpec = tween(200),
        label = "catBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) AccentGold else TextMuted,
        animationSpec = tween(200),
        label = "catText"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gold left accent bar for selected
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) AccentGold else Color.Transparent)
        )
        Spacer(Modifier.width(9.dp))
        Text(
            name,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val nameColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextSecondary,
        animationSpec = tween(200),
        label = "chName"
    )
    val numColor by animateColorAsState(
        targetValue = if (isSelected) AccentGold else TextMuted,
        animationSpec = tween(200),
        label = "chNum"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel number
        Text(
            channel.channelNo.toString(),
            color = numColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.width(6.dp))

        // Logo
        if (channel.image.isNotEmpty()) {
            AsyncImage(
                model = channel.image,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LiveTv, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.width(10.dp))

        // Name
        Text(
            channel.name,
            color = nameColor,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Selected indicator: gold dot
        if (isSelected) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AccentGold)
            )
        }
    }
}
