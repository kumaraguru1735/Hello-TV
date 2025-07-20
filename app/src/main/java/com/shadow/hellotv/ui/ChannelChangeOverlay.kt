package com.shadow.hellotv.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shadow.hellotv.model.ChannelItem

//ChannelChangeOverlay(
//show = showChannelChangeOverlay,
//showChannelList = showChannelList,
//channels = channels,
//selectedChannelIndex = selectedChannelIndex,
//modifier = Modifier.align(Alignment.BottomCenter)
//)

@Composable
fun ChannelChangeOverlay(
    show: Boolean,
    showChannelList: Boolean,
    channels: List<ChannelItem>,
    selectedChannelIndex: Int,
    modifier: Modifier = Modifier
) {
    if (show && !showChannelList) {
        Card(
            modifier = modifier
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                channels.getOrNull(selectedChannelIndex)?.let { channel ->
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = "Channel Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .padding(end = 16.dp),
                        onError = {}
                    )
                    Column {
                        Text(
                            text = "${selectedChannelIndex + 1}. ${channel.name}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (channel.category.isNotEmpty()) {
                            Text(
                                text = channel.category,
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
