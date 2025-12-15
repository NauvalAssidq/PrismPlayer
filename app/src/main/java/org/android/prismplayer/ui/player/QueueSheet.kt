package org.android.prismplayer.ui.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    onOrderFinished: (List<Song>) -> Unit,
    onItemClick: (QueueItem) -> Unit
) {
    val splitLists by remember(queue, currentSong.id) {
        derivedStateOf {
            val currentIndex = queue.indexOfFirst { it.song.id == currentSong.id }
            val splitIndex = if (currentIndex == -1) 0 else currentIndex + 1
            Pair(queue.take(splitIndex), queue.drop(splitIndex))
        }
    }

    var nextUpList by remember(splitLists.second) { mutableStateOf(splitLists.second) }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        nextUpList = nextUpList.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging) {
            val fullNewList = splitLists.first + nextUpList
            if (fullNewList.map { it.queueId } != queue.map { it.queueId }) {
                onOrderFinished(fullNewList.map { it.song })
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color(0xFF121212))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 20) {
                        onClose()
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Brush.verticalGradient(listOf(glowColor.copy(0.15f), Color.Transparent)))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClose() }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(0.2f)))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Next Up", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            }

            Spacer(Modifier.height(10.dp))

            if (nextUpList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("End of playlist", color = Color.White.copy(0.3f))
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(items = nextUpList, key = { _, item -> item.queueId }) { index, item ->
                        ReorderableItem(reorderableState, key = item.queueId) { isDragging ->

                            val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
                            val scale = if (isDragging) 1.05f else 1f

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .shadow(elevation.value, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDragging) Color(0xFF252525) else Color.Transparent)
                                    .border(1.dp, if (isDragging) Color.White.copy(0.3f) else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable { onItemClick(item) }
                                    .padding(8.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DragIndicator,
                                    contentDescription = null,
                                    tint = if (isDragging) Color.White else Color.White.copy(0.2f),
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .draggableHandle()
                                )

                                AsyncImage(
                                    model = item.song.songArtUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF202020))
                                )

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.song.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onRemove(item.song)
                                        nextUpList = nextUpList.toMutableList().apply { remove(item) }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}