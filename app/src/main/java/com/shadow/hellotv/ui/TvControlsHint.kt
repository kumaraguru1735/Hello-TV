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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                    initialOffsetX = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
        exit = fadeOut(animationSpec = tween(300)) +
                slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = tween(300)
                ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .padding(if (isTV) 24.dp else 16.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0xFF6366F1).copy(alpha = 0.3f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E1E2E).copy(alpha = 0.96f),
                                Color(0xFF2A2A3E).copy(alpha = 0.96f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.4f),
                                Color(0xFF8B5CF6).copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = if (isTV) 16.dp else 14.dp, vertical = if (isTV) 12.dp else 10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (isTV) 10.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(if (isTV) 28.dp else 24.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.3f),
                                        Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF6366F1).copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(if (isTV) 16.dp else 14.dp)
                        )
                    }

                    // Controls text
                    Column(
                        verticalArrangement = Arrangement.spacedBy(if (isTV) 4.dp else 3.dp)
                    ) {
                        ControlHintText(
                            text = "OK: Menu • ↑↓: Change • →: Settings",
                            isTV = isTV
                        )
                        ControlHintText(
                            text = "Touch: Tap info • Swipe ↕ change",
                            isTV = isTV
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ControlHintText(
    text: String,
    isTV: Boolean
) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.85f),
        fontSize = if (isTV) 11.sp else 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.3.sp
    )
}