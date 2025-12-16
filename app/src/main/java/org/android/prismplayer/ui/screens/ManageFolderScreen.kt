package org.android.prismplayer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFoldersScreen(
    currentPaths: List<String>,
    onSave: (List<String>) -> Unit,
    onBack: () -> Unit
) {
    var folderList by remember { mutableStateOf(currentPaths) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val path = it.path?.replace("/tree/primary:", "/storage/emulated/0/") ?: it.toString()
            if (!folderList.contains(path)) folderList = folderList + path
        }
    }

    val glassBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.03f)
        )
    )
    val glassBorder = Color.White.copy(alpha = 0.12f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        AuraBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.padding(top = 12.dp),
                    title = {
                        Text(
                            text = "Manage Library",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(40.dp)
                                .clip(RoundedCornerShape(60.dp))
                                .background(glassBrush)
                                .border(1.dp, glassBorder, RoundedCornerShape(60.dp))
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Small subtitle (matches Settings muted label vibe)
                Text(
                    text = "${folderList.size} folders active",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.55f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            text = "LIBRARY",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(0.4f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(folderList) { path ->
                        FolderRowGlass(
                            path = path,
                            glassBrush = glassBrush,
                            glassBorder = glassBorder,
                            onRemove = { folderList = folderList - path }
                        )
                    }

                    item {
                        // Add new folder (glass)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(glassBrush)
                                .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
                                .clickable { launcher.launch(null) }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(0.10f),
                                                Color.White.copy(0.03f)
                                            )
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = Color.White.copy(0.85f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Add Storage Location",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Pick a folder to scan for music",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(0.5f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(0.25f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Bottom button (same as your green pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                ) {
                    Button(
                        onClick = { onSave(folderList) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Rescan Library",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderRowGlass(
    path: String,
    glassBrush: Brush,
    glassBorder: Color,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(glassBrush)
            .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(0.10f),
                            Color.White.copy(0.03f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = null,
                tint = Color.White.copy(0.75f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = File(path).name.ifEmpty { "Root" },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = path,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(24))
                .background(Color.White.copy(0.06f))
                .border(1.dp, Color.White.copy(0.10f), RoundedCornerShape(24))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Remove",
                tint = Color(0xFFCF6679),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AuraBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1DB954).copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, -100f),
                radius = width * 1.3f
            ),
            center = Offset(width * 0.5f, -100f),
            radius = width * 1.3f
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewManageFoldersScreen() {
    MaterialTheme {
        ManageFoldersScreen(
            currentPaths = listOf(
                "/storage/emulated/0/Music",
                "/storage/emulated/0/Download"
            ),
            onSave = {},
            onBack = {}
        )
    }
}
