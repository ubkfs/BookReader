package com.example.readvault.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readvault.data.model.VocabularyWord
import com.example.readvault.ui.viewmodel.ReadVaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    viewModel: ReadVaultViewModel,
    modifier: Modifier = Modifier
) {
    val vocabulary by viewModel.vocabulary.collectAsState(initial = emptyList())
    val flashcardIndex by viewModel.flashcardIndex.collectAsState()
    val isFlipped by viewModel.isFlashcardFlipped.collectAsState()

    var isFlashcardMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMasteryFilter by remember { mutableStateOf("All") }
    var showAddWordDialog by remember { mutableStateOf(false) }

    val filteredList = vocabulary.filter { word ->
        val matchesQuery = searchQuery.isBlank() ||
                word.word.contains(searchQuery, ignoreCase = true) ||
                word.definition.contains(searchQuery, ignoreCase = true)
        val matchesMastery = selectedMasteryFilter == "All" || word.masteryLevel.equals(selectedMasteryFilter, ignoreCase = true)
        matchesQuery && matchesMastery
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (!isFlashcardMode) {
                FloatingActionButton(
                    onClick = { showAddWordDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_vocab_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Word")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Vocabulary Builder",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${vocabulary.size} words collected across your reading",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Flashcard Mode Switch
                FilledTonalButton(
                    onClick = { isFlashcardMode = !isFlashcardMode },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("flashcard_mode_toggle")
                ) {
                    Icon(
                        imageVector = if (isFlashcardMode) Icons.Default.List else Icons.Default.Style,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFlashcardMode) "List" else "Flashcards", fontSize = 12.sp)
                }
            }

            if (isFlashcardMode) {
                // Flashcards Review Mode
                if (vocabulary.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No vocabulary words saved yet. Highlight words while reading to practice.")
                    }
                } else {
                    val currentWord = vocabulary.getOrElse(flashcardIndex % vocabulary.size) { vocabulary.first() }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Card ${(flashcardIndex % vocabulary.size) + 1} of ${vocabulary.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Flashcard Box
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clickable { viewModel.flipFlashcard() }
                                .testTag("flashcard_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFlipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isFlipped) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = currentWord.word,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        if (currentWord.contextSentence.isNotBlank()) {
                                            Text(
                                                text = "\"${currentWord.contextSentence}\"",
                                                fontSize = 14.sp,
                                                fontStyle = FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Text(
                                            text = "Tap to reveal definition",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = currentWord.definition,
                                            fontSize = 18.sp,
                                            lineHeight = 26.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Status: ${currentWord.masteryLevel}") }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Card Actions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateWordMastery(currentWord.id, "Learning")
                                    viewModel.nextFlashcard(vocabulary.size)
                                }
                            ) {
                                Text("Review Again")
                            }

                            Button(
                                onClick = {
                                    viewModel.updateWordMastery(currentWord.id, "Mastered")
                                    viewModel.nextFlashcard(vocabulary.size)
                                }
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mastered")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(onClick = { viewModel.prevFlashcard(vocabulary.size) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                            }
                            IconButton(onClick = { viewModel.nextFlashcard(vocabulary.size) }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }
                }
            } else {
                // Word List Mode
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search vocabulary…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Learning", "Familiar", "Mastered").forEach { mastery ->
                        FilterChip(
                            selected = selectedMasteryFilter == mastery,
                            onClick = { selectedMasteryFilter = mastery },
                            label = { Text(mastery, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching vocabulary words found.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredList, key = { it.id }) { word ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = word.word,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        SuggestionChip(
                                            onClick = {
                                                val nextLevel = when (word.masteryLevel) {
                                                    "Learning" -> "Familiar"
                                                    "Familiar" -> "Mastered"
                                                    else -> "Learning"
                                                }
                                                viewModel.updateWordMastery(word.id, nextLevel)
                                            },
                                            label = { Text(word.masteryLevel, fontSize = 10.sp) },
                                            modifier = Modifier.height(26.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = word.definition,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 20.sp
                                    )

                                    if (word.contextSentence.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "\"${word.contextSentence}\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddWordDialog) {
        var word by remember { mutableStateOf("") }
        var definition by remember { mutableStateOf("") }
        var context by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddWordDialog = false },
            title = { Text("Add Vocabulary Word", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = word,
                        onValueChange = { word = it },
                        label = { Text("Word *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = definition,
                        onValueChange = { definition = it },
                        label = { Text("Definition *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = context,
                        onValueChange = { context = it },
                        label = { Text("Context Sentence") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (word.isNotBlank() && definition.isNotBlank()) {
                            viewModel.addVocabularyWord(word, definition, context)
                            showAddWordDialog = false
                        }
                    },
                    enabled = word.isNotBlank() && definition.isNotBlank()
                ) {
                    Text("Add Word")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWordDialog = false }) { Text("Cancel") }
            }
        )
    }
}
