package br.com.gzin.player

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay

private val Neon = Color(0xFF00B7FF)
private val Deep = Color(0xFF020916)
private val Panel = Color(0xFF06182D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val player = ExoPlayer.Builder(this).build()
        setContent { GzinApp(player) }
    }
    override fun onDestroy() { super.onDestroy() }
}

@Composable
private fun GzinApp(player: ExoPlayer) {
    var screen by remember { mutableStateOf("PLAYER") }
    var folderUri by remember { mutableStateOf<Uri?>(null) }
    var tracks by remember { mutableStateOf(listOf<DocumentFile>()) }
    var current by remember { mutableStateOf("SELECIONE UMA MÚSICA") }
    var playing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var master by remember { mutableFloatStateOf(0.75f) }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            folderUri = uri
            tracks = DocumentFile.fromTreeUri(context, uri)?.listFiles()?.toList() ?: emptyList()
        }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = Neon, background = Deep, surface = Panel)) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF020611), Color(0xFF031D3D), Color.Black))).padding(10.dp)) {
            Header()
            when (screen) {
                "PLAYER" -> {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MasterPanel(master, { master = it }, Modifier.weight(0.78f))
                        Column(Modifier.weight(1.55f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                NeonButton("USB", Modifier.weight(1f)) { folderPicker.launch(null) }
                                NeonButton("OFFLINE", Modifier.weight(1f)) { screen = "PLAYER" }
                            }
                            NeonCard(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📁  NAVEGAR POR PASTAS", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.weight(1f))
                                    TextButton(onClick = { folderPicker.launch(null) }) { Text("ABRIR") }
                                }
                                Text(folderUri?.lastPathSegment ?: "Selecione uma pasta do celular ou pendrive USB-OTG", color = Neon, fontSize = 11.sp)
                                LazyColumn(Modifier.weight(1f)) {
                                    items(tracks) { file ->
                                        Row(Modifier.fillMaxWidth().clickable {
                                            if (file.isDirectory) {
                                                tracks = file.listFiles().toList()
                                            } else {
                                                current = file.name ?: "Faixa"
                                                player.setMediaItem(MediaItem.fromUri(file.uri))
                                                player.prepare()
                                                player.play()
                                                playing = true
                                            }
                                        }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (file.isDirectory) "📁" else "♫", color = Neon)
                                            Spacer(Modifier.width(8.dp))
                                            Text(file.name ?: "Arquivo", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                            Text("›", color = Neon, fontSize = 20.sp)
                                        }
                                        HorizontalDivider(color = Neon.copy(alpha = .18f))
                                    }
                                }
                            }
                            NeonCard {
                                Text(current, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("GZIN ATUALIZAÇÕES • PLAYER", color = Neon, fontSize = 10.sp)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { player.seekToPreviousMediaItem() }) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White) }
                                    IconButton(onClick = { if (player.isPlaying) player.pause() else player.play(); playing = player.isPlaying }) {
                                        Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Neon, modifier = Modifier.size(38.dp))
                                    }
                                    IconButton(onClick = { player.seekToNextMediaItem() }) { Icon(Icons.Default.SkipNext, null, tint = Color.White) }
                                }
                            }
                        }
                    }
                }
                "ANALYSER" -> AnalyzerScreen(Modifier.weight(1f))
                "EQUALIZER" -> EqualizerScreen(Modifier.weight(1f))
                else -> SettingsScreen(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Nav("DYNAMIC", Icons.Default.GraphicEq, screen == "DYNAMIC", Modifier.weight(1f)) { screen = "DYNAMIC" }
                Nav("ANALYSER", Icons.Default.Equalizer, screen == "ANALYSER", Modifier.weight(1f)) { screen = "ANALYSER" }
                Nav("EQUALIZER", Icons.Default.Tune, screen == "EQUALIZER", Modifier.weight(1f)) { screen = "EQUALIZER" }
                Nav("CONFIG", Icons.Default.Settings, screen == "CONFIG", Modifier.weight(1f)) { screen = "CONFIG" }
            }
            if (screen == "DYNAMIC") DynamicScreen()
            Text("GZIN ATUALIZAÇÕES", color = Neon, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
        }
    }
}

@Composable
private fun Header() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("GZIN", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        Text("ATUALIZAÇÕES", color = Neon, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Text("O PLAYER DO SOM AUTOMOTIVO", color = Color.White, fontSize = 10.sp, letterSpacing = 2.sp)
    }
}

@Composable
private fun NeonCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().border(1.dp, Neon, RoundedCornerShape(16.dp)).background(Panel.copy(alpha = .94f), RoundedCornerShape(16.dp)).padding(10.dp), content = content)
}

