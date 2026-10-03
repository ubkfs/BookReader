package com.example.readvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readvault.data.model.ReaderFontFamily
import com.example.readvault.data.model.ReaderLayoutMode
import com.example.readvault.data.model.ReaderPreferences
import com.example.readvault.data.model.ReaderThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    preferences: ReaderPreferences,
    onThemeSelect: (ReaderThemeMode) -> Unit,
    onFontSelect: (ReaderFontFamily) -> Unit,
    onLayoutSelect: (ReaderLayoutMode) -> Unit,
    onFontSizeDelta: (Float) -> Unit,
    onLineHeightDelta: (Float) -> Unit,
    onMarginDelta: (Float) -> Unit,
    onToggleWakeLock: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Reader Appearance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Themes
            Text(text = "Theme", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThemeOptionCard(
                    title = "Light",
                    bgColor = Color(0xFFFFFFFF),
                    textColor = Color(0xFF1E293B),
                    isSelected = preferences.themeMode == ReaderThemeMode.LIGHT,
                    onClick = { onThemeSelect(ReaderThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ThemeOptionCard(
                    title = "Sepia",
                    bgColor = Color(0xFFFBF0D9),
                    textColor = Color(0xFF433422),
                    isSelected = preferences.themeMode == ReaderThemeMode.SEPIA,
                    onClick = { onThemeSelect(ReaderThemeMode.SEPIA) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ThemeOptionCard(
                    title = "Night",
                    bgColor = Color(0xFF181A20),
                    textColor = Color(0xFFE2E8F0),
                    isSelected = preferences.themeMode == ReaderThemeMode.NIGHT,
                    onClick = { onThemeSelect(ReaderThemeMode.NIGHT) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ThemeOptionCard(
                    title = "Contrast",
                    bgColor = Color(0xFF000000),
                    textColor = Color(0xFFF59E0B),
                    isSelected = preferences.themeMode == ReaderThemeMode.HIGH_CONTRAST,
                    onClick = { onThemeSelect(ReaderThemeMode.HIGH_CONTRAST) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font Family
            Text(text = "Font Style", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = preferences.fontFamily == ReaderFontFamily.SERIF,
                    onClick = { onFontSelect(ReaderFontFamily.SERIF) },
                    label = { Text("Serif Book") }
                )
                FilterChip(
                    selected = preferences.fontFamily == ReaderFontFamily.SANS,
                    onClick = { onFontSelect(ReaderFontFamily.SANS) },
                    label = { Text("Modern Sans") }
                )
                FilterChip(
                    selected = preferences.fontFamily == ReaderFontFamily.DYSLEXIC,
                    onClick = { onFontSelect(ReaderFontFamily.DYSLEXIC) },
                    label = { Text("Dyslexic") }
                )
                FilterChip(
                    selected = preferences.fontFamily == ReaderFontFamily.MONOSPACE,
                    onClick = { onFontSelect(ReaderFontFamily.MONOSPACE) },
                    label = { Text("Mono") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font Size Adjuster
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Font Size (${preferences.fontSizeSp.toInt()} sp)", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onFontSizeDelta(-1f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease font size")
                    }
                    Text(
                        text = "Aa",
                        fontWeight = FontWeight.Bold,
                        fontSize = (preferences.fontSizeSp * 0.9f).sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = { onFontSizeDelta(1f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase font size")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Line Spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Line Height (%.1fx)".format(preferences.lineHeightMultiplier), style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onLineHeightDelta(-0.1f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease line height")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onLineHeightDelta(0.1f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase line height")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Keep screen awake toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Keep Screen Awake", style = MaterialTheme.typography.labelLarge)
                    Text(text = "Screen lock stays on while reading", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = preferences.keepAwake,
                    onCheckedChange = { onToggleWakeLock() },
                    modifier = Modifier.testTag("wake_lock_switch")
                )
            }
        }
    }
}

@Composable
fun ThemeOptionCard(
    title: String,
    bgColor: Color,
    textColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}
