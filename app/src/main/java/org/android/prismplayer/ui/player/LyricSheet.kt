package org.android.prismplayer.ui.player

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.data.model.LyricsState
import org.android.prismplayer.ui.components.MusicNoteAnimation
import org.android.prismplayer.ui.utils.LyricLine

@Composable
fun LyricSheet(
    viewModel: AudioViewModel,
    glowColor: Color,
    onClose: () -> Unit
) {
    val lyricState by viewModel.lyricState.collectAsState()
    val syncedLines by viewModel.syncedLyrics.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    LaunchedEffect(currentSong) {
        currentSong?.let { viewModel.initializeLyrics(it) }
    }

    LyricSheetContent(
        lyricState = lyricState,
        syncedLines = syncedLines,
        currentTime = currentTime,
        glowColor = glowColor,
        onClose = onClose,
        onFetchOnline = { viewModel.fetchLyricsOnline() }
    )
}

@Composable
fun LyricSheetContent(
    lyricState: LyricsState,
    syncedLines: List<LyricLine>,
    currentTime: Long,
    glowColor: Color,
    onClose: () -> Unit,
    onFetchOnline: () -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val spotlightBrush = remember(glowColor) {
        Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.45f).compositeOver(Color.Black),
                glowColor.copy(alpha = 0.25f).compositeOver(Color.Black),
                Color(0xFF050505)
            ),
            center = Offset.Unspecified,
            radius = 1000f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color(0xFF050505))
            .background(spotlightBrush)
            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 20) onClose()
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(40.dp, 4.dp).background(Color.White.copy(0.2f), CircleShape))
            }

            if (lyricState is LyricsState.Success) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CustomTabPill("Synced", selected = selectedTab == 0, activeColor = glowColor) { selectedTab = 0 }
                    Spacer(Modifier.width(12.dp))
                    CustomTabPill("Static", selected = selectedTab == 1, activeColor = glowColor) { selectedTab = 1 }
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                when (lyricState) {
                    is LyricsState.Idle -> IdleLyricView(onFetchOnline)
                    is LyricsState.Loading -> LoadingView()
                    is LyricsState.Error -> ErrorView(lyricState.message, onFetchOnline)
                    is LyricsState.Success -> {
                        if (selectedTab == 0) {
                            if (lyricState.isSynced) {
                                SyncedLyricView(syncedLines, currentTime, glowColor)
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Synced lyrics not available", color = Color.White.copy(0.5f))
                                }
                            }
                        } else {
                            StaticLyricView(lyricState.staticLyrics)
                        }
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SyncedLyricView(lines: List<LyricLine>, currentTime: Long, glowColor: Color) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentTime) {
        val index = lines.indexOfLast { it.timestamp <= currentTime }
        if (index >= 0) {
            listState.animateScrollToItem((index - 1).coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (lines.isNotEmpty() && lines.first().timestamp > 5000) {
            item {
                if (currentTime < lines.first().timestamp) {
                    MusicNoteAnimation(glowColor)
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }

        itemsIndexed(lines) { index, line ->
            if (line.content.isBlank()) return@itemsIndexed

            val isActive = line.timestamp <= currentTime &&
                    (index == lines.lastIndex || lines[index + 1].timestamp > currentTime)

            val isGap = if (index < lines.lastIndex) {
                (lines[index + 1].timestamp - line.timestamp) > 10_000
            } else false

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = line.content,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 32.sp
                    ),
                    color = if (isActive) Color.White else Color.White.copy(0.35f),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = if (isActive) 1.02f else 1f
                            scaleY = if (isActive) 1.02f else 1f
                            alpha = if (isActive) 1f else 0.35f
                            if (!isActive) {
                                renderEffect = android.graphics.RenderEffect
                                    .createBlurEffect(3f, 3f, android.graphics.Shader.TileMode.CLAMP)
                                    .asComposeRenderEffect()
                            }
                        }
                )

                if (isActive && isGap) {
                    Spacer(modifier = Modifier.height(14.dp))
                    MusicNoteAnimation(glowColor)
                }
            }
        }

        item {
            LrcLibAttribution()
        }
    }
}

@Composable
fun CustomTabPill(
    text: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val backgroundBrush = if (selected) {
        Brush.verticalGradient(
            colors = listOf(
                activeColor.copy(alpha = 0.25f),
                activeColor.copy(alpha = 0.10f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.03f)
            )
        )
    }

    val borderColor = if (selected) {
        activeColor.copy(alpha = 0.6f)
    } else {
        Color.White.copy(alpha = 0.12f)
    }

    val textColor = if (selected) activeColor else Color.White

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun StaticLyricView(text: String) {
    val stanzas = remember(text) {
        text.split("\n\n", "\r\n\r\n").filter { it.isNotBlank() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
            .padding(top = 32.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        stanzas.forEach { stanza ->
            Text(
                text = stanza.trim(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 38.sp,
                ),
                color = Color.White.copy(0.75f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        LrcLibAttribution()
    }
}

@Composable
fun IdleLyricView(onFetch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Lyrics,
            contentDescription = null,
            tint = Color.White.copy(0.2f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Sing along",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Find lyrics for this song instantly",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(0.5f)
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onFetch,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Icon(Icons.Rounded.CloudDownload, null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text("Search Online", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { /* TODO */ }) {
            Icon(Icons.Rounded.FolderOpen, null, tint = Color.White.copy(0.6f))
            Spacer(Modifier.width(8.dp))
            Text("Import Local File", color = Color.White.copy(0.6f))
        }
    }
}

@Composable
fun LrcLibAttribution() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lyrics provided by",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(0.3f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "LRCLIB.net",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White.copy(0.5f)
        )
    }
}

@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
fun ErrorView(msg: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Oops!", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(msg, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.5f))
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f))
        ) {
            Text("Try Again", color = Color.White)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewLyricSheet() {
    val dummyLyrics = listOf(
        LyricLine(16250, "I text a postcard sent to you"),
        LyricLine(20640, "Did it go through?"),
        LyricLine(24610, "Sendin' all my love to you"),
        LyricLine(32450, "You are the moonlight of my life"),
        LyricLine(36800, "Every night"),
        LyricLine(40880, "Givin' all my love to you")
    )

    val dummyTime = 34000L

    LyricSheetContent(
        lyricState = LyricsState.Success(staticLyrics = "", isSynced = true),
        syncedLines = dummyLyrics,
        currentTime = dummyTime,
        glowColor = Color(0xFF00E676),
        onClose = {},
        onFetchOnline = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewLyricSheetStatic() {
    val staticText = """
        I text a postcard sent to you
        Did it go through?
        Sendin' all my love to you

        You are the moonlight of my life
        Every night
        Givin' all my love to you
    """.trimIndent()

    LyricSheetContent(
        lyricState = LyricsState.Success(staticLyrics = staticText, isSynced = false),
        syncedLines = emptyList(),
        currentTime = 0L,
        glowColor = Color(0xFF00E676),
        onClose = {},
        onFetchOnline = {},
        initialTab = 1 // Static tab
    )
}