package com.example.readvault.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readvault.data.model.*
import com.example.readvault.data.repository.ReadVaultRepository
import com.example.readvault.reader.AmbientAudioGenerator
import com.example.readvault.reader.PomodoroTimer
import com.example.readvault.reader.TextToSpeechManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Library : Screen()
    data class Reader(val bookId: Long) : Screen()
    data class BookDetail(val bookId: Long) : Screen()
    object Vocabulary : Screen()
    object Goals : Screen()
    object Plans : Screen()
    object Support : Screen()
    object Settings : Screen()
}

class ReadVaultViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ReadVaultRepository(application)
    val ttsManager = TextToSpeechManager(application)
    val ambientAudio = AmbientAudioGenerator()
    val pomodoroTimer = PomodoroTimer(viewModelScope)

    // Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Library)
    val currentScreen = _currentScreen.asStateFlow()

    // Library filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: All, 1: Reading, 2: My Vault, 3: Completed
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Data from repository
    val books = repository.booksFlow
    val vocabulary = repository.vocabularyFlow
    val readingGoal = repository.goalsFlow
    val badges = repository.badgesFlow
    val challenges = repository.challengesFlow
    val supportTickets = repository.supportTicketsFlow

    // Filtered books
    val filteredBooks = combine(books, searchQuery, selectedTab, selectedCategory) { bookList, query, tab, cat ->
        bookList.filter { book ->
            val matchesQuery = query.isBlank() ||
                    book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true) ||
                    book.category.contains(query, ignoreCase = true)

            val matchesTab = when (tab) {
                1 -> book.readingStatus == "reading"
                2 -> book.isVaultBook
                3 -> book.readingStatus == "finished"
                else -> true
            }

            val matchesCategory = (cat == "All") || book.category.equals(cat, ignoreCase = true)

            matchesQuery && matchesTab && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Reader state
    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook = _currentBook.asStateFlow()

    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex = _currentChapterIndex.asStateFlow()

    private val _readerPreferences = MutableStateFlow(ReaderPreferences())
    val readerPreferences = _readerPreferences.asStateFlow()

    private val _bookBookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookBookmarks = _bookBookmarks.asStateFlow()

    private val _bookHighlights = MutableStateFlow<List<Highlight>>(emptyList())
    val bookHighlights = _bookHighlights.asStateFlow()

    private val _bookNotes = MutableStateFlow<List<Note>>(emptyList())
    val bookNotes = _bookNotes.asStateFlow()

    private val _readingSessions = MutableStateFlow<List<ReadingSession>>(emptyList())
    val readingSessions = _readingSessions.asStateFlow()

    // Flashcards state
    private val _flashcardIndex = MutableStateFlow(0)
    val flashcardIndex = _flashcardIndex.asStateFlow()

    private val _isFlashcardFlipped = MutableStateFlow(false)
    val isFlashcardFlipped = _isFlashcardFlipped.asStateFlow()

    // Plans
    private val _plans = MutableStateFlow(
        listOf(
            SubscriptionPlan(
                code = "free",
                name = "Free",
                priceUsd = 0.0,
                billingPeriod = "Free Forever",
                description = "Start reading with personal uploads and standard library access.",
                bookLimit = "10 books",
                offlineLimit = "1 offline book",
                features = listOf("EPUB and PDF reading", "Basic reader themes", "1 device access", "Standard reading progress"),
                isCurrent = false
            ),
            SubscriptionPlan(
                code = "monthly",
                name = "ReadVault Pro",
                priceUsd = 9.99,
                billingPeriod = "/month",
                description = "Full catalog access, audio voice reading, focus ambient sounds, and sync.",
                bookLimit = "Unlimited books",
                offlineLimit = "25 offline downloads",
                features = listOf("Unlimited curated catalog", "Voice text-to-speech engine", "Focus mode & Pomodoro timer", "Ambient noise generator", "Flashcards & vocabulary builder", "Export notes & highlights", "2 simultaneous devices"),
                isCurrent = true,
                isPopular = true
            ),
            SubscriptionPlan(
                code = "annual",
                name = "Annual Member",
                priceUsd = 79.99,
                billingPeriod = "/year",
                description = "Save 33% with full premium features, priority support, and family sharing.",
                bookLimit = "Unlimited books",
                offlineLimit = "150 offline downloads",
                features = listOf("All Pro features included", "5 family household profiles", "Priority 24/7 ticket support", "Early access to new book arrivals", "Advanced reading analytics", "Custom font uploads"),
                isCurrent = false
            ),
            SubscriptionPlan(
                code = "lifetime",
                name = "Lifetime Patron",
                priceUsd = 199.99,
                billingPeriod = "One-Time",
                description = "Pay once and read forever with unlimited downloads and exclusive badges.",
                bookLimit = "Unlimited books",
                offlineLimit = "Unlimited offline downloads",
                features = listOf("Lifetime unlimited access", "All future features & upgrades", "6 family profiles", "Exclusive Gold Patron badge", "Direct author interaction access"),
                isCurrent = false
            )
        )
    )
    val plans = _plans.asStateFlow()

    private val _activePlanCode = MutableStateFlow("monthly")
    val activePlanCode = _activePlanCode.asStateFlow()

    private val _couponMessage = MutableStateFlow<String?>(null)
    val couponMessage = _couponMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.refreshAll()
            _readingSessions.value = repository.getReadingSessions()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    // Open Book
    fun openBook(bookId: Long) {
        viewModelScope.launch {
            val book = repository.getBook(bookId)
            _currentBook.value = book
            _currentChapterIndex.value = 0
            if (book != null) {
                loadBookAnnotations(bookId)
                repository.updateBookProgress(bookId, book.currentPage.coerceAtLeast(1), book.totalPages)
            }
            navigateTo(Screen.Reader(bookId))
        }
    }

    private suspend fun loadBookAnnotations(bookId: Long) {
        _bookBookmarks.value = repository.getBookmarks(bookId)
        _bookHighlights.value = repository.getHighlights(bookId)
        _bookNotes.value = repository.getNotes(bookId)
    }

    fun changeChapter(index: Int) {
        val book = _currentBook.value ?: return
        if (index in book.chapters.indices) {
            _currentChapterIndex.value = index
            val approxPage = (index * (book.totalPages.toFloat() / book.chapters.size)).toInt() + 1
            updateReadingPage(approxPage)
        }
    }

    fun updateReadingPage(page: Int) {
        val book = _currentBook.value ?: return
        val clampedPage = page.coerceIn(1, book.totalPages)
        _currentBook.value = book.copy(currentPage = clampedPage)
        viewModelScope.launch {
            repository.updateBookProgress(book.id, clampedPage, book.totalPages)
        }
    }

    // Reader Preferences
    fun updateThemeMode(mode: ReaderThemeMode) {
        _readerPreferences.value = _readerPreferences.value.copy(themeMode = mode)
    }

    fun updateFontFamily(font: ReaderFontFamily) {
        _readerPreferences.value = _readerPreferences.value.copy(fontFamily = font)
    }

    fun updateLayoutMode(layout: ReaderLayoutMode) {
        _readerPreferences.value = _readerPreferences.value.copy(layoutMode = layout)
    }

    fun updateFontSize(delta: Float) {
        val newSize = (_readerPreferences.value.fontSizeSp + delta).coerceIn(12f, 30f)
        _readerPreferences.value = _readerPreferences.value.copy(fontSizeSp = newSize)
    }

    fun updateLineHeight(delta: Float) {
        val newHeight = (_readerPreferences.value.lineHeightMultiplier + delta).coerceIn(1.2f, 2.4f)
        _readerPreferences.value = _readerPreferences.value.copy(lineHeightMultiplier = newHeight)
    }

    fun updateMargin(delta: Float) {
        val newMargin = (_readerPreferences.value.horizontalMarginDp + delta).coerceIn(8f, 40f)
        _readerPreferences.value = _readerPreferences.value.copy(horizontalMarginDp = newMargin)
    }

    fun toggleKeepAwake() {
        _readerPreferences.value = _readerPreferences.value.copy(keepAwake = !_readerPreferences.value.keepAwake)
    }

    // Bookmarks, Highlights, Notes
    fun addBookmark(note: String = "") {
        val book = _currentBook.value ?: return
        val currentCh = book.chapters.getOrNull(_currentChapterIndex.value)?.title ?: "Chapter 1"
        viewModelScope.launch {
            val bm = Bookmark(
                bookId = book.id,
                pageNumber = book.currentPage,
                chapterTitle = currentCh,
                note = note
            )
            repository.addBookmark(bm)
            _bookBookmarks.value = repository.getBookmarks(book.id)
        }
    }

    fun deleteBookmark(id: Long) {
        val book = _currentBook.value ?: return
        viewModelScope.launch {
            repository.deleteBookmark(id)
            _bookBookmarks.value = repository.getBookmarks(book.id)
        }
    }

    fun addHighlight(text: String, colorHex: String = "#FDE047", note: String = "") {
        val book = _currentBook.value ?: return
        viewModelScope.launch {
            val hl = Highlight(
                bookId = book.id,
                pageNumber = book.currentPage,
                selectedText = text,
                colorHex = colorHex,
                note = note
            )
            repository.addHighlight(hl)
            _bookHighlights.value = repository.getHighlights(book.id)
        }
    }

    fun deleteHighlight(id: Long) {
        val book = _currentBook.value ?: return
        viewModelScope.launch {
            repository.deleteHighlight(id)
            _bookHighlights.value = repository.getHighlights(book.id)
        }
    }

    fun addNote(noteText: String, quoteText: String = "") {
        val book = _currentBook.value ?: return
        viewModelScope.launch {
            val n = Note(
                bookId = book.id,
                pageNumber = book.currentPage,
                noteText = noteText,
                quoteText = quoteText
            )
            repository.addNote(n)
            _bookNotes.value = repository.getNotes(book.id)
        }
    }

    fun deleteNote(id: Long) {
        val book = _currentBook.value ?: return
        viewModelScope.launch {
            repository.deleteNote(id)
            _bookNotes.value = repository.getNotes(book.id)
        }
    }

    // Vocabulary
    fun addVocabularyWord(word: String, definition: String, context: String = "") {
        val bookId = _currentBook.value?.id ?: 0L
        viewModelScope.launch {
            repository.addVocabularyWord(word, definition, context, bookId)
        }
    }

    fun updateWordMastery(id: Long, level: String) {
        viewModelScope.launch {
            repository.updateVocabularyMastery(id, level)
        }
    }

    fun deleteVocabulary(id: Long) {
        viewModelScope.launch {
            repository.deleteVocabulary(id)
        }
    }

    // Flashcards
    fun nextFlashcard(total: Int) {
        _isFlashcardFlipped.value = false
        if (total > 0) {
            _flashcardIndex.value = (_flashcardIndex.value + 1) % total
        }
    }

    fun prevFlashcard(total: Int) {
        _isFlashcardFlipped.value = false
        if (total > 0) {
            _flashcardIndex.value = if (_flashcardIndex.value - 1 < 0) total - 1 else _flashcardIndex.value - 1
        }
    }

    fun flipFlashcard() {
        _isFlashcardFlipped.value = !_isFlashcardFlipped.value
    }

    // Upload new book
    fun uploadBook(title: String, author: String, category: String, description: String, content: String) {
        viewModelScope.launch {
            val bookId = repository.addNewBook(title, author, category, description, content)
            openBook(bookId)
        }
    }

    fun toggleVaultBook(bookId: Long) {
        viewModelScope.launch {
            repository.toggleVaultStatus(bookId)
        }
    }

    // Goals & Sessions
    fun saveSession(durationMinutes: Int, pagesRead: Int) {
        val book = _currentBook.value ?: return
        viewModelScope.launch {
            repository.recordReadingSession(book.id, book.title, durationMinutes, pagesRead)
            _readingSessions.value = repository.getReadingSessions()
        }
    }

    fun updateReadingGoals(dailyMin: Int, dailyPages: Int, yearlyBooks: Int) {
        viewModelScope.launch {
            repository.updateGoals(dailyMin, dailyPages, yearlyBooks)
        }
    }

    // Subscriptions
    fun applyCoupon(code: String) {
        val clean = code.trim().uppercase()
        if (clean == "READVAULT20" || clean == "STUDENT" || clean == "WELCOME") {
            _couponMessage.value = "Coupon '$clean' applied! 20% discount granted on all plans."
        } else {
            _couponMessage.value = "Invalid or expired coupon code."
        }
    }

    fun selectPlan(code: String) {
        _activePlanCode.value = code
        _plans.value = _plans.value.map { it.copy(isCurrent = it.code == code) }
    }

    // Support
    fun createSupportTicket(subject: String, category: String, message: String) {
        viewModelScope.launch {
            repository.createSupportTicket(subject, category, message)
        }
    }

    fun replyToTicket(ticketId: Long, message: String) {
        viewModelScope.launch {
            repository.replySupportTicket(ticketId, message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        ambientAudio.stop()
        pomodoroTimer.stopTimer()
    }
}
