package org.android.prismplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeleteConfirmationDialog(
    presetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.15f),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
            .navigationBarsPadding()
    ) {
        // 1. TOP GRIP HANDLE STRIP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(0.15f))
                    )
                }
            }
        }

        // 2. TITLE BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SYSTEM_ALERT // DELETION",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error,
                letterSpacing = 2.sp,
                fontSize = 11.sp
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

        // 3. CONTENT AREA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "> TARGET_ID:",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "'$presetName'",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This action is irreversible. The configuration data will be permanently erased.",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. FOOTER ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[ ABORT ]",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "[ ERASE ]",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable { onConfirm() }
                        .padding(8.dp)
                )
            }
        }
    }
}