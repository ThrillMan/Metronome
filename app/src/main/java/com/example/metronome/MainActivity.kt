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
import androidx.compose.animation.AnimatedVisibility
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
import java.io.File
import java.io.OutputStream
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Timer
import java.util.TimerTask
import kotlin.math.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.ExitToApp
import androidx.core.splashscreen.SplashScreen

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.os.Vibrator
import android.os.Build
import android.os.VibrationEffect

import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward

// Add this enum class to your file
enum class SortOption {
    DATE_DESC,  // Newest first
    DATE_ASC,
    TITLE_ASC,  // A-Z
    TITLE_DESC  // Z-A
}
// Database Entity
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String, // Will be Firebase UID or a guest ID
    val title: String,
    val artist: String,
    val timeSignatureNumerator: Int,
    val timeSignatureDenominator: Int,
    val tempo: Int
)

// Database DAO
@Dao
interface SongDao {
    @Query("SELECT * FROM songs WHERE userId = :userId ORDER BY id DESC")
    suspend fun getAllSongsForUser(userId: String): List<Song>

    @Insert
    suspend fun insertSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("DELETE FROM songs WHERE userId = :userId")
    suspend fun deleteAllSongsForUser(userId: String)
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
    suspend fun getAllSongsForUser(userId: String): List<Song> = songDao.getAllSongsForUser(userId)
    suspend fun insertSong(song: Song) = songDao.insertSong(song)
    suspend fun deleteSong(song: Song) = songDao.deleteSong(song)
    suspend fun deleteAllSongsForUser(userId: String) = songDao.deleteAllSongsForUser(userId)
}

// ViewModel
class SongViewModel(private val repository: SongRepository) : ViewModel() {
    private val _songs = mutableStateOf<List<Song>>(emptyList())
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var currentUserId: String = GUEST_USER_ID

    // --- UPDATED STATE FOR SORT ---
    var searchQuery by mutableStateOf("")
    // Default sort is now newest first
    var sortOption by mutableStateOf(SortOption.DATE_DESC)

    val filteredAndSortedSongs: List<Song> by derivedStateOf {
        val filtered = if (searchQuery.isBlank()) {
            _songs.value
        } else {
            _songs.value.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
            }
        }

        // --- UPDATED SORTING LOGIC ---
        when (sortOption) {
            SortOption.DATE_DESC -> filtered // Already sorted by id DESC from DAO
            SortOption.DATE_ASC -> filtered.sortedBy { it.id } // Sort by id ASC for oldest first
            SortOption.TITLE_ASC -> filtered.sortedBy { it.title }
            SortOption.TITLE_DESC -> filtered.sortedByDescending { it.title }
        }
    }

    fun setCurrentUser(userId: String?) {
        currentUserId = userId ?: GUEST_USER_ID // Use guest ID if null
        // Clear songs when user changes
        _songs.value = emptyList()
    }

    suspend fun loadSongs() {
        _isLoading.value = true
        val userSongs = repository.getAllSongsForUser(currentUserId)
        if (userSongs.isEmpty()) {
            // If the user has no songs, add sample data
            initializeWithSampleData()
        } else {
            _songs.value = userSongs
        }
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
        _isLoading.value = true
        val sampleSongs = listOf(
            Song(userId = currentUserId, title = "Stairway to Heaven", artist = "Led Zeppelin", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 82),
            Song(userId = currentUserId, title = "Black Dog", artist = "Led Zeppelin", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 92),
            Song(userId = currentUserId, title = "Kashmir", artist = "Led Zeppelin", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 86),
            Song(userId = currentUserId, title = "Come As You Are", artist = "Nirvana", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 122),
            Song(userId = currentUserId, title = "Hey Jude", artist = "The Beatles", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 75),
            Song(userId = currentUserId, title = "Come Together", artist = "The Beatles", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 85),
            Song(userId = currentUserId, title = "A Day in the Life", artist = "The Beatles", timeSignatureNumerator = 4, timeSignatureDenominator = 4, tempo = 84),
            Song(userId = currentUserId, title = "Money", artist = "Pink Floyd", timeSignatureNumerator = 7, timeSignatureDenominator = 4, tempo = 122),
            Song(userId = currentUserId, title = "Take Five", artist = "Dave Brubeck", timeSignatureNumerator = 5, timeSignatureDenominator = 4, tempo = 176),
            Song(userId = currentUserId, title = "The Ocean", artist = "Led Zeppelin", timeSignatureNumerator = 7, timeSignatureDenominator = 8, tempo = 84)
        )

        sampleSongs.forEach { song ->
            repository.insertSong(song)
        }
        //loadSongs() // Reload songs after adding samples
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

    companion object {
        const val GUEST_USER_ID = "guest_user"
    }
}

