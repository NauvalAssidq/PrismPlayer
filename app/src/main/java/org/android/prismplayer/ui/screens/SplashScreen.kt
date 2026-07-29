package org.android.prismplayer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import org.android.prismplayer.BuildConfig

@Composable
fun SplashScreen(
    onPermissionsGranted: () -> Unit,
    onPermissionsMissing: () -> Unit
) {
    val context = LocalContext.current

    // Dotted Music Note 5x7 Pattern (1 = dot ON, 0 = dot OFF)
    val logoPattern = remember {
        listOf(
            0, 0, 0, 1, 1,
            0, 0, 1, 1, 0,
            0, 0, 0, 1, 0,
            0, 0, 0, 1, 0,
            1, 1, 1, 1, 0,
            1, 1, 1, 0, 0,
            1, 1, 0, 0, 0
        )
    }

    var activeDotIndex by remember { mutableIntStateOf(0) }
    var bootStatusText by remember { mutableStateOf("INITIALIZING SYSTEM...") }
    val bootProgress = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dots")
    val dotPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        // 1. Sequentially light up the music note dots
        val onCount = logoPattern.count { it == 1 }
        for (i in 1..onCount) {
            delay(35)
            activeDotIndex = i
        }

        // 2. Run boot sequence status progress
        val stages = listOf(
            "INITIALIZING AUDIO ENGINE" to 0.25f,
            "MOUNTING LOCAL STORAGE" to 0.50f,
            "SCANNING METADATA" to 0.75f,
            "APPLYING EQ PROFILES" to 0.95f,
            "READY" to 1.0f
        )

        for ((status, target) in stages) {
            bootStatusText = status
            bootProgress.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 180, easing = LinearEasing)
            )
            delay(80)
        }

        delay(150)

        // 3. Permission Check & Transition
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            onPermissionsGranted()
        } else {
            onPermissionsMissing()
        }
    }

    val accentRed = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. CYBERPUNK CORNER BRACKETS ---
        CornerBrackets(accentColor = accentRed)

        // --- 2. CENTER LOGO & TITLE BLOCK ---
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 5x7 Dotted LED Music Note Canvas
            Canvas(modifier = Modifier.size(width = 80.dp, height = 112.dp)) {
                val cols = 5
                val rows = 7
                val cellWidth = size.width / cols
                val cellHeight = size.height / rows
                val dotRadius = Math.min(cellWidth, cellHeight) * 0.35f

                var enabledSeen = 0
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        val index = row * cols + col
                        val isOnPattern = logoPattern[index] == 1
                        val cx = col * cellWidth + cellWidth / 2f
                        val cy = row * cellHeight + cellHeight / 2f

                        if (isOnPattern) {
                            enabledSeen++
                            val isLit = enabledSeen <= activeDotIndex
                            val color = if (isLit) accentRed else Color(0xFF333333)
                            val alpha = if (isLit) dotPulseAlpha else 0.2f

                            drawCircle(
                                color = color.copy(alpha = alpha),
                                radius = dotRadius,
                                center = Offset(cx, cy)
                            )
                        } else {
                            drawCircle(
                                color = Color(0xFF222222).copy(alpha = 0.15f),
                                radius = dotRadius * 0.7f,
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "THINGSPLAYER",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "OFFLINE AUDIO ENGINE v${BuildConfig.VERSION_NAME.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                letterSpacing = 2.sp,
                fontSize = 10.sp
            )
        }

        // --- 3. BOTTOM BOOT SEQUENCE TRACK ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = bootStatusText,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = if (bootStatusText == "READY") accentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { bootProgress.value },
                modifier = Modifier
                    .width(220.dp)
                    .height(2.dp),
                color = accentRed,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }

        // Live Build Number Footer
        Text(
            text = "BUILD_${BuildConfig.VERSION_CODE}",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
        )
    }
}

@Composable
private fun CornerBrackets(accentColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 2.dp.toPx()
        val bracketSize = 16.dp.toPx()
        val margin = 24.dp.toPx()

        // Top Left Corner
        drawLine(accentColor, Offset(margin, margin), Offset(margin + bracketSize, margin), strokeWidth)
        drawLine(accentColor, Offset(margin, margin), Offset(margin, margin + bracketSize), strokeWidth)

        // Top Right Corner
        drawLine(accentColor, Offset(size.width - margin, margin), Offset(size.width - margin - bracketSize, margin), strokeWidth)
        drawLine(accentColor, Offset(size.width - margin, margin), Offset(size.width - margin, margin + bracketSize), strokeWidth)

        // Bottom Left Corner
        drawLine(accentColor, Offset(margin, size.height - margin), Offset(margin + bracketSize, size.height - margin), strokeWidth)
        drawLine(accentColor, Offset(margin, size.height - margin), Offset(margin, size.height - margin - bracketSize), strokeWidth)

        // Bottom Right Corner
        drawLine(accentColor, Offset(size.width - margin, size.height - margin), Offset(size.width - margin - bracketSize, size.height - margin), strokeWidth)
        drawLine(accentColor, Offset(size.width - margin, size.height - margin), Offset(size.width - margin, size.height - margin - bracketSize), strokeWidth)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewNewCyberpunkSplash() {
    org.android.prismplayer.ui.theme.PrismPlayerTheme {
        SplashScreen(onPermissionsGranted = {}, onPermissionsMissing = {})
    }
}