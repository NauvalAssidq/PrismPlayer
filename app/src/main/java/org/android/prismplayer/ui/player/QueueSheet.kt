package org.android.prismplayer.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import org.android.prismplayer.data.model.QueueItem
import org.android.prismplayer.data.model.Song
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QueueSheet(
    queue: List<QueueItem>,
    currentSong: Song,
    glowColor: Color,
    onClose: () -> Unit,
    onRemove: (Song) -> Unit,
    onMove: (Int, Int) -> Unit,
    onItemClick: (QueueItem) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Logic to separate history from "Next Up"
    val (historySize, nextUpList) = remember(queue, currentSong.id) {
        val currentIndex = queue.indexOfFirst { it.song.id == currentSong.id }
        if (currentIndex == -1) {
            Pair(0, queue)
        } else {
            val splitIndex = currentIndex + 1
            Pair(splitIndex, queue.drop(splitIndex))
        }
    }

    val lazyListState = rememberLazyListState()

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val globalFrom = from.index + historySize
        val globalTo = to.index + historySize
        onMove(globalFrom, globalTo)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // THE CONTAINER: A "System Panel" sliding up
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp) // Gap from top
            .background(Color(0xFF0F0F0F)) // Deep matte black
            .border(
                width = 1.dp,
                color = Color.White.copy(0.15f),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp) // Technical corners
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 20) {
                        onClose()
                    }
                }
            }
    ) {
        // 1. HEADER GRIP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(Color(0xFF151515))
                .clickable { onClose() }, // Click header to close
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

        // 2. TITLE BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "SEQUENCE_EDITOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "NEXT_UP_QUEUE",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "[${nextUpList.size}]",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Divider(color = Color.White.copy(0.1f))

        // 3. THE LIST
        if (nextUpList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "SEQUENCE_EMPTY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(items = nextUpList, key = { _, item -> item.queueId }) { index, item ->

                    ReorderableItem(reorderableState, key = item.queueId) { isDragging ->

                        // Interaction State
                        val scale by animateFloatAsState(if (isDragging) 1.02f else 1f, label = "scale")
                        val bgColor = if (isDragging) Color(0xFF1A1A1A) else Color.Transparent
                        val borderColor = if (isDragging) MaterialTheme.colorScheme.secondary else Color.Transparent

                        // THE ROW ITEM
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .zIndex(if (isDragging) 1f else 0f)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    shadowElevation = if (isDragging) 8.dp.toPx() else 0f
                                }
                                .background(bgColor)
                                .border(1.dp, borderColor) // Highlight border on drag
                                .clickable { onItemClick(item) }
                                .padding(horizontal = 24.dp), // Strict horizontal padding
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // 1. DRAG HANDLE (Technical)
                            Icon(
                                imageVector = Icons.Outlined.DragHandle,
                                contentDescription = "REORDER",
                                tint = if (isDragging) MaterialTheme.colorScheme.secondary else Color.White.copy(0.2f),
                                modifier = Modifier
                                    .size(24.dp)
                                    .draggableHandle() // The touch target
                            )

                            Spacer(Modifier.width(16.dp))

                            // 2. ARTWORK (Tiny Chip)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, Color.White.copy(0.2f))
                                    .background(Color(0xFF050505))
                            ) {
                                AsyncImage(
                                    model = item.song.songArtUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            // 3. METADATA
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDragging) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.song.artist.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // 4. REMOVE ACTION
                            IconButton(
                                onClick = { onRemove(item.song) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "REMOVE",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Technical Divider (only when not dragging to avoid visual glitch)
                        if (!isDragging) {
                            Divider(
                                color = Color.White.copy(0.05f),
                                modifier = Modifier.padding(start = 64.dp) // Indented divider
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun QueueSheetPreview() {
    val now = System.currentTimeMillis()
    val mockCurrentSong = Song(
        id = 1,
        title = "Midnight City",
        artist = "M83",
        albumName = "Hurry Up",
        albumId = 0,
        duration = 240000,
        path = "",
        folderName = "",
        dateAdded = 0,
        songArtUri = null,
        year = 2011,
        trackNumber = 1,
        genre = "Pop",
        dateModified = now
    )

    val mockQueue = listOf(
        QueueItem(
            queueId = "1",
            song = Song(2, "Starboy", "The Weeknd", "Starboy", 0, 200000, "", "", 0, null, 2016, 1, "Pop", now),
        ),
        QueueItem(
            queueId = "2",
            song = Song(3, "Blinding Lights", "The Weeknd", "After Hours", 0, 200000, "", "", 0, null, 2020, 1, "Pop", now),
        ),
        QueueItem(
            queueId = "3",
            song = Song(4, "Wait", "M83", "Hurry Up", 0, 200000, "", "", 0, null, 2011, 1, "Pop", now),
        )
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color.White,
            secondary = Color(0xFFD71921),
            onSurface = Color.White,
            onSurfaceVariant = Color.Gray,
            background = Color.Black
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            QueueSheet(
                queue = mockQueue,
                currentSong = mockCurrentSong,
                glowColor = Color(0xFFD71921),
                onClose = {},
                onRemove = {},
                onMove = { _, _ -> },
                onItemClick = {}
            )
        }
    }
}