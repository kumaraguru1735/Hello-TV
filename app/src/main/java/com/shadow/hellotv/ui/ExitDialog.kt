package com.shadow.hellotv.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    // Modal overlay with blur feel
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
        // Entrance animation: scaleIn + fadeIn
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(300))
        ) {
            // Dialog card - max width 320dp
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = StatusLive.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.03f))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* block clicks from closing */ }
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Warning icon - 48dp with scale-in, StatusLive tint with subtle glow
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(12.dp, CircleShape, spotColor = StatusLive.copy(0.4f))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        StatusLive.copy(alpha = 0.25f),
                                        StatusLive.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .border(1.dp, StatusLive.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning, null,
                            tint = StatusLive,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Title
                    Text(
                        "Exit HelloTV?",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    // Message
                    Text(
                        "Are you sure you want to close HelloTV?",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    // Buttons side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button - SurfaceElevated bg
                        ExitBtn(
                            text = "Cancel",
                            isFocused = cancelFocused,
                            focusRequester = cancelFocus,
                            onFocusChange = { cancelFocused = it },
                            bgColor = SurfaceElevated,
                            focusBorderColor = AccentGold,
                            isTv = isTv,
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        )

                        // Exit button - StatusLive bg
                        ExitBtn(
                            text = "Exit",
                            icon = Icons.Default.ExitToApp,
                            isFocused = exitFocused,
                            focusRequester = exitFocus,
                            onFocusChange = { exitFocused = it },
                            bgColor = StatusLive,
                            focusBorderColor = AccentGold,
                            isTv = isTv,
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
}

@Composable
private fun ExitBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    bgColor: Color,
    focusBorderColor: Color,
    isTv: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .then(
                if (isFocused) Modifier.border(1.5.dp, focusBorderColor, RoundedCornerShape(12.dp))
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    icon, null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
