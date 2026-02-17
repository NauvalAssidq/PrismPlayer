package org.android.prismplayer.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.ui.theme.PrismPlayerTheme

import org.android.prismplayer.data.model.FolderItem

// Reuse the same data structure logic but for folders


@Composable
fun FolderSelectionScreen(
    folders: List<FolderItem>,
    onFinish: (List<String>) -> Unit
) {
    // Local state for toggling
    var currentFolders by remember { mutableStateOf(folders) }

    // Calculate system status
    val mountedCount = currentFolders.count { it.isSelected }
    val totalTracks = currentFolders.filter { it.isSelected }.sumOf { it.count }
    val isReady = mountedCount > 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Same Dotted Matrix Background
        val dotColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40.dp.toPx()
            for (x in 0..size.width.toInt() step step.toInt()) {
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawCircle(
                        color = dotColor,
                        radius = 1.dp.toPx(),
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // --- HEADER SECTION (Identical Layout) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRISM OS // STORAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Reusing the Status Blinker concept
                StatusBlinker(isReady = isReady, readyText = "VOLUMES MOUNTED", waitingText = "NO MEDIA")
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Massive Title
            Text(
                text = "PARTITION\nMOUNT",
                style = MaterialTheme.typography.displayMedium, // Doto Font
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select storage partitions to mount. Unmounted volumes will be ignored by the audio engine.",
                style = MaterialTheme.typography.bodyMedium, // Mono
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- THE VOLUME RACK (List of "Modules") ---
            Text(
                text = "DETECTED VOLUMES [$mountedCount/${folders.size}]",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(currentFolders) { folder ->
                    VolumeModuleCard(
                        folder = folder,
                        onToggle = {
                            currentFolders = currentFolders.map {
                                if (it.path == folder.path) it.copy(isSelected = !it.isSelected) else it
                            }
                        }
                    )
                }
            }

            // --- FOOTER / BOOT LOG ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Boot Logs based on selection
                BootLogLine("SCAN_TARGET", "${mountedCount}_VOLUMES")
                BootLogLine("INDEX_EST", "${totalTracks}_TRACKS")

                Spacer(modifier = Modifier.height(16.dp))

                // The "Initialize" Button (Only visible if ready)
                if (isReady) {
                    Button(
                        onClick = {
                            onFinish(currentFolders.filter { it.isSelected }.map { it.path })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(4.dp), // Sharp corners
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary, // Red Action Button
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            "INITIALIZE SEQUENCE >",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VolumeModuleCard(
    folder: FolderItem,
    onToggle: () -> Unit
) {
    val isMounted = folder.isSelected

    // Exact same animation logic as ModuleCard
    val containerColor by animateColorAsState(
        targetValue = if (isMounted) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isMounted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
        label = "text"
    )
    val borderColor = if (isMounted) Color.Transparent else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable { onToggle() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Folder Name (The "Code Name")
                Text(
                    text = folder.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                // Path (The "Description")
                Text(
                    text = folder.path.replace("/storage/emulated/0/", "~/"),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            // Status Indicator
            if (isMounted) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "[ MOUNTED ]",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${folder.count} FILES",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            } else {
                Text(
                    text = "MOUNT >",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary, // Red prompt
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatusBlinker(isReady: Boolean, readyText: String, waitingText: String) {
    val color = if (isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val infiniteTransition = rememberInfiniteTransition(label = "status")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (isReady) readyText else waitingText,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(end = 8.dp)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(if (isReady) 1f else alpha)
                .background(color, CircleShape)
        )
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewFolderSelect() {
    val mocks = listOf(
        FolderItem("Music", "/storage/emulated/0/Music", 580, true),
        FolderItem("Downloads", "/storage/emulated/0/Downloads", 142, false),
    )
    PrismPlayerTheme {
        FolderSelectionScreen(folders = mocks, onFinish = {})
    }
}