package com.example.readvault.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readvault.data.model.*
import com.example.readvault.ui.components.AnnotationsSheet
import com.example.readvault.ui.components.FocusSheet
import com.example.readvault.ui.components.ReaderSettingsSheet
import com.example.readvault.ui.components.TtsControlsBar
import com.example.readvault.ui.viewmodel.ReadVaultViewModel
import com.example.readvault.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReadVaultViewModel,
    modifier: Modifier = Modifier
) {
    val book by viewModel.currentBook.collectAsState()
    val chapterIndex by viewModel.currentChapterIndex.collectAsState()
    val prefs by viewModel.readerPreferences.collectAsState()
    val bookmarks by viewModel.bookBookmarks.collectAsState()
    val highlights by viewModel.bookHighlights.collectAsState()
    val notes by viewModel.bookNotes.collectAsState()
    val isTtsSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showAnnotationsSheet by remember { mutableStateOf(false) }
    var showFocusSheet by remember { mutableStateOf(false) }
    var showTocDialog by remember { mutableStateOf(false) }
    var showVocabDialog by remember { mutableStateOf(false) }
    var selectedWordForVocab by remember { mutableStateOf("") }
    var showTtsBar by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    // Screen Wake Lock
    DisposableEffect(prefs.keepAwake) {
        if (prefs.keepAwake) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BackHandler {
        viewModel.ttsManager.stop()
        viewModel.navigateTo(Screen.Library)
    }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentBook = book!!
    val currentChapter = currentBook.chapters.getOrNull(chapterIndex)
    val scrollState = rememberScrollState()

    // Determine Theme Colors
    val (themeBg, themeText) = when (prefs.themeMode) {
        ReaderThemeMode.LIGHT -> Color(0xFFFFFFFF) to Color(0xFF1E293B)
        ReaderThemeMode.SEPIA -> Color(0xFFFBF0D9) to Color(0xFF433422)
        ReaderThemeMode.NIGHT -> Color(0xFF181A20) to Color(0xFFE2E8F0)
        ReaderThemeMode.HIGH_CONTRAST -> Color(0xFF000000) to Color(0xFFF59E0B)
    }

    // Determine Font Family
    val readerFontFamily = when (prefs.fontFamily) {
        ReaderFontFamily.SERIF -> FontFamily.Serif
        ReaderFontFamily.SANS -> FontFamily.SansSerif
        ReaderFontFamily.MONOSPACE -> FontFamily.Monospace
        ReaderFontFamily.DYSLEXIC -> FontFamily.Cursive
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Toolbar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentBook.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeText,
                            maxLines = 1
                        )
                        Text(
                            text = currentChapter?.title ?: "Chapter ${chapterIndex + 1}",
                            fontSize = 11.sp,
                            color = themeText.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.ttsManager.stop()
                            viewModel.navigateTo(Screen.Library)
                        },
                        modifier = Modifier.testTag("reader_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = themeText
                        )
                    }
                },
                actions = {
                    // Quick Bookmark
                    IconButton(
                        onClick = { viewModel.addBookmark() },
                        modifier = Modifier.testTag("reader_quick_bookmark_btn")
                    ) {
                        val isBookmarked = bookmarks.any { it.pageNumber == currentBook.currentPage }
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else themeText
                        )
                    }

                    // TTS Voice
                    IconButton(
                        onClick = {
                            showTtsBar = !showTtsBar
                            if (showTtsBar && !isTtsSpeaking) {
                                currentChapter?.content?.let { text ->
                                    viewModel.ttsManager.speakText(text)
                                }
                            }
                        },
                        modifier = Modifier.testTag("reader_voice_btn")
                    ) {
                        Icon(
                            imageVector = if (isTtsSpeaking) Icons.Default.VolumeUp else Icons.Default.Headphones,
                            contentDescription = "Voice Reader",
                            tint = if (isTtsSpeaking) MaterialTheme.colorScheme.primary else themeText
                        )
                    }

                    // Focus & Pomodoro
                    IconButton(
                        onClick = { showFocusSheet = true },
                        modifier = Modifier.testTag("reader_focus_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Focus Mode",
                            tint = themeText
                        )
                    }

                    // Annotations
                    IconButton(
                        onClick = { showAnnotationsSheet = true },
                        modifier = Modifier.testTag("reader_annotations_btn")
                    ) {
                        BadgedBox(badge = {
                            val totalCount = bookmarks.size + highlights.size + notes.size
                            if (totalCount > 0) {
                                Badge { Text("$totalCount") }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Annotations",
                                tint = themeText
                            )
                        }
                    }

                    // Typography Settings
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("reader_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Text Settings",
                            tint = themeText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeBg.copy(alpha = 0.95f),
                    titleContentColor = themeText
                )
            )

            // Chapter Text Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = prefs.horizontalMarginDp.dp, vertical = 16.dp)
                    ) {
                        if (currentChapter != null) {
                            Text(
                                text = currentChapter.title,
                                fontFamily = readerFontFamily,
                                fontSize = (prefs.fontSizeSp + 6).sp,
                                fontWeight = FontWeight.Bold,
                                color = themeText,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            val paragraphs = currentChapter.content.split("\n\n").filter { it.isNotBlank() }
                            paragraphs.forEachIndexed { pIdx, paragraph ->
                                Text(
                                    text = paragraph.trim(),
                                    fontFamily = readerFontFamily,
                                    fontSize = prefs.fontSizeSp.sp,
                                    lineHeight = (prefs.fontSizeSp * prefs.lineHeightMultiplier).sp,
                                    color = themeText,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "No chapters found for this book.",
                                color = themeText,
                                modifier = Modifier.padding(24.dp)
                            )
                        }

                        // Quick Annotation & Highlight Bar
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = themeText.copy(alpha = 0.06f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Quick Tools for Page ${currentBook.currentPage}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = themeText
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Highlight colors
                                    listOf("#FDE047", "#86EFAC", "#93C5FD", "#F472B6", "#FB923C").forEach { hex ->
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(hex)))
                                                .clickable {
                                                    val sample = currentChapter?.content?.take(80) ?: "Highlighted text"
                                                    viewModel.addHighlight(sample, hex)
                                                }
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    FilledTonalButton(
                                        onClick = {
                                            selectedWordForVocab = ""
                                            showVocabDialog = true
                                        },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Word", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // Bottom Navigation Toolbar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = themeBg,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Page Scrubber
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "p. ${currentBook.currentPage}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeText
                        )
                        Slider(
                            value = currentBook.currentPage.toFloat(),
                            onValueChange = { viewModel.updateReadingPage(it.toInt()) },
                            valueRange = 1f..currentBook.totalPages.toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("reader_page_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "${currentBook.totalPages} (${currentBook.progressPercent}%)",
                            fontSize = 11.sp,
                            color = themeText.copy(alpha = 0.7f)
                        )
                    }

                    // Navigation Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (chapterIndex > 0) {
                                    viewModel.changeChapter(chapterIndex - 1)
                                } else if (currentBook.currentPage > 1) {
                                    viewModel.updateReadingPage(currentBook.currentPage - 1)
                                }
                            },
                            enabled = chapterIndex > 0 || currentBook.currentPage > 1,
                            modifier = Modifier.testTag("reader_prev_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Previous Page/Chapter", tint = themeText)
                        }

                        TextButton(onClick = { showTocDialog = true }) {
                            Icon(Icons.Default.List, contentDescription = null, tint = themeText, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Table of Contents", color = themeText, fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = {
                                if (chapterIndex + 1 < currentBook.chapters.size) {
                                    viewModel.changeChapter(chapterIndex + 1)
                                } else if (currentBook.currentPage < currentBook.totalPages) {
                                    viewModel.updateReadingPage(currentBook.currentPage + 1)
                                }
                            },
                            enabled = chapterIndex + 1 < currentBook.chapters.size || currentBook.currentPage < currentBook.totalPages,
                            modifier = Modifier.testTag("reader_next_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next Page/Chapter", tint = themeText)
                        }
                    }
                }
            }
        }

        // Floating TTS Controls Bar
        AnimatedVisibility(
            visible = showTtsBar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            TtsControlsBar(
                ttsManager = viewModel.ttsManager,
                onClose = {
                    showTtsBar = false
                    viewModel.ttsManager.stop()
                }
            )
        }
    }

    // Modal Sheets
    if (showSettingsSheet) {
        ReaderSettingsSheet(
            preferences = prefs,
            onThemeSelect = { viewModel.updateThemeMode(it) },
            onFontSelect = { viewModel.updateFontFamily(it) },
            onLayoutSelect = { viewModel.updateLayoutMode(it) },
            onFontSizeDelta = { viewModel.updateFontSize(it) },
            onLineHeightDelta = { viewModel.updateLineHeight(it) },
            onMarginDelta = { viewModel.updateMargin(it) },
            onToggleWakeLock = { viewModel.toggleKeepAwake() },
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showAnnotationsSheet) {
        AnnotationsSheet(
            bookmarks = bookmarks,
            highlights = highlights,
            notes = notes,
            currentPage = currentBook.currentPage,
            onJumpToPage = { viewModel.updateReadingPage(it) },
            onDeleteBookmark = { viewModel.deleteBookmark(it) },
            onDeleteHighlight = { viewModel.deleteHighlight(it) },
            onDeleteNote = { viewModel.deleteNote(it) },
            onAddNote = { viewModel.addNote(it) },
            onDismiss = { showAnnotationsSheet = false }
        )
    }

    if (showFocusSheet) {
        FocusSheet(
            pomodoroTimer = viewModel.pomodoroTimer,
            ambientAudio = viewModel.ambientAudio,
            onDismiss = { showFocusSheet = false }
        )
    }

    // Table of Contents Dialog
    if (showTocDialog) {
        AlertDialog(
            onDismissRequest = { showTocDialog = false },
            title = { Text("Table of Contents", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    currentBook.chapters.forEachIndexed { idx, ch ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.changeChapter(idx)
                                    showTocDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = if (idx == chapterIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = ch.title,
                                fontWeight = if (idx == chapterIndex) FontWeight.Bold else FontWeight.Normal,
                                color = if (idx == chapterIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTocDialog = false }) { Text("Close") }
            }
        )
    }

    // Add Vocabulary Word Dialog
    if (showVocabDialog) {
        var wordInput by remember { mutableStateOf(selectedWordForVocab) }
        var defInput by remember { mutableStateOf("") }
        var contextInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showVocabDialog = false },
            title = { Text("Add to Vocabulary", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wordInput,
                        onValueChange = { wordInput = it },
                        label = { Text("Word") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = defInput,
                        onValueChange = { defInput = it },
                        label = { Text("Definition / Meaning") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contextInput,
                        onValueChange = { contextInput = it },
                        label = { Text("Context Sentence (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (wordInput.isNotBlank() && defInput.isNotBlank()) {
                            viewModel.addVocabularyWord(wordInput, defInput, contextInput)
                            showVocabDialog = false
                        }
                    },
                    enabled = wordInput.isNotBlank() && defInput.isNotBlank()
                ) {
                    Text("Save Word")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVocabDialog = false }) { Text("Cancel") }
            }
        )
    }
}
