package com.shadow.hellotv.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.scale
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
import kotlin.system.exitProcess

@Composable
fun ExitDialog(
    showExitDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showExitDialog) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        ExitDialogContent(
            onExit = {
                onDismiss()
            },
            onCancel = onDismiss
        )
    }
}

@Composable
private fun ExitDialogContent(
    onExit: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val exitFocusRequester = remember { FocusRequester() }
    val cancelFocusRequester = remember { FocusRequester() }

    var exitButtonFocused by remember { mutableStateOf(false) }
    var cancelButtonFocused by remember { mutableStateOf(false) }

    // Auto-focus the Cancel button (safer default)
    LaunchedEffect(Unit) {
        cancelFocusRequester.requestFocus()
    }

    // Full screen overlay with blur effect
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xCC000000),
                        Color(0xEE000000)
                    )
                )
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onCancel()
            }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Back, Key.Escape -> {
                            onCancel()
                            true
                        }
                        Key.DirectionLeft, Key.DirectionRight -> {
                            if (exitButtonFocused) {
                                cancelFocusRequester.requestFocus()
                            } else {
                                exitFocusRequester.requestFocus()
                            }
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        // Modern dialog card
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .width(480.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    // Prevent clicks on card from closing dialog
                },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1C1C1E)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon with gradient background
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF6B6B),
                                    Color(0xFFFF4757)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Exit Warning",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Exit Application?",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "Are you sure you want to close the app?\nAll progress will be saved.",
                    color = Color(0xFFB0B0B0),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cancel Button (Primary)
                    DialogButton(
                        text = "Cancel",
                        isFocused = cancelButtonFocused,
                        isPrimary = true,
                        focusRequester = cancelFocusRequester,
                        onFocusChanged = { cancelButtonFocused = it },
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )

                    // Exit Button (Danger)
                    DialogButton(
                        text = "Exit",
                        isFocused = exitButtonFocused,
                        isPrimary = false,
                        isDanger = true,
                        focusRequester = exitFocusRequester,
                        onFocusChanged = { exitButtonFocused = it },
                        onClick = {
                            // Multiple methods to ensure app exits on both mobile and TV
                            try {
                                // Method 1: Try ComponentActivity
                                (context as? ComponentActivity)?.let { activity ->
                                    activity.finishAndRemoveTask()
                                    return@DialogButton
                                }

                                // Method 2: Try regular Activity
                                (context as? Activity)?.let { activity ->
                                    activity.finishAndRemoveTask()
                                    return@DialogButton
                                }

                                // Method 3: Force exit as last resort
                                exitProcess(0)
                            } catch (e: Exception) {
                                // If all else fails
                                exitProcess(0)
                            } finally {
                                onExit()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instructions (only visible on TV)
                Text(
                    text = "Use ← → to navigate • OK to select",
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DialogButton(
    text: String,
    isFocused: Boolean,
    isPrimary: Boolean = false,
    isDanger: Boolean = false,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isDanger && isFocused -> Color(0xFFFF4757)
        isDanger -> Color(0xFFE84142)
        isPrimary && isFocused -> Color(0xFF0A84FF)
        isPrimary -> Color(0xFF0066CC)
        isFocused -> Color(0xFF3A3A3C)
        else -> Color(0xFF2C2C2E)
    }

    val scale = if (isFocused) 1.05f else 1f

    Button(
        onClick = onClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .height(56.dp)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .scale(scale)
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isFocused) 8.dp else 2.dp,
            pressedElevation = 12.dp
        )
    ) {
        if (isDanger) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}