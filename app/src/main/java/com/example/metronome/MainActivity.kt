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
//import androidx.compose.ui.input.key.KeyEvent
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetronomeTheme {
                Surface {
                    TempoControllerApp()
                }
            }
        }
    }
}

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

// Define a data class for time signatures
data class TimeSignature(val numerator: Int, val denominator: Int) {
    override fun toString(): String = "$numerator/$denominator"
}

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
                beatSoundId = load(context, R.raw.beat, 1)
                downbeatSoundId = load(context, R.raw.downbeat, 1)
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
fun TempoControllerApp() {
    var tempo by remember { mutableStateOf(120) }
    var manualInput by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }
    var currentBeat by remember { mutableStateOf(0) }

    // Time signature implementation
    val commonTimeSignatures = listOf(
        TimeSignature(2, 4),
        TimeSignature(3, 4),
        TimeSignature(4, 4),
        TimeSignature(5, 4),
        TimeSignature(6, 8),
        TimeSignature(7, 8),
        TimeSignature(12, 8)
    )
    var selectedTimeSignature by remember { mutableStateOf(TimeSignature(4, 4)) }
    var showTimeSignatureMenu by remember { mutableStateOf(false) }

    // Animation state
    val pulseAnimation by animateFloatAsState(
        targetValue = if (isPlaying) 0.5f else 0.4f,
        animationSpec = tween(durationMillis = 100),
        label = "pulse"
    )

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val soundState = rememberSoundState()
    val isInPreview = LocalInspectionMode.current

    // Play sounds when beat changes
    LaunchedEffect(currentBeat) {
        if (isPlaying && !isInPreview) {
            soundState.playBeat(currentBeat == 1)
        }
    }

    // Clean up
    DisposableEffect(Unit) {
        onDispose {
            if (!isInPreview) {
                soundState.release()
            }
        }
    }
    // Metronome tick handler
    LaunchedEffect(isPlaying, tempo, selectedTimeSignature) {
        if (!isPlaying) {
            currentBeat = 0
            return@LaunchedEffect
        }

        while (isPlaying) {
            currentBeat = (currentBeat % selectedTimeSignature.numerator) + 1
            delay(60000L / tempo) // Convert BPM to milliseconds
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time signature selector
            Box {
                Surface(
                    modifier = Modifier
                        .clickable { showTimeSignatureMenu = true }
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
                            text = { Text(text = signature.toString(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer) },
                            onClick = {
                                selectedTimeSignature = signature
                                showTimeSignatureMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual metronome indicator
            Box(
                modifier = Modifier
                    .size(200.dp)
            )
            {
                // Define colors in Composable context
                val current_beat = Color.Black
                val beat = Color.White
                val inactiveColor = Color.White
                // Draw the beat circles
                Surface(
                    modifier = Modifier
                        .clickable { showTimeSignatureMenu = true }
                        .padding(8.dp),
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
                            val circleRadius =
                                if (isCurrentBeat) radius * pulseAnimation else radius * 0.4f

                            drawCircle(
                                color = if (isCurrentBeat) {
                                    if (i == 0) current_beat
                                    else beat
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

            // Display current tempo and beat
            Text(
                text = "$tempo BPM | Beat: ${if (isPlaying) "$currentBeat/${selectedTimeSignature.numerator}" else "--"}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Play/Pause button
            Button(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier.width(150.dp),
            ) {
                Text(if (isPlaying) "Pause" else "Play",
                    color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tempo adjustment controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center)
            {
                Button(
                    onClick = { if (tempo > 40) tempo-- },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("-1",color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { if (tempo < 220) tempo++ },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("+1",color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slider for tempo adjustment
            Slider(
                value = tempo.toFloat(),
                onValueChange = { tempo = it.toInt() },
                valueRange = 40f..220f,
                steps = 180,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Manual BPM input
            BpmInput(
                manualInput = manualInput,
                onManualInputChange = {
                    manualInput = it
                    it.toIntOrNull()?.let { value ->
                        if (value in 40..220) {
                            tempo = value
                        }
                    }
                },
                focusManager = focusManager,
                keyboardController = keyboardController
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewTempoControllerAppLight() {
    MetronomeTheme {
        Surface {
            TempoControllerApp()
        }
    }
}
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewTempoControllerAppDark() {
    MetronomeTheme {
        Surface {
            TempoControllerApp()
        }
    }
}