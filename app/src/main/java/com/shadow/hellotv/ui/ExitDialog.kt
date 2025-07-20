package com.shadow.hellotv.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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

@Composable
fun ExitDialog(
    showExitDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showExitDialog) {
        // Handle back button only when dialog is showing
        BackHandler(enabled = true) {
            onDismiss()
        }

        ExitDialogContent(
            onExit = onDismiss, // Just dismiss the dialog, activity.finish() is handled inside
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

    // Auto-focus the Exit button when dialog appears
    LaunchedEffect(Unit) {
        exitFocusRequester.requestFocus()
    }

    // Full screen overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .onKeyEvent { keyEvent ->
                // Consume ALL key events to prevent them from reaching parent
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Back, Key.Escape -> {
                            onCancel()
                        }
                        Key.DirectionLeft -> {
                            exitFocusRequester.requestFocus()
                        }
                        Key.DirectionRight -> {
                            cancelFocusRequester.requestFocus()
                        }
                        Key.Enter, Key.NumPadEnter -> {
                            // Let buttons handle enter/ok
                        }
                    }
                }
                true // Always consume the event to prevent parent handling
            }
    ) {
        // Main dialog card
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 500.dp, height = 300.dp), // Fixed size for TV
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Exit Application",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Are you sure you want to exit?",
                        color = Color(0xFFCCCCCC),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Buttons section
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Exit Button (Red)
                    Button(
                        onClick = {
                            (context as? ComponentActivity)?.finish()
                            onExit()
                        },
                        modifier = Modifier
                            .focusRequester(exitFocusRequester)
                            .focusable()
                            .size(width = 140.dp, height = 60.dp)
                            .onFocusChanged { exitButtonFocused = it.isFocused }
                            .then(
                                if (exitButtonFocused) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                } else Modifier
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (exitButtonFocused)
                                Color(0xFFFF4444) else Color(0xFFCC3333),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "EXIT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Cancel Button (Gray)
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .focusRequester(cancelFocusRequester)
                            .focusable()
                            .size(width = 140.dp, height = 60.dp)
                            .onFocusChanged { cancelButtonFocused = it.isFocused }
                            .then(
                                if (cancelButtonFocused) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                } else Modifier
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (cancelButtonFocused)
                                Color(0xFF666666) else Color(0xFF444444),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "CANCEL",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Instructions section
                Text(
                    text = "Use ← → to navigate • OK to select • BACK to cancel",
                    color = Color(0xFF999999),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}