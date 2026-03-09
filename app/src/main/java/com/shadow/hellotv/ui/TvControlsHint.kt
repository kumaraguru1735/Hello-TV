package com.shadow.hellotv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadow.hellotv.ui.theme.*

@Composable
fun TvControlsHint(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isTV = configuration.screenWidthDp.dp >= 1000.dp

    var show by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        show = true
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(400)) +
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
        exit = fadeOut(animationSpec = tween(300)) +
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(if (isTV) 24.dp else 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(
                    0.5.dp,
                    AccentGold.copy(alpha = 0.4f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = if (isTV) 18.dp else 14.dp, vertical = if (isTV) 14.dp else 10.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(if (isTV) 10.dp else 8.dp)
            ) {
                // Title
                Text(
                    text = "Controls",
                    color = AccentGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Control hints
                ControlHintRow(key = "OK", description = "Open Menu", isTV = isTV)
                ControlHintRow(key = "\u2191\u2193", description = "Change Channel", isTV = isTV)
                ControlHintRow(key = "\u2192", description = "Settings", isTV = isTV)
                ControlHintRow(key = "\u2190", description = "Close Panel", isTV = isTV)
                ControlHintRow(key = "VOL", description = "Volume Up/Down", isTV = isTV)
            }
        }
    }
}

@Composable
private fun ControlHintRow(
    key: String,
    description: String,
    isTV: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isTV) 10.dp else 8.dp)
    ) {
        // Key label badge
        Box(
            modifier = Modifier
                .width(if (isTV) 42.dp else 36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceElevated)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = key,
                color = AccentGold,
                fontSize = if (isTV) 11.sp else 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.3.sp
            )
        }

        // Description
        Text(
            text = description,
            color = TextSecondary,
            fontSize = if (isTV) 12.sp else 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
}
