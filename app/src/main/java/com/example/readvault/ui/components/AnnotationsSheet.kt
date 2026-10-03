package com.example.readvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readvault.data.model.Bookmark
import com.example.readvault.data.model.Highlight
import com.example.readvault.data.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationsSheet(
    bookmarks: List<Bookmark>,
    highlights: List<Highlight>,
    notes: List<Note>,
    currentPage: Int,
    onJumpToPage: (Int) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDeleteHighlight: (Long) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddNote: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var quickNoteText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Annotations & Saved",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Bookmarks (${bookmarks.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Highlights (${highlights.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Notes (${notes.size})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    if (bookmarks.isEmpty()) {
                        EmptyAnnotationState("No bookmarks yet. Tap the bookmark icon while reading to save pages.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(bookmarks) { bm ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onJumpToPage(bm.pageNumber); onDismiss() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "Page ${bm.pageNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = bm.chapterTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (bm.note.isNotBlank()) {
                                                Text(text = bm.note, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                            }
                                        }
                                        IconButton(onClick = { onDeleteBookmark(bm.id) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (highlights.isEmpty()) {
                        EmptyAnnotationState("No highlights yet. Select text in the book to highlight quotes and key ideas.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(highlights) { hl ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onJumpToPage(hl.pageNumber); onDismiss() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    try {
                                                        Color(android.graphics.Color.parseColor(hl.colorHex))
                                                    } catch (_: Exception) {
                                                        Color(0xFFFDE047)
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "\"${hl.selectedText}\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Page ${hl.pageNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            if (hl.note.isNotBlank()) {
                                                Text(text = "Note: ${hl.note}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                        IconButton(onClick = { onDeleteHighlight(hl.id) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quickNoteText,
                                onValueChange = { quickNoteText = it },
                                placeholder = { Text("Write note for page $currentPage…", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f).testTag("quick_note_input"),
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (quickNoteText.isNotBlank()) {
                                        onAddNote(quickNoteText)
                                        quickNoteText = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("save_note_btn")
                            ) {
                                Text("Save")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (notes.isEmpty()) {
                            EmptyAnnotationState("No personal notes saved for this book yet.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(notes) { n ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onJumpToPage(n.pageNumber); onDismiss() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = n.noteText, fontSize = 14.sp)
                                                if (n.quoteText.isNotBlank()) {
                                                    Text(text = "Ref: \"${n.quoteText}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Text(text = "Page ${n.pageNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = { onDeleteNote(n.id) }) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EmptyAnnotationState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
