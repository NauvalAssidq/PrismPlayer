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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.android.prismplayer.data.model.Song

@Composable
fun GenerateAiPlaylistSheet(
    allSongs: List<Song>,
    onCurate: suspend (String, List<Song>) -> Result<List<Song>>,
    onCreatePlaylistWithSongs: (String, List<Song>) -> Unit,
    onDismiss: () -> Unit
) {
    var promptText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
            .imePadding()
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
                text = "SYS_AI // VIBE_CURATOR",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
                fontSize = 11.sp
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

        // 3. INPUT AREA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "> DESCRIBE_MOOD_OR_VIBE:",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            BasicTextField(
                value = promptText,
                onValueChange = {
                    promptText = it
                    errorMessage = null
                },
                enabled = !isLoading,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (promptText.isNotBlank() && !isLoading) {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = onCurate(promptText.trim(), allSongs)
                                isLoading = false
                                result.fold(
                                    onSuccess = { matchedSongs ->
                                        if (matchedSongs.isEmpty()) {
                                            errorMessage = "No matching songs found for this vibe."
                                        } else {
                                            onCreatePlaylistWithSongs(promptText.trim(), matchedSongs)
                                            onDismiss()
                                        }
                                    },
                                    onFailure = { err ->
                                        errorMessage = err.message ?: "Failed to generate playlist."
                                    }
                                )
                            }
                        }
                    }
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.3f), RoundedCornerShape(4.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (promptText.isEmpty()) {
                            Text(
                                text = "e.g. Late night synthwave drive",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. FOOTER ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[ CURATING_WITH_GEMINI... ]",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
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
                        text = "[ GENERATE PLAYLIST ]",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (promptText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.2f),
                        modifier = Modifier
                            .clickable(enabled = promptText.isNotBlank()) {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val result = onCurate(promptText.trim(), allSongs)
                                    isLoading = false
                                    result.fold(
                                        onSuccess = { matchedSongs ->
                                            if (matchedSongs.isEmpty()) {
                                                errorMessage = "No matching songs found for this vibe."
                                            } else {
                                                onCreatePlaylistWithSongs(promptText.trim(), matchedSongs)
                                                onDismiss()
                                            }
                                        },
                                        onFailure = { err ->
                                            errorMessage = err.message ?: "Failed to generate playlist."
                                        }
                                    )
                                }
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
