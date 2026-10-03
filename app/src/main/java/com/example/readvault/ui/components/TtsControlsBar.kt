package com.example.readvault.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readvault.reader.TextToSpeechManager

@Composable
fun TtsControlsBar(
    ttsManager: TextToSpeechManager,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    val rate by ttsManager.rate.collectAsState()
    val sentenceIndex by ttsManager.currentSentenceIndex.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tts_controls_bar"),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSpeaking) "Reading Aloud (Sentence ${sentenceIndex + 1})…" else "Voice Reading Paused",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close Voice Bar", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Rate chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { r ->
                        SuggestionChip(
                            onClick = { ttsManager.setSpeechRate(r) },
                            label = { Text("${r}x", fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (rate == r) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = null,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                ttsManager.pause()
                            } else {
                                ttsManager.resume()
                            }
                        },
                        modifier = Modifier.testTag("tts_play_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (isSpeaking) "Pause" else "Resume",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = { ttsManager.stop() },
                        modifier = Modifier.testTag("tts_stop_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.StopCircle,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
