package com.example.readvault.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readvault.reader.AmbientAudioGenerator
import com.example.readvault.reader.AmbientSoundType
import com.example.readvault.reader.PomodoroTimer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSheet(
    pomodoroTimer: PomodoroTimer,
    ambientAudio: AmbientAudioGenerator,
    onDismiss: () -> Unit
) {
    val isTimerRunning by pomodoroTimer.isRunning.collectAsState()
    val remainingSec by pomodoroTimer.remainingSeconds.collectAsState()
    val totalSec by pomodoroTimer.totalSeconds.collectAsState()
    val currentAmbient by ambientAudio.currentSound.collectAsState()

    var selectedPresetMinutes by remember { mutableIntStateOf(25) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Focus Mode & Ambient Audio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Immerse yourself with timed reading and soothing sounds",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Timer Display Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pomodoroTimer.formattedTime,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isTimerRunning) "Focusing…" else "Ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preset Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPresetMinutes == 25,
                    onClick = {
                        selectedPresetMinutes = 25
                        pomodoroTimer.startTimer(25)
                    },
                    label = { Text("25m Pomodoro") }
                )
                FilterChip(
                    selected = selectedPresetMinutes == 45,
                    onClick = {
                        selectedPresetMinutes = 45
                        pomodoroTimer.startTimer(45)
                    },
                    label = { Text("45m Deep") }
                )
                FilterChip(
                    selected = selectedPresetMinutes == 60,
                    onClick = {
                        selectedPresetMinutes = 60
                        pomodoroTimer.startTimer(60)
                    },
                    label = { Text("60m Marathon") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = {
                        if (isTimerRunning) {
                            pomodoroTimer.pauseTimer()
                        } else {
                            pomodoroTimer.resumeTimer()
                        }
                    },
                    modifier = Modifier.testTag("pomodoro_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTimerRunning) "Pause" else "Start Timer")
                }

                OutlinedButton(
                    onClick = { pomodoroTimer.stopTimer() }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Ambient Audio Generator
            Text(
                text = "Ambient Focus Sound (Offline Generator)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AmbientSoundChip(
                        type = AmbientSoundType.RAIN,
                        icon = Icons.Default.WaterDrop,
                        isSelected = currentAmbient == AmbientSoundType.RAIN,
                        onClick = { ambientAudio.playSound(AmbientSoundType.RAIN) },
                        modifier = Modifier.weight(1f)
                    )
                    AmbientSoundChip(
                        type = AmbientSoundType.OCEAN,
                        icon = Icons.Default.Waves,
                        isSelected = currentAmbient == AmbientSoundType.OCEAN,
                        onClick = { ambientAudio.playSound(AmbientSoundType.OCEAN) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AmbientSoundChip(
                        type = AmbientSoundType.WHITE_NOISE,
                        icon = Icons.Default.GraphicEq,
                        isSelected = currentAmbient == AmbientSoundType.WHITE_NOISE,
                        onClick = { ambientAudio.playSound(AmbientSoundType.WHITE_NOISE) },
                        modifier = Modifier.weight(1f)
                    )
                    AmbientSoundChip(
                        type = AmbientSoundType.FOREST,
                        icon = Icons.Default.Park,
                        isSelected = currentAmbient == AmbientSoundType.FOREST,
                        onClick = { ambientAudio.playSound(AmbientSoundType.FOREST) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (currentAmbient != AmbientSoundType.OFF) {
                    Button(
                        onClick = { ambientAudio.stop() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.VolumeOff, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Turn Off Ambient Sound")
                    }
                }
            }
        }
    }
}

@Composable
fun AmbientSoundChip(
    type: AmbientSoundType,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(52.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = type.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
