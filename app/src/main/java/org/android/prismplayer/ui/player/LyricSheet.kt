package org.android.prismplayer.ui.player

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.data.model.LyricsState
import org.android.prismplayer.ui.components.SignalWaveformAnimation
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

    // THE CONTAINER: Teleprompter Panel
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .background(Color(0xFF0F0F0F))
            .border(
                width = 1.dp,
                color = Color.White.copy(0.15f),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 20) onClose()
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. HEADER GRIP
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Color(0xFF151515))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                // "Grip Texture"
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(Color.White.copy(0.2f))
                        )
                    }
                }
            }

            Divider(color = Color.White.copy(0.1f))

            // 2. TABS (Mechanical Switch)
            if (lyricState is LyricsState.Success) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TechTabButton("SYNC_DATA", selected = selectedTab == 0) { selectedTab = 0 }
                    Spacer(Modifier.width(16.dp))
                    TechTabButton("STATIC_TXT", selected = selectedTab == 1) { selectedTab = 1 }
                }
                Divider(color = Color.White.copy(0.05f))
            }

            // 3. CONTENT AREA
            Box(modifier = Modifier.fillMaxSize()) {
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
                                    Text(
                                        "SYNC_DATA_UNAVAILABLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White.copy(0.5f)
                                    )
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
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 100.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Intro Signal
        if (lines.isNotEmpty() && lines.first().timestamp > 5000) {
            item {
                if (currentTime < lines.first().timestamp) {
                    SignalWaveformAnimation(glowColor)
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

            // Current Line Logic
            val alpha = if (isActive) 1f else 0.3f
            val scale = if (isActive) 1.05f else 1f
            val blur = if (isActive) 0.dp else 2.dp // Simulation, handled by alpha mostly

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = line.content,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 40.sp
                    ),
                    color = if (isActive) Color.White else Color.White.copy(0.4f),
                    textAlign = TextAlign.Center, // Teleprompter style
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                )

                if (isActive && isGap) {
                    Spacer(modifier = Modifier.height(24.dp))
                    SignalWaveformAnimation(glowColor)
                }
            }
        }

        item {
            TechAttribution()
        }
    }
}

@Composable
fun TechTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(0.2f)
    val textColor = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(0.5f)

    Box(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = textColor
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
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        stanzas.forEach { stanza ->
            Text(
                text = stanza.trim(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 32.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = Color.White.copy(0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        TechAttribution()
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
            imageVector = Icons.Outlined.Lyrics,
            contentDescription = null,
            tint = Color.White.copy(0.2f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(24.dp))

        Text(
            "NO_LYRIC_DATA",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(32.dp))

        // Technical Button
        Button(
            onClick = onFetch,
            modifier = Modifier
                .height(48.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Icon(Icons.Outlined.CloudDownload, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text("INITIATE_QUERY", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun TechAttribution() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SOURCE: LRCLIB.NET",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = Color.White.copy(0.3f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            strokeWidth = 2.dp,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ErrorView(msg: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "CONNECTION_FAILURE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text("RETRY", color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

// PREVIEWS
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewLyricSheet() {
    val dummyLyrics = listOf(
        LyricLine(16250, "I text a postcard sent to you"),
        LyricLine(20640, "Did it go through?"),
        LyricLine(24610, "Sendin' all my love to you")
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color.White,
            secondary = Color(0xFFD71921),
            background = Color.Black
        )
    ) {
        LyricSheetContent(
            lyricState = LyricsState.Success(staticLyrics = "", isSynced = true),
            syncedLines = dummyLyrics,
            currentTime = 22000L,
            glowColor = Color(0xFFD71921),
            onClose = {},
            onFetchOnline = {}
        )
    }
}