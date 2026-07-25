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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import org.android.prismplayer.ui.components.CustomBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import org.android.prismplayer.ui.utils.AppTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    bottomPadding: Dp,
    onReselectFolders: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val isScanning by viewModel.isScanning.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState(initial = "")

    SettingsContent(
        isScanning = isScanning,
        currentTheme = currentTheme,
        geminiApiKey = geminiApiKey,
        onBack = onBack,
        onRescan = { if (!isScanning) viewModel.rescanLibrary() },
        onThemeChanged = viewModel::setTheme,
        onOpenEqualizer = onOpenEqualizer,
        onReselectFolders = onReselectFolders,
        onSaveGeminiKey = viewModel::setGeminiApiKey,
        bottomPadding = bottomPadding
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    isScanning: Boolean,
    currentTheme: AppTheme,
    geminiApiKey: String,
    onBack: () -> Unit,
    onRescan: () -> Unit,
    onThemeChanged: (AppTheme) -> Unit,
    onOpenEqualizer: () -> Unit,
    onReselectFolders: () -> Unit,
    onSaveGeminiKey: (String) -> Unit,
    bottomPadding: Dp
) {
    var showGeminiDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
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
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = bottomPadding)
                .verticalScroll(scrollState)
        ) {
            ConfigSectionHeader("APPEARANCE")

            ThemeSelector(currentTheme, onThemeChanged)

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f), modifier = Modifier.padding(horizontal = 24.dp))

            ConfigSectionHeader("AUDIO_PROCESSING")

            ConfigItem(
                icon = Icons.Outlined.GraphicEq,
                label = "EQUALIZER",
                description = "FREQUENCY_RESPONSE_TUNING",
                onClick = onOpenEqualizer
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f), modifier = Modifier.padding(horizontal = 24.dp))

            ConfigSectionHeader("DATA_MANAGEMENT")

            ConfigItem(
                icon = Icons.Outlined.Sync,
                label = "FORCE_RESCAN",
                description = "UPDATE_INDEX_CACHE",
                onClick = onRescan
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f), modifier = Modifier.padding(horizontal = 24.dp))

            ConfigItem(
                icon = Icons.Outlined.FolderOpen,
                label = "MOUNT_PATHS",
                description = "CONFIGURE_STORAGE_ACCESS",
                onClick = onReselectFolders
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f), modifier = Modifier.padding(horizontal = 24.dp))

            ConfigSectionHeader("ARTIFICIAL_INTELLIGENCE")

            ConfigItem(
                icon = Icons.Outlined.Memory,
                label = "GEMINI_ENGINE",
                description = if (geminiApiKey.isNotBlank()) "API_KEY_CONFIGURED" else "API_KEY_MISSING",
                onClick = { showGeminiDialog = true }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PRISM v1.0.0-BETA.3 // BUILD_2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (isScanning) {
            SystemProcessDialog()
        }

        CustomBottomSheet(
            visible = showGeminiDialog,
            onDismiss = { showGeminiDialog = false }
        ) {
            GeminiKeyDialog(
                currentKey = geminiApiKey,
                onDismiss = { showGeminiDialog = false },
                onSave = { newKey ->
                    onSaveGeminiKey(newKey)
                    showGeminiDialog = false
                }
            )
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
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
                .background(MaterialTheme.colorScheme.surface) // Deep matte black
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), RoundedCornerShape(2.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Lighter header strip
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.1f))

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

@Composable
fun GeminiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentKey) }

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
                text = "SYS_CONFIG // GEMINI_API",
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
                text = "> ENTER_API_KEY:",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onSave(text.trim()) }
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
                        if (text.isEmpty()) {
                            Text(
                                text = "AIzaSy...",
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
                    text = "[ EXECUTE ]",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onSave(text.trim()) }
                        .padding(8.dp)
                )
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
            currentTheme = AppTheme.DARK,
            geminiApiKey = "",
            onBack = {},
            onRescan = {},
            onThemeChanged = {},
            onOpenEqualizer = {},
            onReselectFolders = {},
            onSaveGeminiKey = {},
            bottomPadding = 80.dp
        )
    }
}

@Composable
fun ThemeSelector(currentTheme: AppTheme, onThemeSelected: (AppTheme) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeOption(
            label = "SYSTEM",
            isSelected = currentTheme == AppTheme.SYSTEM,
            onClick = { onThemeSelected(AppTheme.SYSTEM) },
            modifier = Modifier.weight(1f)
        )
        ThemeOption(
            label = "LIGHT",
            isSelected = currentTheme == AppTheme.LIGHT,
            onClick = { onThemeSelected(AppTheme.LIGHT) },
            modifier = Modifier.weight(1f)
        )
        ThemeOption(
            label = "DARK",
            isSelected = currentTheme == AppTheme.DARK,
            onClick = { onThemeSelected(AppTheme.DARK) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .border(1.dp, if(isSelected) Color.Transparent else MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}