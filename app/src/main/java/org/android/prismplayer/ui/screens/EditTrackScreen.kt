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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.android.prismplayer.data.model.Song

// --- ENTRY POINT (Logic Holder) ---
@Composable
fun EditTrackRoute(
    songId: Long,
    onBack: () -> Unit,
    viewModel: EditTrackViewModel = viewModel(factory = EditTrackViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 1. Setup Permission Launcher HERE, inside the screen
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onPermissionGranted()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Handle Events (Permissions / Success Navigation)
    LaunchedEffect(true) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EditEvent.RequestPermission -> {
                    // Launch the system dialog
                    permissionLauncher.launch(
                        IntentSenderRequest.Builder(event.intentSender).build()
                    )
                }
                is EditEvent.SaveSuccess -> {
                    Toast.makeText(context, "Saved Successfully", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            }
        }
    }

    // 3. Load Data
    LaunchedEffect(songId) {
        viewModel.loadSong(songId)
    }

    // 4. Render UI based on State
    when (val state = uiState) {
        is EditUiState.Loading -> EditLoadingState()
        is EditUiState.Error -> EditErrorState(state.message, onRetry = { viewModel.loadSong(songId) }, onBack = onBack)

        // Even if permission is requested, we stay in 'Content' state so the UI doesn't flicker/black out
        is EditUiState.Content -> {
            EditTrackScreen(
                song = state.song,
                onBack = onBack,
                onSave = { updatedSong -> viewModel.saveSong(updatedSong) }
            )
        }
    }
}

// --- UI COMPONENT (Stateless Form) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTrackScreen(
    song: Song,
    onBack: () -> Unit,
    onSave: (Song) -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.albumName) }
    var year by remember { mutableStateOf(if (song.year == 0) "" else song.year.toString()) }
    var trackNumber by remember { mutableStateOf(if (song.trackNumber == 0) "" else song.trackNumber.toString()) }
    var genre by remember { mutableStateOf(song.genre) }
    var selectedImageUri by remember { mutableStateOf<String?>(song.songArtUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri.toString()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        AuraBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Edit Metadata", fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color(0xFF1DB954)
                    ),
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null) } },
                    actions = {
                        IconButton(onClick = {
                            val artToSave = if (selectedImageUri != song.songArtUri) selectedImageUri else null

                            val updatedSong = song.copy(
                                title = title,
                                artist = artist,
                                albumName = album,
                                year = year.toIntOrNull() ?: 0,
                                genre = genre,
                                trackNumber = trackNumber.toIntOrNull() ?: 0,
                                songArtUri = artToSave
                            )
                            onSave(updatedSong)
                        }) {
                            Icon(Icons.Rounded.Check, null)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF1DB954).copy(0.3f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF252525))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                        .clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                ) {
                    if (!selectedImageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Rounded.MusicNote, null, tint = Color.White.copy(0.1f), modifier = Modifier.size(100.dp).align(Alignment.Center))
                    }
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(48.dp).clip(CircleShape).background(Color(0xFF1DB954).copy(0.9f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Edit, null, tint = Color.Black, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(Modifier.height(40.dp))

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    PrismTextField("Title", title, { title = it }, Icons.Rounded.MusicNote)
                    PrismTextField("Artist", artist, { artist = it }, Icons.Rounded.GraphicEq)
                    PrismTextField("Album", album, { album = it }, Icons.Rounded.Edit)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.weight(1f)) { PrismTextField("Year", year, { year = it }, Icons.Rounded.CalendarToday, KeyboardType.Number) }
                        Box(Modifier.weight(1f)) { PrismTextField("Track #", trackNumber, { trackNumber = it }, Icons.Rounded.Numbers, KeyboardType.Number) }
                    }
                    PrismTextField("Genre", genre, { genre = it })
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun PrismTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(0.6f),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF151515),
                unfocusedContainerColor = Color(0xFF151515),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF1DB954),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            trailingIcon = if (icon != null) {
                { Icon(icon, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(20.dp)) }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
        )
    }
}

@Composable
fun EditLoadingState() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF1DB954), strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Loading Metadata...", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.5f))
        }
    }
}

@Composable
fun EditErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Rounded.Warning, null, tint = Color(0xFFEF5350), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Failed to load song", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.5f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onBack, border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.2f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("Go Back") }
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))) { Text("Retry", color = Color.Black) }
            }
        }
    }
}

@Composable
private fun AuraBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1DB954).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(width * 0.5f, -100f),
                radius = width * 1.3f
            ),
            center = Offset(width * 0.5f, -100f),
            radius = width * 1.3f
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF050505,
    heightDp = 1000
)
@Composable
fun EditTrackScreenPreview() {
    val mockSong = Song(
        id = 1,
        title = "Midnight City",
        artist = "M83",
        albumName = "Hurry Up, We're Dreaming",
        albumId = 1,
        duration = 240000,
        path = "",
        folderName = "Music",
        dateAdded = 0L,
        songArtUri = null,
        year = 1993,
        genre = "Rock",
        trackNumber = 12
    )

    MaterialTheme {
        EditTrackScreen(
            song = mockSong,
            onBack = {},
            onSave = {}
        )
    }
}