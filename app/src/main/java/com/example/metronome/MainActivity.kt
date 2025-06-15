// MainActivity.kt
package com.example.metronome
import android.content.Context
import android.content.res.Configuration
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.input.key.onKeyEvent
import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.metronome.ui.theme.MetronomeTheme
import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.room.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch

// Database Entity
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val artist: String,
    val timeSignatureNumerator: Int,
    val timeSignatureDenominator: Int,
    val tempo: Int
)

// Database DAO
@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY id DESC")
    suspend fun getAllSongs(): List<Song>

    @Insert
    suspend fun insertSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
}

// Database
@Database(
    entities = [Song::class],
    version = 1,
    exportSchema = false
)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: SongDatabase? = null

        fun getDatabase(context: Context): SongDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SongDatabase::class.java,
                    "song_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Repository
class SongRepository(private val songDao: SongDao) {
    suspend fun getAllSongs(): List<Song> = songDao.getAllSongs()
    suspend fun insertSong(song: Song) = songDao.insertSong(song)
    suspend fun deleteSong(song: Song) = songDao.deleteSong(song)
    suspend fun deleteAllSongs() = songDao.deleteAllSongs()
}

// ViewModel
class SongViewModel(private val repository: SongRepository) : ViewModel() {
    private val _songs = mutableStateOf<List<Song>>(emptyList())
    val songs: State<List<Song>> = _songs

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    suspend fun loadSongs() {
        _isLoading.value = true
        _songs.value = repository.getAllSongs()
        _isLoading.value = false
    }

    suspend fun addSong(song: Song) {
        repository.insertSong(song)
        loadSongs()
    }

    suspend fun deleteSong(song: Song) {
        repository.deleteSong(song)
        loadSongs()
    }

    suspend fun initializeWithSampleData() {
        _isLoading.value = true // Set loading to true
        val sampleSongs = listOf(
            Song(title = "Stairway to Heaven", artist = "Led Zeppelin", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 82),
            Song(title = "Black Dog", artist = "Led Zeppelin", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 92),
            Song(title = "Kashmir", artist = "Led Zeppelin", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 86),
            Song(title = "Come As You Are", artist = "Nirvana", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 122),
            Song(title = "Hey Jude", artist = "The Beatles", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 75),
            Song(title = "Come Together", artist = "The Beatles", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 85),
            Song(title = "A Day in the Life", artist = "The Beatles", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 84),
            Song(title = "Money", artist = "Pink Floyd", timeSignatureNumerator = 7, timeSignatureDenominator = 4, tempo = 122),
            Song(title = "Take Five", artist = "Dave Brubeck", timeSignatureNumerator = 5, timeSignatureDenominator = 4, tempo = 176),
            Song(title = "The Ocean", artist = "Led Zeppelin", timeSignatureNumerator = 7, timeSignatureDenominator = 8, tempo = 84)
        )

        sampleSongs.forEach { song ->
            repository.insertSong(song)
        }
        loadSongs()
    }
}

// ViewModelFactory
class SongViewModelFactory(private val repository: SongRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetronomeTheme {
                Surface {
                    MetronomeApp()
                }
            }
        }
    }
}

