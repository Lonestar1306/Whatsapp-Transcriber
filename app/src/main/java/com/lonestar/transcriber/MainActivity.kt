package com.lonestar.transcriber

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var sharedAudioUri: Uri? = null
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("audio/") == true) {
            sharedAudioUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
        }

        setContent {
            val context = LocalContext.current
            val isDarkTheme = isSystemInDarkTheme()
            val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val colorScheme = when {
                dynamicColor && isDarkTheme -> dynamicDarkColorScheme(context)
                dynamicColor && !isDarkTheme -> dynamicLightColorScheme(context)
                isDarkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                if (sharedAudioUri != null) {
                    // Aperto da WhatsApp
                    TranscriberBottomSheet(
                        audioUri = sharedAudioUri,
                        onDismiss = { finish() }
                    )
                } else {
                    // Aperto dall'icona nella Home (Launcher)
                    HistoryScreen(onClose = { finish() })
                }
            }
        }
    }
}

// --- HELPER STORAGE API KEY ---
fun getSavedApiKey(context: Context): String {
    val prefs = context.getSharedPreferences("TranscriberPrefs", Context.MODE_PRIVATE)
    return prefs.getString("API_KEY", "") ?: ""
}

fun saveApiKey(context: Context, apiKey: String) {
    val prefs = context.getSharedPreferences("TranscriberPrefs", Context.MODE_PRIVATE)
    prefs.edit { putString("API_KEY", apiKey) }
}

// --- HELPER CRONOLOGIA (JSON) ---
data class HistoryItem(val date: String, val text: String)

fun saveToHistory(context: Context, text: String) {
    if (text.isBlank() || text.startsWith("Errore")) return
    val prefs = context.getSharedPreferences("TranscriberPrefs", Context.MODE_PRIVATE)
    val historyStr = prefs.getString("HISTORY", "[]") ?: "[]"
    val array = JSONArray(historyStr)

    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val currentDate = formatter.format(Date())

    val newItem = JSONObject().apply {
        put("date", currentDate)
        put("text", text)
    }
    array.put(newItem)
    prefs.edit { putString("HISTORY", array.toString()) }
}

fun getHistory(context: Context): List<HistoryItem> {
    val prefs = context.getSharedPreferences("TranscriberPrefs", Context.MODE_PRIVATE)
    val historyStr = prefs.getString("HISTORY", "[]") ?: "[]"
    val array = JSONArray(historyStr)
    val list = mutableListOf<HistoryItem>()
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(HistoryItem(obj.getString("date"), obj.getString("text")))
    }
    return list.reversed() // Mostra i più recenti in alto
}

// --- HELPER AUDIO ---
suspend fun readAudioFromUri(context: Context, uri: Uri): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// --- ANIMAZIONE ONDA ---
@Composable
fun AudioWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val animations = List(4) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 400, delayMillis = index * 100, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave_anim_$index"
        )
    }
    Row(
        modifier = Modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        animations.forEach { anim ->
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight(anim.value)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            )
        }
    }
}

// --- SCHERMATA CRONOLOGIA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val historyItems = remember { getHistory(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cronologia Trascrizioni") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (historyItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nessuna trascrizione salvata.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.date,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- BOTTOM SHEET PRINCIPALE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriberBottomSheet(audioUri: Uri, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboard.current

    var transcriptionText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSummarizing by remember { mutableStateOf(false) }
    var currentApiKey by remember { mutableStateOf(getSavedApiKey(context)) }
    var isSettingsView by remember { mutableStateOf(currentApiKey.isEmpty()) }

    fun startTranscription(key: String) {
        if (key.isBlank()) return

        coroutineScope.launch {
            isLoading = true
            transcriptionText = ""

            val audioBytes = readAudioFromUri(context, audioUri)
            if (audioBytes == null) {
                transcriptionText = "Errore: Impossibile leggere il file audio."
                isLoading = false
                return@launch
            }

            val generativeModel = GenerativeModel("gemini-3.5-flash-lite", key)
            val inputContent = content {
                blob("audio/ogg", audioBytes)
                text("Trascrivi fedelmente questo audio WhatsApp in italiano, senza aggiungere commenti.")
            }

            try {
                isLoading = false
                generativeModel.generateContentStream(inputContent).collect { chunk ->
                    transcriptionText += chunk.text ?: ""
                }
                // Salva in automatico a fine streaming
                saveToHistory(context, transcriptionText)
            } catch (e: Exception) {
                transcriptionText = "Errore durante la trascrizione:\n${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (currentApiKey.isNotEmpty() && !isSettingsView) {
            startTranscription(currentApiKey)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        if (isSettingsView) {
            // === VISTA IMPOSTAZIONI ===
            var inputKey by remember { mutableStateOf(currentApiKey) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Impostazioni API",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("Inserisci API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { if (currentApiKey.isEmpty()) onDismiss() else isSettingsView = false }) {
                        Text("Annulla")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val newKey = inputKey.trim()
                        currentApiKey = newKey
                        saveApiKey(context, newKey)
                        isSettingsView = false
                        startTranscription(newKey)
                    }) {
                        Text("Salva")
                    }
                }
            }
        } else {
            // === VISTA TRASCRIZIONE ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trascrizione",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { isSettingsView = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Impostazioni", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    AudioWaveAnimation()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Analisi in corso...", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false) // Permette lo scroll se il testo è lunghissimo
                    ) {
                        Text(
                            text = transcriptionText.ifEmpty { "In attesa..." },
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Controlli
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tasto TLDR
                        OutlinedButton(
                            onClick = {
                                if (transcriptionText.isNotBlank()) {
                                    isSummarizing = true
                                    coroutineScope.launch {
                                        val model = GenerativeModel("gemini-3.5-flash-lite", currentApiKey)
                                        try {
                                            val response = model.generateContent("Riassumi questo testo in 3 punti chiave brevissimi (bullet points) in italiano:\n\n$transcriptionText")
                                            transcriptionText += "\n\n📌 TL;DR:\n${response.text}"
                                            // Aggiorna anche la cronologia sovrascrivendo l'ultimo salvataggio con la versione estesa
                                            saveToHistory(context, transcriptionText)
                                        } catch (_: Exception) { }
                                        isSummarizing = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isSummarizing && transcriptionText.isNotBlank()
                        ) {
                            Text(if (isSummarizing) "..." else "TL;DR")
                        }

                        // Tasto Copia
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    clipboardManager.setClipEntry(androidx.compose.ui.platform.ClipEntry(android.content.ClipData.newPlainText("transcription", transcriptionText)))
                                    Toast.makeText(context, "Testo copiato!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Copia")
                        }
                    }
                }
            }
        }
    }
}