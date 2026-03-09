package com.shadow.hellotv.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadow.hellotv.ui.theme.*

@Composable
fun TvChannelNumberOverlay(
    input: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(top = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TvSurfaceCard.copy(alpha = 0.95f))
            .border(1.dp, AccentGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Go to Channel", color = TextSecondary, fontSize = 11.sp)
            Text(
                input,
                color = AccentGold,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
