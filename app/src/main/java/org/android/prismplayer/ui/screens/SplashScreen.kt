package org.android.prismplayer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onPermissionsGranted: () -> Unit,
    onPermissionsMissing: () -> Unit
) {
    val context = LocalContext.current

    // --- ANIMATIONS ---
    val infiniteTransition = rememberInfiniteTransition(label = "prism_intro")

    // 1. Rotation: A slow, hypnotic twist for the glass stack
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "rotation"
    )

    // 2. Breathing: The glow expands and contracts
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    // 3. Loading Line: A simple progress bar moving across
    val progressWidth by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "loader"
    )

    // --- LOGIC ---
    LaunchedEffect(Unit) {
        delay(2000)

        val permissionToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val isGranted = ContextCompat.checkSelfPermission(
            context,
            permissionToCheck
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            onPermissionsGranted()
        } else {
            onPermissionsMissing()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)) // Deepest Black Base
    ) {
        // --- 1. ATMOSPHERE (Canvas for Seamless Glow) ---
        // Using Canvas prevents the "boxy" clipping because we draw directly to the screen buffer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerTop = Offset(size.width / 2, -100f) // Slightly above screen
            val centerBottomRight = Offset(size.width * 1.2f, size.height * 1.2f)

            // Top Green Flare (The Main Light)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1DB954).copy(alpha = glowAlpha), // Animated breathing alpha
                        Color.Transparent
                    ),
                    center = centerTop,
                    radius = size.width * 1.2f // Dynamic radius based on screen width
                ),
                center = centerTop,
                radius = size.width * 1.2f
            )

            // Bottom Purple Flare (Depth)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9C27B0).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = centerBottomRight,
                    radius = size.width // Dynamic radius
                ),
                center = centerBottomRight,
                radius = size.width
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- 2. THE HERO: ROTATING PRISM STACK ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Back Layer (Purple Tint)
                GlassCardCompat(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            rotationZ = rotation - 15f
                            alpha = 0.4f
                        },
                    color = Color(0xFF9C27B0)
                )

                // Middle Layer (White Tint)
                GlassCardCompat(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            rotationZ = rotation + 10f
                            alpha = 0.6f
                        },
                    color = Color.White
                )

                // Front Layer (Hero Green)
                GlassCardCompat(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer {
                            rotationZ = rotation
                            shadowElevation = 50f
                            spotShadowColor = if (Build.VERSION.SDK_INT >= 28) Color(0xFF1DB954) else Color.Black
                        },
                    color = Color(0xFF1DB954),
                    isHero = true
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // --- 3. TYPOGRAPHY ---
            Text(
                text = "PRISM",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. MINIMALIST LOADER ---
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = 0.3f)
                        .offset(x = (120 * progressWidth).dp - 40.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF1DB954),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

// --- SHARED COMPONENT ---
@Composable
fun GlassCardCompat(
    modifier: Modifier = Modifier,
    color: Color,
    isHero: Boolean = false,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF121212)) // Dark Glass Base
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(if (isHero) 0.4f else 0.1f),
                        Color.White.copy(0.05f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            color.copy(if (isHero) 0.1f else 0.03f),
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
fun PreviewSplashScreen() {
    MaterialTheme {
        SplashScreen(onPermissionsGranted = {}, onPermissionsMissing = {})
    }
}