val MetronomeIcon: ImageVector = ImageVector.Builder(
    name = "Metronome",
    defaultWidth = 120.dp,
    defaultHeight = 120.dp,
    viewportWidth = 120f,
    viewportHeight = 120f
).apply {
    // Metronome base
    path(
        fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF1976D2)),
        stroke = null
    ) {
        moveTo(20f, 100f)
        lineTo(100f, 100f)
        lineTo(95f, 110f)
        lineTo(25f, 110f)
        close()
    }

    // Metronome body (trapezoid)
    path(
        fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF2196F3)),
        stroke = null
    ) {
        moveTo(30f, 100f)
        lineTo(90f, 100f)
        lineTo(70f, 20f)
        lineTo(50f, 20f)
        close()
    }

    // Metronome arm (pendulum)
    path(
        fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF0D47A1)),
        stroke = null,
        strokeLineWidth = 3f
    ) {
        moveTo(60f, 25f)
        lineTo(60f, 85f)
        moveTo(55f, 30f)
        lineTo(65f, 30f)
        lineTo(65f, 35f)
        lineTo(55f, 35f)
        close()
    }

    // Weight on pendulum
    path(
        fill = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF9800)),
        stroke = null
    ) {
        moveTo(55f, 45f)
        lineTo(65f, 45f)
        lineTo(65f, 55f)
        lineTo(55f, 55f)
        close()
    }

    // Center pivot
    path(
        fill = androidx.compose.ui.graphics.SolidColor(Color(0xFF424242)),
        stroke = null
    ) {
        moveTo(58f, 23f)
        lineTo(62f, 23f)
        lineTo(62f, 27f)
        lineTo(58f, 27f)
        close()
    }
}.build()

// Splash Screen Composable
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    // Animation values
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "logo_alpha"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            delayMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "text_alpha"
    )

    // Pendulum swing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pendulum")
    val pendulumRotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pendulum_rotation"
    )

    // Gradient background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A237E),
            Color(0xFF3F51B5),
            Color(0xFF5C6BC0)
        )
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000) // Show splash for 3 seconds
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with pendulum animation
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .rotate(if (startAnimation) pendulumRotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberVectorPainter(MetronomeIcon),
                    contentDescription = "Metronome Logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name with fade-in
            Text(
                text = "METRONOME",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Keep the Beat",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            if (startAnimation) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(textAlpha)
                )
            }
        }

        // Musical notes floating animation
        MusicNotesAnimation(
            modifier = Modifier.fillMaxSize(),
            alpha = textAlpha
        )
    }
}

@Composable
fun MusicNotesAnimation(
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "music_notes")

    // Multiple floating notes with different timings
    val notes = remember {
        listOf(
            FloatingNote(0.2f, 0.1f, 8000),
            FloatingNote(0.8f, 0.2f, 10000),
            FloatingNote(0.1f, 0.9f, 12000),
            FloatingNote(0.9f, 0.8f, 9000),
            FloatingNote(0.5f, 0.3f, 11000)
        )
    }

    Box(modifier = modifier) {
        notes.forEach { note ->
            val yOffset by infiniteTransition.animateFloat(
                initialValue = note.startY,
                targetValue = note.startY - 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = note.duration,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "note_${note.startX}"
            )

            val noteAlpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = note.duration
                        0f at 0
                        1f at (note.duration * 0.2f).toInt()
                        1f at (note.duration * 0.8f).toInt()
                        0f at note.duration
                    }
                ),
                label = "note_alpha_${note.startX}"
            )

            Text(
                text = "♪",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
                    .offset(
                        x = (note.startX * 300).dp,
                        y = (yOffset * 600).dp
                    )
                    .alpha(alpha * noteAlpha * 0.6f)
            )
        }
    }
}

