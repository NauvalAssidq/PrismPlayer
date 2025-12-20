package org.android.prismplayer.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

// --- THE OVERHAUL ---
// Concept: "System Bootloader"
// No more swiping pages. This is a single "Diagnostic Panel" where user activates modules.

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(onAllPermissionsGranted: () -> Unit) {

    // --- LOGIC ---
    val storagePermission = rememberPermissionState(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    // Check if system is ready (All modules online)
    val isSystemReady by remember(storagePermission.status, notificationPermission?.status) {
        derivedStateOf {
            storagePermission.status.isGranted &&
                    (notificationPermission == null || notificationPermission.status.isGranted)
        }
    }

    LaunchedEffect(isSystemReady) {
        if (isSystemReady) {
            onAllPermissionsGranted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Dotted Matrix Background (The "PCB Board" feel)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40.dp.toPx()
            for (x in 0..size.width.toInt() step step.toInt()) {
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
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
            // --- HEADER SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Brand" Tag
                Text(
                    text = "PRISM OS // v1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Live Status Indicator
                StatusBlinker(isReady = isSystemReady)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // The Massive "Boot" Title
            Text(
                text = "SYSTEM\nCHECK",
                style = MaterialTheme.typography.displayMedium, // Doto Font
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Initialize required modules to mount the file system and audio engine.",
                style = MaterialTheme.typography.bodyMedium, // Mono
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // --- THE MODULE RACK ---
            // Instead of pages, we list "Modules" that look like hardware slots.

            Text(
                text = "REQUIRED MODULES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary, // Red
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Module 1: Storage
            ModuleCard(
                codeName = "MEM_READ_01",
                description = "INTERNAL STORAGE ACCESS",
                permissionState = storagePermission,
                isRequired = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Module 2: Audio/Notifications
            if (notificationPermission != null) {
                ModuleCard(
                    codeName = "SIG_OUT_02",
                    description = "PLAYBACK CONTROLLER",
                    permissionState = notificationPermission,
                    isRequired = false // Technically optional but recommended
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- FOOTER / BOOT LOG ---
            // Just decorative technical text to sell the vibe
            Column(modifier = Modifier.fillMaxWidth()) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))
                BootLogLine("KERNEL", "INITIALIZED")
                BootLogLine("UI_LAYER", "RENDER_OK")
                BootLogLine("AUDIO_ENG", if (isSystemReady) "READY" else "WAITING...")
            }
        }
    }
}

// --- COMPONENTS ---

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ModuleCard(
    codeName: String,
    description: String,
    permissionState: PermissionState,
    isRequired: Boolean
) {
    val isGranted = permissionState.status.isGranted

    // Animate between "Wireframe" (Off) and "Solid" (On)
    val containerColor by animateColorAsState(
        targetValue = if (isGranted) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isGranted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
        label = "text"
    )
    val borderColor = if (isGranted) Color.Transparent else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(4.dp)) // Tech-sharp corners
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(enabled = !isGranted) {
                permissionState.launchPermissionRequest()
            }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = codeName,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }

            // The "Switch" or Status Indicator
            if (isGranted) {
                Text(
                    text = "[ ONLINE ]",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // Blinking "CONNECT" prompt
                val infiniteTransition = rememberInfiniteTransition(label = "blink")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "alpha"
                )
                Text(
                    text = "CONNECT >",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = alpha),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatusBlinker(isReady: Boolean) {
    val color = if (isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val infiniteTransition = rememberInfiniteTransition(label = "status")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (isReady) "SYSTEM READY" else "SYSTEM HALTED",
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

@Composable
fun BootLogLine(tag: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall, // Mono font
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
        )
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
        )
    }
}

@Preview
@Composable
fun PreviewOverhaul() {
    org.android.prismplayer.ui.theme.PrismPlayerTheme {
        PermissionScreen {}
    }
}