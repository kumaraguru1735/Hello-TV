package com.shadow.hellotv.ui.mobile

import androidx.compose.foundation.background
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
        containerColor = Color(0xFF0D1B2A),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // ── Search bar ──
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                cursorBrush = SolidColor(HotstarBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text("Search channels...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                Icons.Default.Clear, null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp).clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            )

            // ── Language chips (horizontal, top) ──
            if (vm.languages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
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

            // ── Main content: Categories (left) + Channels (right) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // ── Left: Category column ──
                if (vm.categories.isNotEmpty() && searchQuery.isEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .width(90.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.03f)),
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

                // ── Right: Channel list ──
                LazyColumn(
                    state = channelListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // Channel count header
                    item {
                        Text(
                            "${filteredChannels.size} channels",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }

                    itemsIndexed(filteredChannels) { index, channel ->
                        val originalIndex = if (searchQuery.isEmpty()) index else vm.channels.indexOf(channel)
                        ChannelRow(
                            channel = channel,
                            isSelected = originalIndex == vm.selectedChannelIndex,
                            onClick = { onChannelSelected(originalIndex) }
                        )
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) HotstarBlue
                else Color.White.copy(alpha = 0.06f)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) HotstarBlue.copy(alpha = 0.15f) else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(HotstarBlue)
                )
            }
            Text(
                name,
                color = if (selected) HotstarBlueLight else Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) HotstarBlue.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel number
        Text(
            channel.channelNo.toString(),
            color = if (isSelected) HotstarBlue else Color.White.copy(alpha = 0.3f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        // Logo
        if (channel.image.isNotEmpty()) {
            AsyncImage(
                model = channel.image,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LiveTv, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.width(8.dp))

        // Name
        Text(
            channel.name,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Live indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StatusLive)
            )
        }
    }
}