data class FloatingNote(
    val startX: Float,
    val startY: Float,
    val duration: Int
)

@Composable
fun HomeScreenLogo(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    tempo: Int = 120,
    currentBeat: Int = 0,
    maxBeats: Int = 4
) {
    val infiniteTransition = rememberInfiniteTransition(label = "home_logo")

    // Pendulum swing based on tempo and play state
    val pendulumAngle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) (60000 / tempo) else 2000,
                easing = if (isPlaying) LinearEasing else EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pendulum_swing"
    )

    // Gentle breathing animation when not playing
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Beat pulse animation
    val beatPulse by animateFloatAsState(
        targetValue = if (isPlaying && currentBeat > 0) 1.2f else 1f,
        animationSpec = tween(
            durationMillis = 100,
            easing = FastOutLinearInEasing
        ),
        label = "beat_pulse"
    )

    val scale = if (isPlaying) beatPulse else breathingScale

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            drawMetronomeCompact(
                pendulumAngle = pendulumAngle,
                isPlaying = isPlaying,
                currentBeat = currentBeat,
                maxBeats = maxBeats
            )
        }
    }
}

// Minimalist metronome drawing for home screen
private fun DrawScope.drawMetronomeCompact(
    pendulumAngle: Float,
    isPlaying: Boolean,
    currentBeat: Int,
    maxBeats: Int
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val baseWidth = size.width * 0.6f
    val height = size.height * 0.8f

    // Color scheme
    val primaryColor = Color(0xFF1976D2)
    val accentColor = Color(0xFF2196F3)
    val activeColor = Color(0xFFFF5722)
    val backgroundGrad = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.1f),
            accentColor.copy(alpha = 0.05f)
        )
    )

    // Base
    drawRoundRect(
        brush = SolidColor(primaryColor),
        topLeft = Offset(centerX - baseWidth * 0.4f, centerY + height * 0.3f),
        size = Size(baseWidth * 0.8f, height * 0.1f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
    )

    // Body (simplified trapezoid)
    val bodyPath = Path().apply {
        moveTo(centerX - baseWidth * 0.25f, centerY + height * 0.3f)
        lineTo(centerX + baseWidth * 0.25f, centerY + height * 0.3f)
        lineTo(centerX + baseWidth * 0.15f, centerY - height * 0.3f)
        lineTo(centerX - baseWidth * 0.15f, centerY - height * 0.3f)
        close()
    }

    drawPath(
        path = bodyPath,
        brush = backgroundGrad
    )

    drawPath(
        path = bodyPath,
        color = accentColor,
        style = Stroke(width = 2.dp.toPx())
    )

    // Pendulum with rotation
    rotate(degrees = pendulumAngle, pivot = Offset(centerX, centerY - height * 0.25f)) {
        // Pendulum rod
        drawLine(
            color = if (isPlaying) activeColor else primaryColor,
            start = Offset(centerX, centerY - height * 0.25f),
            end = Offset(centerX, centerY + height * 0.15f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Weight
        drawCircle(
            color = if (isPlaying) activeColor else Color(0xFFFF9800),
            radius = 8.dp.toPx(),
            center = Offset(centerX, centerY - height * 0.05f)
        )

        // Highlight on weight
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = 3.dp.toPx(),
            center = Offset(centerX - 2.dp.toPx(), centerY - height * 0.05f - 2.dp.toPx())
        )
    }

    // Center pivot
    drawCircle(
        color = Color(0xFF424242),
        radius = 3.dp.toPx(),
        center = Offset(centerX, centerY - height * 0.25f)
    )
}

// Header component with logo and app title
@Composable
fun MetronomeHeader(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    tempo: Int = 120,
    currentBeat: Int = 0,
    maxBeats: Int = 4
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeScreenLogo(
            isPlaying = isPlaying,
            tempo = tempo,
            currentBeat = currentBeat,
            maxBeats = maxBeats
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "METRONOME",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            AnimatedVisibility(
                visible = isPlaying,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Text(
                    text = "♪ $tempo BPM ♪",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Floating compact logo for corner placement
@Composable
fun FloatingMetronomeLogo(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    tempo: Int = 120,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_logo")

    val pendulumAngle by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) (60000 / tempo) else 2000,
                easing = if (isPlaying) LinearEasing else EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_pendulum"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_movement"
    )

    Card(
        modifier = modifier
            .size(size)
            .offset(y = floatingOffset.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(size * 0.7f)
            ) {
                drawMetronomeIcon(
                    pendulumAngle = pendulumAngle,
                    isPlaying = isPlaying
                )
            }
        }
    }
}

// Simple icon version for floating logo
private fun DrawScope.drawMetronomeIcon(
    pendulumAngle: Float,
    isPlaying: Boolean
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val scale = 0.6f

    // Simple body
    val bodyPath = Path().apply {
        moveTo(centerX - size.width * 0.2f * scale, centerY + size.height * 0.3f * scale)
        lineTo(centerX + size.width * 0.2f * scale, centerY + size.height * 0.3f * scale)
        lineTo(centerX + size.width * 0.1f * scale, centerY - size.height * 0.3f * scale)
        lineTo(centerX - size.width * 0.1f * scale, centerY - size.height * 0.3f * scale)
        close()
    }

    drawPath(
        path = bodyPath,
        color = Color(0xFF1976D2)
    )

    // Pendulum
    rotate(
        degrees = pendulumAngle,
        pivot = Offset(centerX, centerY - size.height * 0.2f * scale)
    ) {
        drawLine(
            color = if (isPlaying) Color(0xFFFF5722) else Color(0xFF0D47A1),
            start = Offset(centerX, centerY - size.height * 0.2f * scale),
            end = Offset(centerX, centerY + size.height * 0.1f * scale),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(
            color = Color(0xFFFF9800),
            radius = 3.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}

// Enum to manage authentication status
enum class AuthStatus {
    LOADING, LOGGED_OUT, LOGGED_IN, GUEST
}



class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        setContent {
            MetronomeTheme {
                var showSplash by remember { mutableStateOf(true) }
                var authStatus by remember { mutableStateOf(AuthStatus.LOADING) }

                // Check auth status when splash is done
                LaunchedEffect(showSplash) {
                    if (!showSplash) {
                        authStatus =
                            if (auth.currentUser != null) AuthStatus.LOGGED_IN else AuthStatus.LOGGED_OUT
                    }
                }

                Surface {
                    if (showSplash) {
                        SplashScreen {
                            showSplash = false
                        }
                    } else {
                        when (authStatus) {
                            AuthStatus.LOADING -> {
                                // Show loading indicator or just wait
                                // The LaunchedEffect above will update authStatus
                            }

                            AuthStatus.LOGGED_OUT -> {
                                AuthScreen(
                                    onSignInSuccess = { authStatus = AuthStatus.LOGGED_IN },
                                    onContinueAsGuest = { authStatus = AuthStatus.GUEST }
                                )
                            }

                            AuthStatus.LOGGED_IN, AuthStatus.GUEST -> {
                                MetronomeApp(
                                    authStatus = authStatus,
                                    onSignOut = {
                                        auth.signOut()
                                        authStatus = AuthStatus.LOGGED_OUT
                                    },
                                    onLoginClicked = {
                                        // This will navigate the user back to the login screen
                                        authStatus = AuthStatus.LOGGED_OUT
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AuthScreen(onSignInSuccess: () -> Unit, onContinueAsGuest: () -> Unit) {
    val context = LocalContext.current
    val auth = Firebase.auth

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken!!, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        onSignInSuccess()
                    } else {
                        // Handle sign-in failure
                        Log.w("AuthScreen", "signInWithCredential", it.exception)
                    }
                }
        } catch (e: ApiException) {
            Log.w("AuthScreen", "Google sign in failed", e)
        }
    }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Metronome", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { launcher.launch(googleSignInClient.signInIntent) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in with Google")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue as Guest")
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
    userId: String, // Pass user ID (Firebase UID or guest ID)
    onSongSelected: (Song) -> Unit
) {
    val context = LocalContext.current
    val database = remember { SongDatabase.getDatabase(context) }
    val repository = remember { SongRepository(database.songDao()) }
    val viewModel: SongViewModel = viewModel(factory = SongViewModel.SongViewModelFactory(repository))
    val coroutineScope = rememberCoroutineScope()

    // Observe ViewModel state
    val songs = viewModel.filteredAndSortedSongs
    val isLoading by viewModel.isLoading

    // Load songs for the current user when the screen is shown or user ID changes
    LaunchedEffect(userId) {
        viewModel.setCurrentUser(userId)
        viewModel.loadSongs()
    }

    var showAddDialog by remember { mutableStateOf(false) }

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

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                label = { Text("Search by title or artist") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedTextColor = MaterialTheme.colorScheme.tertiary
                )
            )

            // --- NEW AND IMPROVED SORT BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sort by:", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary)

                // --- DATE SORT BUTTON ---
                val isDateSortActive =
                    viewModel.sortOption == SortOption.DATE_ASC || viewModel.sortOption == SortOption.DATE_DESC
                Button(
                    onClick = {
                        // If already sorting by date, toggle direction. Otherwise, set to default date sort.
                        viewModel.sortOption = if (isDateSortActive) {
                            if (viewModel.sortOption == SortOption.DATE_DESC) SortOption.DATE_ASC else SortOption.DATE_DESC
                        } else {
                            SortOption.DATE_DESC
                        }
                    },
                    colors = if (isDateSortActive) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Date")
                    // Show sort direction icon if this button is active
                    if (isDateSortActive) {
                        Icon(
                            imageVector = if (viewModel.sortOption == SortOption.DATE_DESC) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = "Sort Direction",
                            modifier = Modifier.size(18.dp).padding(start = 4.dp)
                        )
                    }
                }

                // --- TITLE SORT BUTTON ---
                val isTitleSortActive =
                    viewModel.sortOption == SortOption.TITLE_ASC || viewModel.sortOption == SortOption.TITLE_DESC
                Button(
                    onClick = {
                        // If already sorting by title, toggle direction. Otherwise, set to default title sort.
                        viewModel.sortOption = if (isTitleSortActive) {
                            if (viewModel.sortOption == SortOption.TITLE_ASC) SortOption.TITLE_DESC else SortOption.TITLE_ASC
                        } else {
                            SortOption.TITLE_ASC
                        }
                    },
                    colors = if (isTitleSortActive) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Title")
                    // Show sort direction icon if this button is active
                    if (isTitleSortActive) {
                        Icon(
                            imageVector = if (viewModel.sortOption == SortOption.TITLE_ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Sort Direction",
                            modifier = Modifier.size(18.dp).padding(start = 4.dp)
                        )
                    }
                }
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
                            onSongSelected(selectedSong)
                        }
                    )
                }
            }
        }
    }

    // Add Song Dialog
    if (showAddDialog) {
        var dialogTitle by remember { mutableStateOf("") }
        var dialogArtist by remember { mutableStateOf("") }
        var dialogTempo by remember { mutableStateOf("120") }
        var selectedTimeSignature by remember { mutableStateOf(TimeSignature(4, 4)) }
        var showTimeSignatureMenu by remember { mutableStateOf(false) }
        val commonTimeSignatures = listOf(
            TimeSignature(2, 4), TimeSignature(3, 4), TimeSignature(4, 4),
            TimeSignature(5, 4), TimeSignature(6, 8), TimeSignature(7, 8),
            TimeSignature(7, 4), TimeSignature(12, 8)
        )

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Song") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = dialogTitle,
                        onValueChange = { dialogTitle = it },
                        label = { Text("Song Title") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedTextColor = MaterialTheme.colorScheme.tertiary
                        )
                    )

                    TextField(
                        value = dialogArtist,
                        onValueChange = { dialogArtist = it },
                        label = { Text("Artist") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedTextColor = MaterialTheme.colorScheme.tertiary
                        )
                    )

                    TextField(
                        value = dialogTempo,
                        onValueChange = { dialogTempo = it },
                        label = { Text("Tempo (BPM)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedTextColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                    Box {
                        Surface(
                            modifier = Modifier.clickable { showTimeSignatureMenu = true }.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(text = "Time Signature: $selectedTimeSignature", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        DropdownMenu(expanded = showTimeSignatureMenu, onDismissRequest = { showTimeSignatureMenu = false }) {
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
                                userId = userId, // Use the provided user ID
                                title = dialogTitle.trim(),
                                artist = dialogArtist.trim(),
                                timeSignatureNumerator = selectedTimeSignature.numerator,
                                timeSignatureDenominator = selectedTimeSignature.denominator,
                                tempo = tempo
                            )
                            coroutineScope.launch {
                                viewModel.addSong(newSong)
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
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${song.timeSignatureNumerator}/${song.timeSignatureDenominator} • ${song.tempo} BPM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Song",
                    tint = MaterialTheme.colorScheme.error
                )
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

// Updated SoundState class with proper implementation
@Stable
class SoundState {
    private var soundPool: SoundPool? = null
    private var beatSoundId = 0
    private var downbeatSoundId = 0
    private var loaded = false
    private var vibrator: Vibrator? = null

    fun init(context: Context) {
        if (soundPool != null) return
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .build()
            .apply {
                setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) loaded = true
                    Log.d("SoundState", "Sound loaded with status: $status")
                }

                // Load actual sound files - you need these in res/raw/
                try {
                    beatSoundId = load(context, R.raw.beat, 1)
                    downbeatSoundId = load(context, R.raw.downbeat, 1)
                    Log.d(
                        "SoundState",
                        "Sound files loaded - beat: $beatSoundId, downbeat: $downbeatSoundId"
                    )
                } catch (e: Exception) {
                    Log.e("SoundState", "Failed to load sound files", e)
                    // Fallback: create synthetic sounds
                    createSyntheticSounds(context)
                }
            }
    }

    private fun createSyntheticSounds(context: Context) {
        // Create temporary WAV files for beat sounds
        try {
            val beatFile = createTempSoundFile(context, "beat", 800) // 800Hz tone
            val downbeatFile = createTempSoundFile(context, "downbeat", 1200) // 1200Hz tone

            beatSoundId = soundPool?.load(beatFile.absolutePath, 1) ?: 0
            downbeatSoundId = soundPool?.load(downbeatFile.absolutePath, 1) ?: 0

            Log.d("SoundState", "Synthetic sounds created")
        } catch (e: Exception) {
            Log.e("SoundState", "Failed to create synthetic sounds", e)
        }
    }

    private fun createTempSoundFile(context: Context, name: String, frequency: Int): File {
        val file = File(context.cacheDir, "$name.wav")
        val sampleRate = 44100
        val duration = 0.1 // 100ms
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)

        // Generate sine wave
        for (i in 0 until numSamples) {
            val sample =
                (Math.sin(2 * Math.PI * i * frequency / sampleRate) * Short.MAX_VALUE * 0.5).toInt()
            samples[i] = sample.toShort()
        }

        // Write WAV file
        file.outputStream().use { output ->
            writeWavHeader(output, numSamples, sampleRate)
            val buffer = ByteArray(numSamples * 2)
            for (i in samples.indices) {
                buffer[i * 2] = (samples[i].toInt() and 0xff).toByte()
                buffer[i * 2 + 1] = ((samples[i].toInt() shr 8) and 0xff).toByte()
            }
            output.write(buffer)
        }

        return file
    }

    private fun writeWavHeader(output: OutputStream, numSamples: Int, sampleRate: Int) {
        val dataSize = numSamples * 2
        val fileSize = dataSize + 36

        output.write("RIFF".toByteArray())
        output.write(intToByteArray(fileSize))
        output.write("WAVE".toByteArray())
        output.write("fmt ".toByteArray())
        output.write(intToByteArray(16)) // fmt chunk size
        output.write(shortToByteArray(1)) // audio format (PCM)
        output.write(shortToByteArray(1)) // num channels
        output.write(intToByteArray(sampleRate))
        output.write(intToByteArray(sampleRate * 2)) // byte rate
        output.write(shortToByteArray(2)) // block align
        output.write(shortToByteArray(16)) // bits per sample
        output.write("data".toByteArray())
        output.write(intToByteArray(dataSize))
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xff).toByte(),
            ((value shr 8) and 0xff).toByte(),
            ((value shr 16) and 0xff).toByte(),
            ((value shr 24) and 0xff).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xff).toByte(),
            ((value.toInt() shr 8) and 0xff).toByte()
        )
    }

    fun playBeat(isDownbeat: Boolean) {
        val pool = soundPool
        if (pool == null || !loaded) {
            Log.w("SoundState", "Sound not ready - pool: $pool, loaded: $loaded")
            return
        }

        // Trigger vibration on downbeat
        if (isDownbeat) {
            Log.d("VIBRATION", "Vibration triggered!!")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                // deprecated in API 26
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        }

        val soundId = if (isDownbeat) downbeatSoundId else beatSoundId

        if (soundId == 0) {
            Log.w("SoundState", "Sound ID is 0 - not loaded properly")
            return
        }

        try {
            val result = pool.play(soundId, 1f, 1f, 1, 0, 1f)
            Log.d(
                "SoundState",
                "Played sound - ID: $soundId, result: $result, isDownbeat: $isDownbeat"
            )
        } catch (e: Exception) {
            Log.e("SoundState", "Failed to play sound", e)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded = false
        beatSoundId = 0
        downbeatSoundId = 0
        vibrator = null // Release the vibrator
        Log.d("SoundState", "Sound resources released")
    }
}

// Alternative: Simple ToneGenerator-based implementation
@Stable
class SimpleSoundState {
    private var toneGenerator: android.media.ToneGenerator? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return

        try {
            toneGenerator = android.media.ToneGenerator(
                android.media.AudioManager.STREAM_MUSIC,
                80 // Volume (0-100)
            )
            initialized = true
            Log.d("SimpleSoundState", "ToneGenerator initialized")
        } catch (e: Exception) {
            Log.e("SimpleSoundState", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playBeat(isDownbeat: Boolean) {
        if (!initialized) {
            Log.w("SimpleSoundState", "Not initialized")
            return
        }

        try {
            val tone = if (isDownbeat) {
                android.media.ToneGenerator.TONE_PROP_BEEP2 // Higher pitch for downbeat
            } else {
                android.media.ToneGenerator.TONE_PROP_BEEP // Regular beat
            }

            toneGenerator?.startTone(tone, 100) // 100ms duration
            Log.d("SimpleSoundState", "Played tone - isDownbeat: $isDownbeat")
        } catch (e: Exception) {
            Log.e("SimpleSoundState", "Failed to play tone", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
        initialized = false
        Log.d("SimpleSoundState", "ToneGenerator released")
    }
}

// Updated rememberSoundState composable
@Composable
fun rememberSoundState(): SoundState {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current
    val soundState = remember { SoundState() }

    LaunchedEffect(Unit) {
        if (!isInPreview) {
            soundState.init(context)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!isInPreview) {
                soundState.release()
            }
        }
    }

    return soundState
}

// Alternative simple sound composable
@Composable
fun rememberSimpleSoundState(): SimpleSoundState {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current
    val soundState = remember { SimpleSoundState() }

    LaunchedEffect(Unit) {
        if (!isInPreview) {
            soundState.init(context)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!isInPreview) {
                soundState.release()
            }
        }
    }

    return soundState
}

private fun DrawScope.drawMetronomeDots(
    isPlaying: Boolean,
    currentBeat: Int,
    maxBeats: Int
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val baseWidth = size.width * 0.6f
    val height = size.height * 0.8f

    // Color scheme
    val primaryColor = Color(0xFF1976D2)
    val accentColor = Color(0xFF2196F3)
    val activeColor = Color(0xFFFF5722)
    val backgroundGrad = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.1f),
            accentColor.copy(alpha = 0.05f)
        )
    )


    // Beat indicators (small dots around the logo)
    if (isPlaying) {
        val radius = size.width * 0.45f
        for (i in 1..maxBeats) {
            val angle = (i - 1) * (360f / maxBeats) - 90f
            val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * radius

            drawCircle(
                color = if (i == currentBeat) activeColor else primaryColor.copy(alpha = 0.3f),
                radius = if (i == currentBeat) 10.dp.toPx() else 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    } else {
        val radius = size.width * 0.45f
        for (i in 1..maxBeats) {
            val angle = (i - 1) * (360f / maxBeats) - 90f
            val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * radius

            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeScreen(
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
            if (!isInPreview) {
                soundState.release()
            }
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
        modifier = Modifier.fillMaxSize()
            .padding(start = 32.dp, end = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with animated logo
        MetronomeHeader(
            isPlaying = isPlaying,
            tempo = tempo,
            currentBeat = currentBeat,
            maxBeats = selectedTimeSignature.numerator
        )
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val canvasWidth = screenWidth * 0.8f
        Canvas(
            modifier = Modifier.size(canvasWidth)
        ) {
            drawMetronomeDots(
                isPlaying = isPlaying,
                currentBeat = currentBeat,
                maxBeats = selectedTimeSignature.numerator
            )
        }

        // Rest of the metronome interface
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, end = 32.dp)
                .verticalScroll(
                    state = rememberScrollState(),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    enabled = true
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(modifier = Modifier.height(16.dp))
            Box {
                Surface(
                    modifier = Modifier.clickable { showTimeSignatureMenu = true }
                        .padding(8.dp),
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
                            text = {
                                Text(
                                    text = signature.toString(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            onClick = {
                                onTimeSignatureChange(signature)
                                showTimeSignatureMenu = false
                            }
                        )
                    }
                }
            }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { if (tempo > 40) onTempoChange(tempo - 1) },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("-1", color = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { if (tempo < 220) onTempoChange(tempo + 1) },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("+1", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = tempo.toFloat(),
                onValueChange = { onTempoChange(it.toInt()) },
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
                            onTempoChange(value)
                        }
                    }
                },
                focusManager = focusManager,
                keyboardController = keyboardController
            )
        }
    }
}

@Composable
fun MetronomeApp(
    authStatus: AuthStatus,
    onSignOut: () -> Unit,
    onLoginClicked: () -> Unit
) {
    val auth = Firebase.auth
    val user = auth.currentUser
    var currentScreen by remember { mutableStateOf(Screen.Metronome) }

    // State is "lifted" to this common parent composable
    var tempo by remember { mutableIntStateOf(120) }
    var timeSignature by remember { mutableStateOf(TimeSignature(4, 4)) }

    // Determine the user ID to use for the database
    val userId = if (authStatus == AuthStatus.LOGGED_IN) user?.uid else SongViewModel.GUEST_USER_ID

    Column(modifier = Modifier.fillMaxSize()) {
        // Show user info and logout button only if logged in
        if (authStatus == AuthStatus.LOGGED_IN && user != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = user.email ?: "Logged In",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onSignOut) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        else if(authStatus == AuthStatus.GUEST){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Guest Mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // New Login button for guests
                TextButton(onClick = onLoginClicked) {
                    Text("Login")
                }
            }
        }

        TabRow(
            selectedTabIndex = currentScreen.ordinal,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            listOf("Metronome", "Song Library").forEachIndexed { index, title ->
                Tab(
                    selected = currentScreen.ordinal == index,
                    onClick = { currentScreen = Screen.values()[index] },
                    text = { Text(title, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.statusBarsPadding().padding(top = 8.dp)
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentScreen) {
                Screen.Metronome -> MetronomeScreen(
                    tempo = tempo,
                    onTempoChange = { newTempo -> tempo = newTempo },
                    selectedTimeSignature = timeSignature,
                    onTimeSignatureChange = { newTimeSignature -> timeSignature = newTimeSignature }
                )
                Screen.Settings -> SettingsScreen(
                    userId = userId ?: SongViewModel.GUEST_USER_ID, // Ensure userId is not null
                    onSongSelected = { selectedSong ->
                        tempo = selectedSong.tempo
                        timeSignature = TimeSignature(
                            selectedSong.timeSignatureNumerator,
                            selectedSong.timeSignatureDenominator
                        )
                        currentScreen = Screen.Metronome
                    }
                )
            }
        }
    }
}