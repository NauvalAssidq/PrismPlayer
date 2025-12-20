package org.android.prismplayer.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    bottomPadding: Dp,
    onReselectFolders: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val isScanning by viewModel.isScanning.collectAsState()

    SettingsContent(
        isScanning = isScanning,
        onBack = onBack,
        onRescan = { if (!isScanning) viewModel.rescanLibrary() },
        onOpenEqualizer = onOpenEqualizer,
        onReselectFolders = onReselectFolders,
        bottomPadding = bottomPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    isScanning: Boolean,
    onBack: () -> Unit,
    onRescan: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onReselectFolders: () -> Unit,
    bottomPadding: Dp
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "SYSTEM_CONFIG",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "RETURN",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                Divider(color = Color.White.copy(0.1f))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = bottomPadding)
        ) {
            ConfigSectionHeader("AUDIO_PROCESSING")

            ConfigItem(
                icon = Icons.Outlined.GraphicEq,
                label = "EQUALIZER",
                description = "FREQUENCY_RESPONSE_TUNING",
                onClick = onOpenEqualizer
            )

            Divider(color = Color.White.copy(0.05f), modifier = Modifier.padding(horizontal = 24.dp))

            ConfigSectionHeader("DATA_MANAGEMENT")

            ConfigItem(
                icon = Icons.Outlined.Sync,
                label = "FORCE_RESCAN",
                description = "UPDATE_INDEX_CACHE",
                onClick = onRescan
            )

            Divider(color = Color.White.copy(0.05f), modifier = Modifier.padding(horizontal = 24.dp))

            ConfigItem(
                icon = Icons.Outlined.FolderOpen,
                label = "MOUNT_PATHS",
                description = "CONFIGURE_STORAGE_ACCESS",
                onClick = onReselectFolders
            )

            Divider(color = Color.White.copy(0.1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PRISM_OS v1.0 // BUILD_2024",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (isScanning) {
            SystemProcessDialog()
        }
    }
}

@Composable
fun ConfigSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun ConfigItem(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge, // Standard font for readability
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace, // Tech font for subtext
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                fontSize = 10.sp
            )
        }

        Text(
            text = ">>",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
        )
    }
}

@Composable
fun SystemProcessDialog() {
    val infiniteTransition = rememberInfiniteTransition(label = "status_light")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha"
    )

    Dialog(
        onDismissRequest = { /* Lock */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(2.dp)) // Sharp, technical corners
                .background(Color(0xFF0F0F0F)) // Deep matte black
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), RoundedCornerShape(2.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A)) // Lighter header strip
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TASK_MANAGER // PID_99",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BUSY",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(alpha)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(0.1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Technical Label
                Text(
                    text = "> EXECUTING_INDEX_SCAN...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Updating local database references.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.secondary, // Red Accent
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "[ WAIT ]",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                    )
                    Text(
                        text = "--:--",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewSettingsScreen() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF050505),
            primary = Color.White,
            secondary = Color(0xFFD71921),
            onSurfaceVariant = Color.Gray,
            outline = Color.White.copy(0.2f)
        )
    ) {
        SettingsContent(
            isScanning = false,
            onBack = {},
            onRescan = {},
            onOpenEqualizer = {},
            onReselectFolders = {},
            bottomPadding = 80.dp
        )
    }
}