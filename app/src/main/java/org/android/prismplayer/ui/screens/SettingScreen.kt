package org.android.prismplayer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val isScanning by viewModel.isScanning.collectAsState()
    SettingsContent(
        isScanning = isScanning,
        onBack = onBack,
        onRescan = { viewModel.rescanLibrary() },
        onOpenEqualizer = onOpenEqualizer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    isScanning: Boolean,
    onBack: () -> Unit,
    onRescan: () -> Unit,
    onOpenEqualizer: () -> Unit
) {
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
                    title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, null)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "Audio",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(0.4f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                SettingsItem(
                    icon = Icons.Rounded.GraphicEq,
                    title = "Equalizer",
                    subtitle = "Adjust frequencies and bass",
                    onClick = onOpenEqualizer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Library",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(0.4f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                SettingsItem(
                    icon = Icons.Rounded.Sync,
                    title = "Rescan Library",
                    subtitle = "Refresh your music list",
                    onClick = onRescan
                )
            }
        }

        if (isScanning) {
            ScanningDialog()
        }
    }
}

@Composable
fun ScanningDialog() {
    Dialog(
        onDismissRequest = { /* Prevent dismissal */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 22.dp, end = 32.dp, top = 24.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color(0xFF1DB954),
                    trackColor = Color(0xFF1DB954).copy(alpha = 0.2f),
                    strokeWidth = 3.dp
                )

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = "Scanning Library",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Updating your tracks...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(0.1f),
                            Color.White.copy(0.03f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(0.7f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.5f)
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
fun PreviewSettingsScreen() {
    MaterialTheme {
        SettingsContent(
            isScanning = false,
            onBack = {},
            onRescan = {},
            onOpenEqualizer = {}
        )
    }
}