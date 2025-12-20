package org.android.prismplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DeleteConfirmationDialog(
    presetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_light")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), // Faster blink for alert
        label = "alpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF0F0F0F))
                .border(1.dp, MaterialTheme.colorScheme.error.copy(0.5f), RoundedCornerShape(2.dp)) // Red border for danger
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A0505))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SYSTEM_ALERT // DELETION",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.error
                )

                // Live Status Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "WARNING",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(alpha)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.error.copy(0.2f))

            // 2. CONTENT AREA
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Technical Label
                Text(
                    text = "> TARGET_ID: '$presetName'",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "This action is irreversible. The configuration data will be permanently erased from local storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. FOOTER ACTIONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[ CANCEL ]",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    // Destructive Action
                    Box(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(0.5f), RoundedCornerShape(2.dp))
                            .clickable { onConfirm() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "CONFIRM_ERASE",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun DeleteConfirmationDialogPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color.White,
            error = Color(0xFFFF4444), // Explicit bright red for preview
            onSurface = Color.White,
            onSurfaceVariant = Color.Gray,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.8f)),
            contentAlignment = Alignment.Center
        ) {
            DeleteConfirmationDialog(
                presetName = "MY_BASS_CONFIG_01",
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}