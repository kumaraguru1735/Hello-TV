package com.shadow.hellotv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TvControlsHint(modifier: Modifier = Modifier) {
    Box(modifier = modifier) { // Apply the modifier here
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "TV: OK for menu, ↑↓ to change, → for settings, ← to exit",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "Touch: Tap for info, Drag ↕ to change",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}
