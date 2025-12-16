package org.android.prismplayer.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

data class PermissionPageContent(
    val step: Int,
    val title: String,
    val headline: String,
    val description: String,
    val buttonText: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(onAllPermissionsGranted: () -> Unit) {
    val storagePermission = rememberPermissionState(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    val currentStep by remember(storagePermission.status, notificationPermission?.status) {
        derivedStateOf {
            when {
                !storagePermission.status.isGranted -> 0 // Step 0: Storage
                notificationPermission != null && !notificationPermission.status.isGranted -> 1 // Step 1: Notifications
                else -> 2 // Step 2: Finished
            }
        }
    }

    LaunchedEffect(currentStep) {
        if (currentStep == 2) {
            onAllPermissionsGranted()
        }
    }

    val content = if (currentStep == 0) {
        PermissionPageContent(
            step = 0,
            title = "SETUP",
            headline = "Import Local\nLibrary",
            description = "Prism organizes your device storage into a beautiful, stats-driven experience.",
            buttonText = "Sync Music",
            icon = Icons.Rounded.Folder,
            color = Color(0xFF1DB954) // Green
        )
    } else {
        PermissionPageContent(
            step = 1,
            title = "ALERTS",
            headline = "Enable\nPlayback Controls",
            description = "Allow notifications to control music from your lock screen and status bar.",
            buttonText = "Allow Access",
            icon = Icons.Rounded.Notifications,
            color = Color(0xFF9C27B0)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val topColor = if (currentStep == 0) Color(0xFF1DB954) else Color(0xFF9C27B0)
            val bottomColor = if (currentStep == 0) Color(0xFF9C27B0) else Color(0xFF1DB954)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(topColor.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = width * 1.5f
                ),
                center = Offset(0f, 0f),
                radius = width * 1.5f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(bottomColor.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(width, height),
                    radius = width * 1.2f
                ),
                center = Offset(width, height),
                radius = width * 1.2f
            )
        }

        AnimatedContent(
            targetState = content,
            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(300)) },
            label = "PermissionStep"
        ) { page ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.weight(0.15f))
                Box(contentAlignment = Alignment.Center) {
                    GlassCard(
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer {
                                rotationZ = -15f
                                translationX = -40f
                            }
                            .alpha(0.4f),
                        color = Color.White
                    )

                    GlassCard(
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer {
                                rotationZ = 15f
                                translationX = 40f
                            }
                            .alpha(0.6f),
                        color = page.color
                    )

                    GlassCard(
                        modifier = Modifier
                            .size(220.dp)
                            .shadow(
                                elevation = 50.dp,
                                spotColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) page.color else Color.Black,
                                shape = RoundedCornerShape(40.dp)
                            ),
                        color = Color.White,
                        isHero = true
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            tint = page.color,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.2f))

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = page.color,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = page.headline,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.W900,
                        letterSpacing = (-1).sp,
                        lineHeight = 48.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.weight(0.2f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Button(
                        onClick = {
                            if (page.step == 0) {
                                storagePermission.launchPermissionRequest()
                            } else {
                                notificationPermission?.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .shadow(
                                elevation = 25.dp,
                                spotColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) page.color else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = page.color,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = page.buttonText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Icon(Icons.Rounded.ArrowForward, null, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    color: Color,
    isHero: Boolean = false,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFF121212))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(if (isHero) 0.3f else 0.1f),
                        Color.White.copy(0.05f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = RoundedCornerShape(40.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            color.copy(if (isHero) 0.08f else 0.02f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
        )
        content()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewPermissionScreen() {
    MaterialTheme {
        PermissionScreen(
            onAllPermissionsGranted = {},
        )
    }
}