// Enum for navigation
enum class Screen {
    Metronome, Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    // Callback to notify the parent when a song is selected
    onSongSelected: (Song) -> Unit
) {
    val isInPreview = LocalInspectionMode.current

    // Don't use the ViewModel in preview, use it in the real app
    var songs: List<Song> by remember { mutableStateOf(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    if (!isInPreview) {
        val context = LocalContext.current
        val database = remember { SongDatabase.getDatabase(context) }
        val repository = remember { SongRepository(database.songDao()) }
        val viewModel: SongViewModel = viewModel(factory = SongViewModelFactory(repository))
        val coroutineScope = rememberCoroutineScope()

        // Observe ViewModel state
        songs = viewModel.songs.value
        isLoading = viewModel.isLoading.value


        // Load songs and initialize with sample data if needed
        LaunchedEffect(Unit) {
            if (viewModel.songs.value.isEmpty()) {
                val allSongs = repository.getAllSongs()
                if(allSongs.isEmpty()){
                    viewModel.initializeWithSampleData()
                } else {
                    viewModel.loadSongs()
                }
            }
        }

        // The rest of your SettingsScreen logic that uses the ViewModel
        var showAddDialog by remember { mutableStateOf(false) }
        var newTitle by remember { mutableStateOf("") }
        var newArtist by remember { mutableStateOf("") }
        var newTempo by remember { mutableStateOf("120") }
        var selectedTimeSignature by remember { mutableStateOf(TimeSignature(4, 4)) }
        var showTimeSignatureMenu by remember { mutableStateOf(false) }

        val commonTimeSignatures = listOf(
            TimeSignature(2, 4), TimeSignature(3, 4), TimeSignature(4, 4),
            TimeSignature(5, 4), TimeSignature(6, 8), TimeSignature(7, 8),
            TimeSignature(7, 4), TimeSignature(12, 8)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Song Library",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Song",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Songs list
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(songs) { song ->
                        SongCard(
                            song = song,
                            onDelete = {
                                coroutineScope.launch { viewModel.deleteSong(song) }
                            },
                            onSelect = { selectedSong ->
                                // Use the callback to pass the selected song up
                                onSongSelected(selectedSong)
                            }
                        )
                    }
                }
            }
        }
        // Add Song Dialog
        if (showAddDialog) {
            // LOCAL STATE for dialog fields (avoids interference with other UI)
            var dialogTitle by remember { mutableStateOf("") }
            var dialogArtist by remember { mutableStateOf("") }
            var dialogTempo by remember { mutableStateOf("120") }

            var selectedTimeSignature by remember { mutableStateOf(TimeSignature(4, 4)) }
            var showTimeSignatureMenu by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add New Song") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = dialogTitle,
                            onValueChange = { dialogTitle = it },
                            label = { Text("Song Title") }
                        )
                        TextField(
                            value = dialogArtist,
                            onValueChange = { dialogArtist = it },
                            label = { Text("Artist") }
                        )
                        TextField(
                            value = dialogTempo,
                            onValueChange = { dialogTempo = it },
                            label = { Text("Tempo (BPM)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Box {
                            Surface(
                                modifier = Modifier
                                    .clickable { showTimeSignatureMenu = true }
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Time Signature: ${selectedTimeSignature}",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            DropdownMenu(
                                expanded = showTimeSignatureMenu,
                                onDismissRequest = { showTimeSignatureMenu = false }
                            ) {
                                commonTimeSignatures.forEach { signature ->
                                    DropdownMenuItem(
                                        text = { Text(signature.toString()) },
                                        onClick = {
                                            selectedTimeSignature = signature
                                            showTimeSignatureMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (dialogTitle.isNotBlank() && dialogArtist.isNotBlank()) {
                                val tempo = dialogTempo.toIntOrNull()?.coerceIn(40, 220) ?: 120
                                val newSong = Song(
                                    title = dialogTitle.trim(),
                                    artist = dialogArtist.trim(),
                                    timeSignatureNumerator = selectedTimeSignature.numerator,
                                    timeSignatureDenominator = selectedTimeSignature.denominator,
                                    tempo = tempo
                                )

                                // Insert into DB before resetting
                                coroutineScope.launch {
                                    viewModel.addSong(newSong)
                                    // Close dialog only after insertion
                                    showAddDialog = false
                                }
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    } else {
        // --- PREVIEW ONLY ---
        // In preview mode, display a static list of fake songs
        val previewSongs = listOf(
            Song(1, "Take Five", "Dave Brubeck", 5, 4, 176),
            Song(2, "Money", "Pink Floyd", 7, 4, 122),
            Song(3, "Stairway to Heaven", "Led Zeppelin", 4, 4, 82)
        )
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(previewSongs) { song ->
                SongCard(song = song, onDelete = { /* No-op in preview */ }, onSelect = { /* No-op in preview */ })
            }
        }
    }
}

@Composable
fun SongCard(
    song: Song,
    onDelete: () -> Unit,
    onSelect: (Song) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(song) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = song.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = song.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "${song.timeSignatureNumerator}/${song.timeSignatureDenominator} • ${song.tempo} BPM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Song", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ... BpmInput composable remains the same ...
@Composable
fun BpmInput(
    manualInput: String,
    onManualInputChange: (String) -> Unit,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(16.dp)
    ) {
        TextField(
            value = manualInput,
            onValueChange = { onManualInputChange(it) },
            label = { Text("Set exact BPM (40–220)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (!it.isFocused) {
                        onManualInputChange("") // Clear when focus is lost
                    }
                }
                .onKeyEvent { event ->
                    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        true
                    } else {
                        false
                    }
                },
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

// ... TimeSignature data class remains the same ...
data class TimeSignature(val numerator: Int, val denominator: Int) {
    override fun toString(): String = "$numerator/$denominator"
}

// ... Sound related code remains the same ...
@Composable
fun rememberSoundState(): SoundState {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current
    val soundState = remember { SoundState() }

    LaunchedEffect(Unit) {
        if (!isInPreview) { // Only initialize in real app
            soundState.init(context)
        }
    }

    return soundState
}
@Stable
class SoundState {
    private var soundPool: SoundPool? = null
    private var beatSoundId = 0
    private var downbeatSoundId = 0
    private var loaded = false

    fun init(context: Context) {
        if (soundPool != null) return

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .build()
            .apply {
                setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) loaded = true
                }

                // Load sounds
                // NOTE: Make sure you have beat.wav and downbeat.wav in your res/raw folder
                // beatSoundId = load(context, R.raw.beat, 1)
                // downbeatSoundId = load(context, R.raw.downbeat, 1)
            }
    }

    fun playBeat(isDownbeat: Boolean) {
        if (!loaded) return
        val pool = soundPool ?: return

        val soundId = if (isDownbeat) downbeatSoundId else beatSoundId

        try {
            pool.play(soundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            // Ignore in preview
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeScreen(
    // Accept state from the parent
    tempo: Int,
    onTempoChange: (Int) -> Unit,
    selectedTimeSignature: TimeSignature,
    onTimeSignatureChange: (TimeSignature) -> Unit
) {
    var manualInput by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }
    var currentBeat by remember { mutableIntStateOf(0) }
    var showTimeSignatureMenu by remember { mutableStateOf(false) }

    val commonTimeSignatures = listOf(
        TimeSignature(2, 4), TimeSignature(3, 4), TimeSignature(4, 4),
        TimeSignature(5, 4), TimeSignature(6, 8), TimeSignature(7, 8),
        TimeSignature(12, 8)
    )
    val pulseAnimation by animateFloatAsState(
        targetValue = if (isPlaying) 0.5f else 0.4f,
        animationSpec = tween(durationMillis = 100), label = "pulse"
    )
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val soundState = rememberSoundState()
    val isInPreview = LocalInspectionMode.current

    LaunchedEffect(currentBeat) {
        if (isPlaying && !isInPreview) {
            soundState.playBeat(currentBeat == 1)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            if (!isInPreview) { soundState.release() }
        }
    }
    LaunchedEffect(isPlaying, tempo, selectedTimeSignature) {
        if (!isPlaying) {
            currentBeat = 0
            return@LaunchedEffect
        }
        while (isPlaying) {
            currentBeat = (currentBeat % selectedTimeSignature.numerator) + 1
            delay(60000L / tempo)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Surface(
                modifier = Modifier.clickable { showTimeSignatureMenu = true }.padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = selectedTimeSignature.toString(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            DropdownMenu(
                expanded = showTimeSignatureMenu,
                onDismissRequest = { showTimeSignatureMenu = false }
            ) {
                commonTimeSignatures.forEach { signature ->
                    DropdownMenuItem(
                        text = { Text(text = signature.toString(), color = MaterialTheme.colorScheme.onPrimaryContainer) },
                        onClick = {
                            onTimeSignatureChange(signature) // Use callback
                            showTimeSignatureMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.size(200.dp)) {
            val current_beat = Color.Black
            val beat = Color.White
            val inactiveColor = Color.White
            Surface(
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 4
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val angleStep = 360f / selectedTimeSignature.numerator
                    for (i in 0 until selectedTimeSignature.numerator) {
                        val angle = Math.toRadians((i * angleStep).toDouble())
                        val x = centerX + (radius * 1.5 * kotlin.math.cos(angle)).toFloat()
                        val y = centerY + (radius * 1.5 * kotlin.math.sin(angle)).toFloat()
                        val isCurrentBeat = i + 1 == currentBeat
                        val circleRadius = if (isCurrentBeat) radius * pulseAnimation else radius * 0.4f
                        drawCircle(
                            color = if (isCurrentBeat) {
                                if (i == 0) current_beat else beat
                            } else {
                                inactiveColor.copy(alpha = 0.2f)
                            },
                            radius = circleRadius,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$tempo BPM | Beat: ${if (isPlaying) "$currentBeat/${selectedTimeSignature.numerator}" else "--"}",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(onClick = { isPlaying = !isPlaying }, modifier = Modifier.width(150.dp)) {
            Text(if (isPlaying) "Pause" else "Play", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = { if (tempo > 40) onTempoChange(tempo - 1) }, modifier = Modifier.width(100.dp)) {
                Text("-1", color = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { if (tempo < 220) onTempoChange(tempo + 1) }, modifier = Modifier.width(100.dp)) {
                Text("+1", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = tempo.toFloat(),
            onValueChange = { onTempoChange(it.toInt()) }, // Use callback
            valueRange = 40f..220f,
            steps = 180,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        BpmInput(
            manualInput = manualInput,
            onManualInputChange = {
                manualInput = it
                it.toIntOrNull()?.let { value ->
                    if (value in 40..220) {
                        onTempoChange(value) // Use callback
                    }
                }
            },
            focusManager = focusManager,
            keyboardController = keyboardController
        )
    }
}


@Composable
fun MetronomeApp() {
    var currentScreen by remember { mutableStateOf(Screen.Metronome) }

    // --- STATE LIFTING ---
    // State is now "lifted" to this common parent composable
    var tempo by remember { mutableIntStateOf(120) }
    var timeSignature by remember { mutableStateOf(TimeSignature(4, 4)) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = currentScreen.ordinal,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            listOf("Metronome", "Song Library").forEachIndexed { index, title ->
                Tab(
                    selected = currentScreen.ordinal == index,
                    onClick = { currentScreen = Screen.values()[index] },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.Metronome -> MetronomeScreen(
                    // Pass state down to the MetronomeScreen
                    tempo = tempo,
                    onTempoChange = { newTempo -> tempo = newTempo },
                    selectedTimeSignature = timeSignature,
                    onTimeSignatureChange = { newTimeSignature -> timeSignature = newTimeSignature }
                )
                Screen.Settings -> SettingsScreen(
                    // Pass a callback to the SettingsScreen
                    onSongSelected = { selectedSong ->
                        // When a song is selected, update the state here
                        tempo = selectedSong.tempo
                        timeSignature = TimeSignature(
                            selectedSong.timeSignatureNumerator,
                            selectedSong.timeSignatureDenominator
                        )
                        // And navigate back to the metronome
                        currentScreen = Screen.Metronome
                    }
                )
            }
        }
    }
}

// --- NEW PREVIEW FOR SETTINGSSCREEN ---
@Preview(showBackground = true, name = "Settings Screen Preview")
@Composable
fun SettingsScreenPreview() {
    MetronomeTheme {
        Surface {
            // This will now work because our preview logic is self-contained
            SettingsScreen(onSongSelected = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTempoControllerAppLight() {
    MetronomeTheme {
        Surface {
            MetronomeApp()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewTempoControllerAppDark() {
    MetronomeTheme {
        Surface {
            MetronomeApp()
        }
    }
}