@Composable
private fun NeonButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.height(45.dp).border(1.dp, Neon, RoundedCornerShape(12.dp)).background(Color.Black, RoundedCornerShape(12.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun MasterPanel(value: Float, onChange: (Float) -> Unit, modifier: Modifier) {
    NeonCard(modifier.fillMaxHeight()) {
        Text("◖  MASTER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
        Spacer(Modifier.height(16.dp))
        Text("dB", color = Neon)
        for (n in listOf("+10", "+5", "0", "-5", "-10", "-15", "-20", "-25", "-30", "-35", "-40")) {
            Text(n, color = Color.LightGray, fontSize = 10.sp)
        }
        Spacer(Modifier.weight(1f))
        Slider(value = value, onValueChange = onChange, modifier = Modifier.height(38.dp), colors = SliderDefaults.colors(thumbColor = Neon, activeTrackColor = Neon))
        Text("${((value * 40) - 40).toInt()} dB", color = Neon, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("L      R", color = Color.LightGray, fontSize = 10.sp)
    }
}

@Composable
private fun Nav(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.height(62.dp).border(1.dp, Neon, RoundedCornerShape(12.dp)).background(if (selected) Color(0xFF063B68) else Color.Black, RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = Neon, modifier = Modifier.size(21.dp))
        Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AnalyzerScreen(modifier: Modifier = Modifier) {
    var live by remember { mutableStateOf(true) }
    var phase by remember { mutableIntStateOf(0) }
    LaunchedEffect(live) { while (live) { phase++; delay(180) } }
    NeonCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ANALYSER • TEMPO REAL", color = Color.White, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Switch(live, { live = it })
        }
        Spectrum(phase, Modifier.fillMaxWidth().weight(1f))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("20Hz", color = Neon, fontSize = 10.sp); Text("100Hz", color = Neon, fontSize = 10.sp)
            Text("1KHz", color = Neon, fontSize = 10.sp); Text("10KHz", color = Neon, fontSize = 10.sp); Text("20KHz", color = Neon, fontSize = 10.sp)
        }
        Text("Picos L/R  •  RMS L/R  •  Frequência de pico", color = Color.LightGray, fontSize = 11.sp)
        Text("Visualização demonstrativa: integração do analisador de áudio do dispositivo será habilitada na etapa de áudio nativo.", color = Color.Gray, fontSize = 9.sp)
    }
}

@Composable
private fun Spectrum(phase: Int, modifier: Modifier = Modifier) {
    Canvas(modifier.background(Color(0xFF020914), RoundedCornerShape(8.dp))) {
        val count = 42
        val gap = size.width / (count * 1.5f)
        repeat(count) { i ->
            val h = (0.15f + (kotlin.math.abs(kotlin.math.sin((i + phase) * .27)) * .75f)).toFloat() * size.height
            val x = i * gap * 1.5f + gap * .2f
            val color = when { i < 10 -> Color(0xFF00AFFF); i < 30 -> Color(0xFF00E676); else -> Color(0xFFFFE600) }
            drawRoundRect(color, Offset(x, size.height - h), androidx.compose.ui.geometry.Size(gap, h), androidx.compose.ui.geometry.CornerRadius(3f))
        }
    }
}

@Composable
private fun EqualizerScreen(modifier: Modifier = Modifier) {
    val bands = listOf("32", "64", "125", "250", "500", "1K", "2K", "4K", "8K", "16K")
    val values = remember { mutableStateListOf(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f) }
    var enabled by remember { mutableStateOf(true) }
    NeonCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("EQUALIZER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Spacer(Modifier.weight(1f))
            Text("ON", color = Neon); Switch(enabled, { enabled = it })
        }
        Text("GANHO POR BANDA  •  -12 dB A +12 dB", color = Color.LightGray, fontSize = 10.sp)
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
            bands.forEachIndexed { i, band ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(band, color = Color.White, fontSize = 8.sp)
                    Slider(value = values[i], onValueChange = { values[i] = it }, valueRange = -12f..12f, modifier = Modifier.weight(1f).width(30.dp), colors = SliderDefaults.colors(thumbColor = Neon, activeTrackColor = Neon, inactiveTrackColor = Color.DarkGray))
                    Text(values[i].toInt().toString(), color = Neon, fontSize = 9.sp)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("FLAT", "BASS", "LOUD").forEach { preset -> OutlinedButton(onClick = {
                val presetValues = when (preset) { "BASS" -> listOf(6f,5f,4f,2f,0f,0f,0f,0f,0f,0f); "LOUD" -> listOf(4f,3f,1f,0f,2f,3f,2f,1f,2f,3f); else -> List(10) { 0f } }
                presetValues.forEachIndexed { i, v -> values[i] = v }
            }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(2.dp)) { Text(preset, fontSize = 9.sp) } }
        }
        Text("Ajustes visuais nesta versão inicial; aplicação ao áudio requer integração nativa por dispositivo.", color = Color.Gray, fontSize = 9.sp)
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier = Modifier) {
    NeonCard(modifier) {
        Text("CONFIGURAÇÕES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        listOf("Aparência azul neon", "Reprodução em segundo plano", "Biblioteca USB / pastas", "Sobre o GZIN ATUALIZAÇÕES").forEach {
            Text(it, color = Color.White, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp))
            HorizontalDivider(color = Neon.copy(alpha = .25f))
        }
    }
}

@Composable
private fun DynamicScreen() {
    NeonCard {
        Text("DYNAMIC", color = Color.White, fontWeight = FontWeight.Black)
        Text("Painel de efeitos dinâmicos será conectado ao sinal de áudio na integração final.", color = Color.LightGray, fontSize = 11.sp)
    }
}
