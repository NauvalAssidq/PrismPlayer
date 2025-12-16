package org.android.prismplayer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.ImageView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import org.android.prismplayer.R

@Composable
fun SplashScreen(
    onPermissionsGranted: () -> Unit,
    onPermissionsMissing: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "prism_intro")

    val rotation by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        delay(1000)
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) onPermissionsGranted() else onPermissionsMissing()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // Ambient glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerTop = Offset(size.width / 2, -120f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1DB954).copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = centerTop,
                    radius = size.width * 1.4f
                ),
                center = centerTop,
                radius = size.width * 1.4f
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                GlassCardCompat(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            rotationZ = rotation - 12f
                            alpha = 0.45f
                        },
                    color = Color.White
                )

                GlassCardCompat(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer {
                            rotationZ = rotation
                            shadowElevation = 60f
                            spotShadowColor =
                                if (Build.VERSION.SDK_INT >= 28)
                                    Color(0xFF1DB954)
                                else Color.Black
                        },
                    color = Color(0xFF1DB954),
                    isHero = true
                ) {
                    AndroidView(
                        modifier = Modifier.size(256.dp),
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                setImageResource(R.drawable.ic_launcher_foreground)
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = "PRISM",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Loader
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.3f)
                        .offset(x = (120 * progress).dp - 40.dp)
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