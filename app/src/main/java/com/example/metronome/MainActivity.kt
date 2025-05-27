// MainActivity.kt
package com.example.metronome
import android.content.res.Configuration
import android.os.Bundle
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
import com.example.metronome.ui.theme.MetronomeTheme

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
fun DarkThemeApp(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            surface = Color.Black,
            onSurface = Color.White,
            primary = Color(0xFFBB86FC)
        ),
        shapes = MaterialTheme.shapes, // Domyślne kształty
        typography = MaterialTheme.typography, // Domyślna typografia
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempoControllerApp() {
    var tempo by remember { mutableStateOf(120) }
    var manualInput by remember { mutableStateOf("") }

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
            // Wyświetl aktualne tempo
            Text(
                text = "Current Tempo",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )

            Text(
                text = "$tempo BPM",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 48.sp,
                modifier = Modifier.padding(vertical = 24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center)
            {
                Button(
                    onClick = { if (tempo > 40) tempo-- },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("-1")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { if (tempo < 220) tempo++ },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("+1")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Suwak do regulacji tempa
            Text(
                text = "Adjust tempo:",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = tempo.toFloat(),
                onValueChange = { tempo = it.toInt() },
                valueRange = 40f..220f,
                steps = 180,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Ręczne wprowadzanie wartości
            TextField(
                value = manualInput,
                onValueChange = {
                    manualInput = it
                    it.toIntOrNull()?.let { value ->
                        if (value in 40..220) {
                            tempo = value
                        }
                    }
                },
                label = { Text("Set exact BPM (40-220)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary
                )
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