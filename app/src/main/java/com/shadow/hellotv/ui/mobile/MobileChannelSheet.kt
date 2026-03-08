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
    val listState = rememberLazyListState()

    // Filter channels by search
    val filteredChannels = remember(searchQuery, vm.channels) {
        if (searchQuery.isEmpty()) vm.channels
        else vm.channels.filter { it.name.contains(searchQuery, ignoreCase = true) || it.channelNo.toString().contains(searchQuery) }
    }

    LaunchedEffect(vm.selectedChannelIndex) {
        if (filteredChannels.isNotEmpty() && searchQuery.isEmpty()) {
            listState.animateScrollToItem(vm.selectedChannelIndex.coerceIn(0, filteredChannels.size - 1))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextMuted)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(HotstarBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text("Search channels...", color = TextDisabled, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Clear, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            )

            // Language filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                item {
                    MobileFilterChip("All", vm.selectedLanguageId == null) {
                        vm.selectedLanguageId = null
                        vm.applyFilters()
                    }
                }
                items(vm.languages) { lang ->
                    MobileFilterChip(lang.name, vm.selectedLanguageId == lang.id) {
                        vm.selectedLanguageId = if (vm.selectedLanguageId == lang.id) null else lang.id
                        vm.applyFilters()
                    }
                }
            }

            // Category filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    MobileFilterChip("All", vm.selectedCategoryId == null) {
                        vm.selectedCategoryId = null
                        vm.applyFilters()
                    }
                }
                items(vm.categories) { cat ->
                    MobileFilterChip(cat.name, vm.selectedCategoryId == cat.id) {
                        vm.selectedCategoryId = if (vm.selectedCategoryId == cat.id) null else cat.id
                        vm.applyFilters()
                    }
                }
            }

            // Channel list
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(filteredChannels) { index, channel ->
                    val originalIndex = if (searchQuery.isEmpty()) index else vm.channels.indexOf(channel)
                    MobileChannelItem(
                        channel = channel,
                        isSelected = originalIndex == vm.selectedChannelIndex,
                        onClick = { onChannelSelected(originalIndex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) HotstarBlue else SurfaceElevated)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun MobileChannelItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) HotstarBlue.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            channel.channelNo.toString(),
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.width(30.dp)
        )

        if (channel.image.isNotEmpty()) {
            AsyncImage(
                model = channel.image,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LiveTv, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.width(10.dp))

        Text(
            channel.name,
            color = if (isSelected) HotstarBlueLight else TextPrimary,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

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
