package org.android.prismplayer.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.collectLatest
import org.android.prismplayer.data.model.Song

// [Previous EditTrackRoute, ViewModel integration code remains identical]
// I will focus on the UI overhaul of EditTrackScreen and components.

@Composable
fun EditTrackRoute(
    songId: Long,
    onBack: () -> Unit,
    viewModel: EditTrackViewModel = viewModel(factory = EditTrackViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onPermissionGranted()
        } else {
            Toast.makeText(context, "PERMISSION_DENIED", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(true) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EditEvent.RequestPermission -> {
                    permissionLauncher.launch(
                        IntentSenderRequest.Builder(event.intentSender).build()
                    )
                }
                is EditEvent.SaveSuccess -> {
                    Toast.makeText(context, "METADATA_UPDATED", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            }
        }
    }

    LaunchedEffect(songId) {
        viewModel.loadSong(songId)
    }

    when (val state = uiState) {
        is EditUiState.Loading -> EditLoadingState()
        is EditUiState.Error -> EditErrorState(state.message, onRetry = { viewModel.loadSong(songId) }, onBack = onBack)

        is EditUiState.Content -> {
            EditTrackScreen(
                song = state.song,
                onBack = onBack,
                onSave = { title, artist, album, year, genre, track, artUri ->
                    viewModel.onSaveClicked(
                        originalSong = state.song,
                        title = title,
                        artist = artist,
                        album = album,
                        yearInput = year,
                        genre = genre,
                        trackInput = track,
                        currentArtUri = artUri
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTrackScreen(
    song: Song,
    onBack: () -> Unit,
    onSave: (String, String, String, String, String, String, String?) -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.albumName) }
    var year by remember { mutableStateOf(if (song.year == 0) "" else song.year.toString()) }
    var trackNumber by remember { mutableStateOf(if (song.trackNumber == 0) "" else song.trackNumber.toString()) }
    var genre by remember { mutableStateOf(song.genre) }
    var selectedImageUri by remember(song.id) {
        mutableStateOf("content://media/external/audio/media/${song.id}/albumart")
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri.toString()
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "METADATA_EDITOR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.Close, "ABORT", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF0F0F0F)
                    )
                )
                Divider(color = Color.White.copy(0.1f))
            }
        },
        bottomBar = {
            // COMMAND BAR
            Column {
                Divider(color = Color.White.copy(0.1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0F0F))
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // CANCEL
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(4.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("DISCARD", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(0.7f))
                    }

                    // SAVE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            .clickable(onClick = { onSave(title, artist, album, year, genre, trackNumber, selectedImageUri) }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("WRITE_DATA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // --- ARTWORK SECTION ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .border(1.dp, Color.White.copy(0.2f))
                        .background(Color(0xFF050505))
                        .clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                ) {
                    if (selectedImageUri.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedImageUri)
                                .setParameter("last_modified", System.currentTimeMillis())
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().alpha(0.7f) // Slight dim for text visibility
                        )
                    }

                    // Edit Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary),
                    ) {
                        Text(
                            "MODIFY_SOURCE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Technical Corners
                    CornerBrackets()
                }
            }

            Divider(color = Color.White.copy(0.1f))

            // --- FORM FIELDS ---
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "CORE_METADATA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                TechTextField("TITLE_TAG", title) { title = it }
                TechTextField("ARTIST_TAG", artist) { artist = it }
                TechTextField("ALBUM_TAG", album) { album = it }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.weight(1f)) { TechTextField("YEAR_INT", year, KeyboardType.Number) { year = it } }
                    Box(Modifier.weight(1f)) { TechTextField("TRACK_IDX", trackNumber, KeyboardType.Number) { trackNumber = it } }
                }

                TechTextField("GENRE_TAG", genre) { genre = it }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun TechTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color.White.copy(0.5f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF151515))
                .border(1.dp, Color.White.copy(0.1f))
                .padding(horizontal = 12.dp, vertical = 14.dp)
        )
    }
}

@Composable
fun CornerBrackets() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val length = 10.dp.toPx()
        val stroke = 1.dp.toPx()
        val color = Color.White
        drawLine(color, Offset(0f, 0f), Offset(length, 0f), stroke)
        drawLine(color, Offset(0f, 0f), Offset(0f, length), stroke)
        drawLine(color, Offset(size.width, 0f), Offset(size.width - length, 0f), stroke)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, length), stroke)
        drawLine(color, Offset(0f, size.height), Offset(length, size.height), stroke)
        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - length), stroke)
        drawLine(color, Offset(size.width, size.height), Offset(size.width - length, size.height), stroke)
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - length), stroke)
    }
}


@Composable
fun EditLoadingState() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ACCESSING_FILE...", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun EditErrorState(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("READ_FAILURE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = Color.White.copy(0.5f))
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("RETRY_CONNECTION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun EditTrackScreenPreview() {
    val mockSong = Song(
        id = 1, title = "Midnight City", artist = "M83",
        albumName = "Hurry Up", albumId = 1, duration = 240000,
        path = "", folderName = "Music", dateAdded = 0L,
        songArtUri = null, year = 2011, genre = "Rock", trackNumber = 12
    )

    MaterialTheme {
        EditTrackScreen(
            song = mockSong,
            onBack = {},
            onSave = { _, _, _, _, _, _, _ -> }
        )
    }
}