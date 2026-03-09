package com.shadow.hellotv.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadow.hellotv.LocalIsTv
import com.shadow.hellotv.ui.theme.*
import kotlin.system.exitProcess

@Composable
fun ExitDialog(
    showExitDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showExitDialog) {
        BackHandler(enabled = true) { onDismiss() }
        ExitDialogContent(onCancel = onDismiss)
    }
}

@Composable
private fun ExitDialogContent(onCancel: () -> Unit) {
    val context = LocalContext.current
    val isTv = LocalIsTv.current
    val config = LocalConfiguration.current
    val isPortrait = config.screenHeightDp > config.screenWidthDp
    val isSmall = config.screenWidthDp < 400

    val cancelFocus = remember { FocusRequester() }
    val exitFocus = remember { FocusRequester() }
    var cancelFocused by remember { mutableStateOf(false) }
    var exitFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { cancelFocus.requestFocus() }

    fun doExit() {
        try {
            (context as? ComponentActivity)?.finishAndRemoveTask()
                ?: (context as? Activity)?.finishAndRemoveTask()
                ?: exitProcess(0)
        } catch (_: Exception) {
            exitProcess(0)
        }
    }

    // Overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerOverlay)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCancel() }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.Back, Key.Escape -> { onCancel(); true }
                        Key.DirectionLeft, Key.DirectionRight -> {
                            if (exitFocused) cancelFocus.requestFocus()
                            else exitFocus.requestFocus()
                            true
                        }
                        else -> false
                    }
                } else false
            },
        contentAlignment = Alignment.Center
    ) {
        // Dialog card - responsive width
        val cardWidth = when {
            isSmall || isPortrait -> Modifier.fillMaxWidth(0.85f)
            else -> Modifier.width(420.dp)
        }

        Box(
            modifier = Modifier
                .then(cardWidth)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = StatusLive.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(SurfaceCard, SurfaceDark)
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.03f))
                    ),
                    RoundedCornerShape(24.dp)
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* block clicks from closing */ }
                .padding(if (isPortrait) 24.dp else 32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                val iconSize = if (isPortrait) 56.dp else 72.dp
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    StatusLive.copy(alpha = 0.2f),
                                    StatusLive.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            StatusLive.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning, null,
                        tint = StatusLive,
                        modifier = Modifier.size(iconSize * 0.5f)
                    )
                }

                Spacer(Modifier.height(if (isPortrait) 16.dp else 24.dp))

                Text(
                    "Exit App?",
                    color = TextPrimary,
                    fontSize = if (isPortrait) 22.sp else 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Are you sure you want to close HelloTV?",
                    color = TextSecondary,
                    fontSize = if (isPortrait) 13.sp else 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(if (isPortrait) 20.dp else 28.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel
                    ExitBtn(
                        text = "Cancel",
                        isFocused = cancelFocused,
                        focusRequester = cancelFocus,
                        onFocusChange = { cancelFocused = it },
                        isPortrait = isPortrait,
                        colors = listOf(AccentGold, AccentGoldDark),
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )

                    // Exit
                    ExitBtn(
                        text = "Exit",
                        icon = Icons.Default.ExitToApp,
                        isFocused = exitFocused,
                        focusRequester = exitFocus,
                        onFocusChange = { exitFocused = it },
                        isPortrait = isPortrait,
                        colors = listOf(StatusLive, StatusLive.copy(alpha = 0.8f)),
                        onClick = ::doExit,
                        modifier = Modifier.weight(1f)
                    )
                }

                // TV navigation hint
                if (isTv) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "\u2190 \u2192 to navigate \u2022 OK to select",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ExitBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    isPortrait: Boolean,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val btnHeight = if (isPortrait) 44.dp else 50.dp

    Box(
        modifier = modifier
            .height(btnHeight)
            .shadow(
                if (isFocused) 12.dp else 0.dp,
                RoundedCornerShape(12.dp),
                spotColor = colors[0].copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused)
                    Brush.horizontalGradient(colors)
                else
                    Brush.horizontalGradient(colors.map { it.copy(alpha = 0.15f) })
            )
            .then(
                if (isFocused) Modifier.border(1.5.dp, colors[0], RoundedCornerShape(12.dp))
                else Modifier
            )
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChange(it.isFocused) }
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.Enter || event.key == Key.DirectionCenter)
                ) {
                    onClick(); true
                } else false
            }
            .clickable { onClick() }
            .scale(if (isFocused) 1.03f else 1f),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    icon, null,
                    tint = Color.White.copy(alpha = if (isFocused) 1f else 0.7f),
                    modifier = Modifier.size(if (isPortrait) 16.dp else 18.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text,
                color = Color.White.copy(alpha = if (isFocused) 1f else 0.7f),
                fontSize = if (isPortrait) 14.sp else 16.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}
