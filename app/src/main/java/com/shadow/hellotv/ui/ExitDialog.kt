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
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color(0xFFEC4899).copy(alpha = 0.4f)
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    // Prevent clicks on card from closing dialog
                },
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1E2E),
                                Color(0xFF2A2A3E)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFEC4899).copy(alpha = 0.5f),
                                Color(0xFFEF4444).copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFEC4899).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFEC4899).copy(alpha = 0.3f),
                                            Color(0xFFEF4444).copy(alpha = 0.3f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFEC4899).copy(alpha = 0.6f),
                                            Color(0xFFEF4444).copy(alpha = 0.6f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Exit Warning",
                                tint = Color(0xFFEC4899),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Title
                    Text(
                        text = "Exit Application?",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    Text(
                        text = "Are you sure you want to close the app?\nAll progress will be saved.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(36.dp))

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
                                try {
                                    (context as? ComponentActivity)?.let { activity ->
                                        activity.finishAndRemoveTask()
                                        return@DialogButton
                                    }

                                    (context as? Activity)?.let { activity ->
                                        activity.finishAndRemoveTask()
                                        return@DialogButton
                                    }

                                    exitProcess(0)
                                } catch (e: Exception) {
                                    exitProcess(0)
                                } finally {
                                    onExit()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Instructions
                    Text(
                        text = "Use ← → to navigate • OK to select",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }
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
    val scale = if (isFocused) 1.05f else 1f

    Button(
        onClick = onClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .height(58.dp)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .scale(scale)
            .shadow(
                elevation = if (isFocused) 16.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isDanger)
                    Color(0xFFEC4899).copy(alpha = 0.5f)
                else
                    Color(0xFF6366F1).copy(alpha = 0.5f)
            )
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = if (isDanger) {
                                listOf(
                                    Color(0xFFEC4899),
                                    Color(0xFFEF4444)
                                )
                            } else {
                                listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = when {
                            isDanger && isFocused -> listOf(
                                Color(0xFFEC4899).copy(alpha = 0.4f),
                                Color(0xFFEF4444).copy(alpha = 0.4f)
                            )
                            isDanger -> listOf(
                                Color(0xFFEC4899).copy(alpha = 0.2f),
                                Color(0xFFEF4444).copy(alpha = 0.2f)
                            )
                            isPrimary && isFocused -> listOf(
                                Color(0xFF6366F1).copy(alpha = 0.4f),
                                Color(0xFF8B5CF6).copy(alpha = 0.4f)
                            )
                            isPrimary -> listOf(
                                Color(0xFF6366F1).copy(alpha = 0.2f),
                                Color(0xFF8B5CF6).copy(alpha = 0.2f)
                            )
                            isFocused -> listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.15f)
                            )
                            else -> listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDanger) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = if (isFocused) Color(0xFFEC4899) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 17.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}