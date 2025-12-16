package org.android.prismplayer.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

data class FolderItem(val name: String, val path: String, val count: Int, var isSelected: Boolean)

@Composable
fun FolderSelectionScreen(
    folders: List<FolderItem>,
    onFinish: (List<String>) -> Unit
) {
    var currentFolders by remember { mutableStateOf(folders) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF9C27B0).copy(0.15f), Color.Transparent),
                    center = Offset(width, 0f),
                    radius = width * 1.3f
                ),
                center = Offset(width, 0f),
                radius = width * 1.3f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1DB954).copy(0.1f), Color.Transparent),
                    center = Offset(0f, height),
                    radius = width * 1.2f
                ),
                center = Offset(0f, height),
                radius = width * 1.2f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "SOURCES",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF1DB954),
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select Your\nFolders",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                lineHeight = 44.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "We found ${folders.size} folders with audio files. Uncheck the ones you want to ignore (like WhatsApp Audio).",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.5f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(currentFolders) { folder ->
                    FolderRow(
                        item = folder,
                        onToggle = {
                            currentFolders = currentFolders.map {
                                if (it.path == folder.path) it.copy(isSelected = !it.isSelected) else it
                            }
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
        ) {
            Button(
                onClick = {
                    val selectedPaths = currentFolders.filter { it.isSelected }.map { it.path }
                    onFinish(selectedPaths)
                },
                modifier = Modifier
                    .height(64.dp)
                    .shadow(
                        elevation = 20.dp,
                        spotColor = Color(0xFF1DB954),
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1DB954),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) {
                Text("Enter Prism", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.ArrowForward, null)
            }
        }
    }
}

@Composable
fun FolderRow(item: FolderItem, onToggle: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (item.isSelected) Color(0xFF1DB954) else Color.White.copy(0.1f),
        label = "border"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF121212).copy(0.8f))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Folder,
                null,
                tint = if(item.isSelected) Color.White else Color.White.copy(0.3f)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if(item.isSelected) Color.White else Color.White.copy(0.4f)
            )
            Text(
                text = "${item.count} tracks",
                style = MaterialTheme.typography.labelMedium,
                color = if(item.isSelected) Color(0xFF1DB954) else Color.White.copy(0.2f)
            )
        }

        NeonCheckbox(checked = item.isSelected)
    }
}

@Composable
fun NeonCheckbox(checked: Boolean) {
    val color by animateColorAsState(if (checked) Color(0xFF1DB954) else Color.White.copy(0.1f))
    val scale by animateFloatAsState(if (checked) 1f else 0.8f)

    Box(
        modifier = Modifier
            .size(24.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (checked) color else Color.Transparent)
            .border(2.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(Icons.Rounded.Check, null, tint = Color.Black, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewFolderSelect() {
    val mocks = listOf(
        FolderItem("Downloads", "/path/dl", 142, true),
        FolderItem("Music", "/path/music", 580, true),
        FolderItem("WhatsApp Audio", "/path/wa", 12, false),
        FolderItem("Telegram", "/path/tg", 5, false)
    )
    MaterialTheme {
        FolderSelectionScreen(folders = mocks, onFinish = {})
    }
}