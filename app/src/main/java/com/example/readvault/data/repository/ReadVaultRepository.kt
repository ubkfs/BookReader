package com.example.readvault.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.readvault.data.db.ReadVaultDatabaseHelper
import com.example.readvault.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ReadVaultRepository(context: Context) {
    private val dbHelper = ReadVaultDatabaseHelper(context)

    private val _booksFlow = MutableStateFlow<List<Book>>(emptyList())
    val booksFlow: Flow<List<Book>> = _booksFlow.asStateFlow()

    private val _vocabularyFlow = MutableStateFlow<List<VocabularyWord>>(emptyList())
    val vocabularyFlow: Flow<List<VocabularyWord>> = _vocabularyFlow.asStateFlow()

    private val _goalsFlow = MutableStateFlow(ReadingGoal())
    val goalsFlow: Flow<ReadingGoal> = _goalsFlow.asStateFlow()

    private val _badgesFlow = MutableStateFlow<List<Badge>>(emptyList())
    val badgesFlow: Flow<List<Badge>> = _badgesFlow.asStateFlow()

    private val _challengesFlow = MutableStateFlow<List<ReadingChallenge>>(emptyList())
    val challengesFlow: Flow<List<ReadingChallenge>> = _challengesFlow.asStateFlow()

    private val _supportTicketsFlow = MutableStateFlow<List<SupportTicket>>(emptyList())
    val supportTicketsFlow: Flow<List<SupportTicket>> = _supportTicketsFlow.asStateFlow()

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        refreshBooks()
        refreshVocabulary()
        refreshGoals()
        refreshBadges()
        refreshChallenges()
        refreshTickets()
    }

    suspend fun refreshBooks() = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM book_books ORDER BY reading_status = 'reading' DESC, id ASC", null)
        val books = mutableListOf<Book>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val author = cursor.getString(cursor.getColumnIndexOrThrow("author"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            val totalPages = cursor.getInt(cursor.getColumnIndexOrThrow("total_pages"))
            val currentPage = cursor.getInt(cursor.getColumnIndexOrThrow("current_page"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: ""
            val rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"))
            val fileType = cursor.getString(cursor.getColumnIndexOrThrow("file_type")) ?: "EPUB"
            val readingStatus = cursor.getString(cursor.getColumnIndexOrThrow("reading_status")) ?: "not_started"
            val coverStartColor = cursor.getLong(cursor.getColumnIndexOrThrow("cover_start_color"))
            val coverEndColor = cursor.getLong(cursor.getColumnIndexOrThrow("cover_end_color"))
            val isVault = cursor.getInt(cursor.getColumnIndexOrThrow("is_vault_book")) == 1

            // Load chapters
            val chapters = loadChapters(db, id)
            books.add(
                Book(
                    id = id,
                    title = title,
                    author = author,
                    category = category,
                    totalPages = totalPages,
                    currentPage = currentPage,
                    description = description,
                    rating = rating,
                    fileType = fileType,
                    readingStatus = readingStatus,
                    coverStartColor = coverStartColor,
                    coverEndColor = coverEndColor,
                    chapters = chapters,
                    isVaultBook = isVault
                )
            )
        }
        cursor.close()
        _booksFlow.value = books
    }

    private fun loadChapters(db: android.database.sqlite.SQLiteDatabase, bookId: Long): List<Chapter> {
        val chCursor = db.rawQuery(
            "SELECT * FROM book_chapters WHERE book_id = ? ORDER BY chapter_index ASC",
            arrayOf(bookId.toString())
        )
        val list = mutableListOf<Chapter>()
        while (chCursor.moveToNext()) {
            list.add(
                Chapter(
                    id = chCursor.getLong(chCursor.getColumnIndexOrThrow("id")),
                    bookId = bookId,
                    chapterIndex = chCursor.getInt(chCursor.getColumnIndexOrThrow("chapter_index")),
                    title = chCursor.getString(chCursor.getColumnIndexOrThrow("title")),
                    content = chCursor.getString(chCursor.getColumnIndexOrThrow("content"))
                )
            )
        }
        chCursor.close()
        return list
    }

    suspend fun getBook(id: Long): Book? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM book_books WHERE id = ? LIMIT 1", arrayOf(id.toString()))
        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val author = cursor.getString(cursor.getColumnIndexOrThrow("author"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            val totalPages = cursor.getInt(cursor.getColumnIndexOrThrow("total_pages"))
            val currentPage = cursor.getInt(cursor.getColumnIndexOrThrow("current_page"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: ""
            val rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"))
            val fileType = cursor.getString(cursor.getColumnIndexOrThrow("file_type")) ?: "EPUB"
            val readingStatus = cursor.getString(cursor.getColumnIndexOrThrow("reading_status")) ?: "not_started"
            val coverStartColor = cursor.getLong(cursor.getColumnIndexOrThrow("cover_start_color"))
            val coverEndColor = cursor.getLong(cursor.getColumnIndexOrThrow("cover_end_color"))
            val isVault = cursor.getInt(cursor.getColumnIndexOrThrow("is_vault_book")) == 1
            val chapters = loadChapters(db, id)
            cursor.close()
            Book(
                id = id,
                title = title,
                author = author,
                category = category,
                totalPages = totalPages,
                currentPage = currentPage,
                description = description,
                rating = rating,
                fileType = fileType,
                readingStatus = readingStatus,
                coverStartColor = coverStartColor,
                coverEndColor = coverEndColor,
                chapters = chapters,
                isVaultBook = isVault
            )
        } else {
            cursor.close()
            null
        }
    }

    suspend fun updateBookProgress(bookId: Long, page: Int, totalPages: Int) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val status = if (page >= totalPages) "finished" else "reading"
        val cv = ContentValues().apply {
            put("current_page", page)
            put("reading_status", status)
        }
        db.update("book_books", cv, "id = ?", arrayOf(bookId.toString()))
        refreshBooks()
    }

    suspend fun addNewBook(
        title: String,
        author: String,
        category: String,
        description: String,
        content: String
    ): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val pages = ((content.length / 500) + 1).coerceAtLeast(3)
        val cv = ContentValues().apply {
            put("title", title.trim())
            put("author", author.ifBlank { "Personal Upload" })
            put("category", category.ifBlank { "Personal" })
            put("total_pages", pages)
            put("current_page", 1)
            put("description", description.ifBlank { "Uploaded personal text" })
            put("rating", 5.0)
            put("file_type", "EPUB")
            put("reading_status", "not_started")
            put("cover_start_color", 0xFF0D9488L)
            put("cover_end_color", 0xFF115E59L)
            put("is_vault_book", 1)
        }
        val bookId = db.insert("book_books", null, cv)
        val chCv = ContentValues().apply {
            put("book_id", bookId)
            put("chapter_index", 1)
            put("title", "Chapter 1: " + title.trim())
            put("content", content)
        }
        db.insert("book_chapters", null, chCv)
        refreshBooks()
        bookId
    }

    suspend fun toggleVaultStatus(bookId: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery("SELECT is_vault_book FROM book_books WHERE id = ?", arrayOf(bookId.toString()))
        if (cursor.moveToFirst()) {
            val current = cursor.getInt(0)
            val updated = if (current == 1) 0 else 1
            val cv = ContentValues().apply { put("is_vault_book", updated) }
            db.update("book_books", cv, "id = ?", arrayOf(bookId.toString()))
        }
        cursor.close()
        refreshBooks()
    }

    // Bookmarks
    suspend fun getBookmarks(bookId: Long): List<Bookmark> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM book_bookmarks WHERE book_id = ? ORDER BY page_number ASC",
            arrayOf(bookId.toString())
        )
        val list = mutableListOf<Bookmark>()
        while (cursor.moveToNext()) {
            list.add(
                Bookmark(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    bookId = bookId,
                    pageNumber = cursor.getInt(cursor.getColumnIndexOrThrow("page_number")),
                    chapterTitle = cursor.getString(cursor.getColumnIndexOrThrow("chapter_title")),
                    note = cursor.getString(cursor.getColumnIndexOrThrow("note")) ?: "",
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun addBookmark(bookmark: Bookmark): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("book_id", bookmark.bookId)
            put("page_number", bookmark.pageNumber)
            put("chapter_title", bookmark.chapterTitle)
            put("note", bookmark.note)
            put("created_at", bookmark.createdAt)
        }
        db.insert("book_bookmarks", null, cv)
    }

    suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("book_bookmarks", "id = ?", arrayOf(id.toString()))
    }

    // Highlights
    suspend fun getHighlights(bookId: Long): List<Highlight> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM book_highlights WHERE book_id = ? ORDER BY page_number ASC",
            arrayOf(bookId.toString())
        )
        val list = mutableListOf<Highlight>()
        while (cursor.moveToNext()) {
            list.add(
                Highlight(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    bookId = bookId,
                    pageNumber = cursor.getInt(cursor.getColumnIndexOrThrow("page_number")),
                    selectedText = cursor.getString(cursor.getColumnIndexOrThrow("selected_text")),
                    colorHex = cursor.getString(cursor.getColumnIndexOrThrow("color_hex")),
                    note = cursor.getString(cursor.getColumnIndexOrThrow("note")) ?: "",
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun addHighlight(highlight: Highlight): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("book_id", highlight.bookId)
            put("page_number", highlight.pageNumber)
            put("selected_text", highlight.selectedText)
            put("color_hex", highlight.colorHex)
            put("note", highlight.note)
            put("created_at", highlight.createdAt)
        }
        db.insert("book_highlights", null, cv)
    }

    suspend fun deleteHighlight(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("book_highlights", "id = ?", arrayOf(id.toString()))
    }

    // Notes
    suspend fun getNotes(bookId: Long): List<Note> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM book_notes WHERE book_id = ? ORDER BY page_number ASC",
            arrayOf(bookId.toString())
        )
        val list = mutableListOf<Note>()
        while (cursor.moveToNext()) {
            list.add(
                Note(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    bookId = bookId,
                    pageNumber = cursor.getInt(cursor.getColumnIndexOrThrow("page_number")),
                    noteText = cursor.getString(cursor.getColumnIndexOrThrow("note_text")),
                    quoteText = cursor.getString(cursor.getColumnIndexOrThrow("quote_text")) ?: "",
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun addNote(note: Note): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("book_id", note.bookId)
            put("page_number", note.pageNumber)
            put("note_text", note.noteText)
            put("quote_text", note.quoteText)
            put("created_at", note.createdAt)
        }
        db.insert("book_notes", null, cv)
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("book_notes", "id = ?", arrayOf(id.toString()))
    }

    // Vocabulary
    suspend fun refreshVocabulary() = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM book_vocabulary ORDER BY created_at DESC", null)
        val list = mutableListOf<VocabularyWord>()
        while (cursor.moveToNext()) {
            list.add(
                VocabularyWord(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    bookId = cursor.getLong(cursor.getColumnIndexOrThrow("book_id")),
                    word = cursor.getString(cursor.getColumnIndexOrThrow("word")),
                    definition = cursor.getString(cursor.getColumnIndexOrThrow("definition")),
                    contextSentence = cursor.getString(cursor.getColumnIndexOrThrow("context_sentence")) ?: "",
                    masteryLevel = cursor.getString(cursor.getColumnIndexOrThrow("mastery_level")) ?: "Learning",
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                )
            )
        }
        cursor.close()
        _vocabularyFlow.value = list
    }

    suspend fun addVocabularyWord(word: String, definition: String, context: String, bookId: Long = 0) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("book_id", bookId)
            put("word", word.trim())
            put("definition", definition.trim())
            put("context_sentence", context.trim())
            put("mastery_level", "Learning")
            put("created_at", System.currentTimeMillis())
        }
        db.insert("book_vocabulary", null, cv)
        refreshVocabulary()
    }

    suspend fun updateVocabularyMastery(id: Long, level: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply { put("mastery_level", level) }
        db.update("book_vocabulary", cv, "id = ?", arrayOf(id.toString()))
        refreshVocabulary()
    }

    suspend fun deleteVocabulary(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("book_vocabulary", "id = ?", arrayOf(id.toString()))
        refreshVocabulary()
    }

    // Goals & Sessions
    suspend fun refreshGoals() = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM book_reading_goals WHERE id = 1 LIMIT 1", null)
        if (cursor.moveToFirst()) {
            val goal = ReadingGoal(
                dailyMinutesGoal = cursor.getInt(cursor.getColumnIndexOrThrow("daily_minutes_goal")),
                dailyPagesGoal = cursor.getInt(cursor.getColumnIndexOrThrow("daily_pages_goal")),
                yearlyBooksGoal = cursor.getInt(cursor.getColumnIndexOrThrow("yearly_books_goal")),
                minutesReadToday = cursor.getInt(cursor.getColumnIndexOrThrow("minutes_read_today")),
                pagesReadToday = cursor.getInt(cursor.getColumnIndexOrThrow("pages_read_today")),
                booksReadThisYear = cursor.getInt(cursor.getColumnIndexOrThrow("books_read_this_year")),
                currentStreakDays = cursor.getInt(cursor.getColumnIndexOrThrow("current_streak_days")),
                longestStreakDays = cursor.getInt(cursor.getColumnIndexOrThrow("longest_streak_days")),
                lastReadDate = cursor.getString(cursor.getColumnIndexOrThrow("last_read_date")) ?: "Today"
            )
            _goalsFlow.value = goal
        }
        cursor.close()
    }

    suspend fun updateGoals(dailyMinutes: Int, dailyPages: Int, yearlyBooks: Int) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("daily_minutes_goal", dailyMinutes)
            put("daily_pages_goal", dailyPages)
            put("yearly_books_goal", yearlyBooks)
        }
        db.update("book_reading_goals", cv, "id = 1", null)
        refreshGoals()
    }

    suspend fun recordReadingSession(bookId: Long, bookTitle: String, durationMinutes: Int, pagesRead: Int) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("book_id", bookId)
            put("book_title", bookTitle)
            put("duration_minutes", durationMinutes)
            put("pages_read", pagesRead)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert("book_reading_sessions", null, cv)

        // Increment today's progress
        db.execSQL(
            "UPDATE book_reading_goals SET minutes_read_today = minutes_read_today + ?, pages_read_today = pages_read_today + ? WHERE id = 1",
            arrayOf(durationMinutes, pagesRead)
        )
        refreshGoals()
    }

    suspend fun getReadingSessions(): List<ReadingSession> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM book_reading_sessions ORDER BY timestamp DESC LIMIT 20", null)
        val list = mutableListOf<ReadingSession>()
        while (cursor.moveToNext()) {
            list.add(
                ReadingSession(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    bookId = cursor.getLong(cursor.getColumnIndexOrThrow("book_id")),
                    bookTitle = cursor.getString(cursor.getColumnIndexOrThrow("book_title")),
                    durationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("duration_minutes")),
                    pagesRead = cursor.getInt(cursor.getColumnIndexOrThrow("pages_read")),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                )
            )
        }
        cursor.close()
        list
    }

    // Badges & Challenges
    suspend fun refreshBadges() = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM book_badges ORDER BY points ASC", null)
        val list = mutableListOf<Badge>()
        while (cursor.moveToNext()) {
            list.add(
                Badge(
                    code = cursor.getString(cursor.getColumnIndexOrThrow("code")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    points = cursor.getInt(cursor.getColumnIndexOrThrow("points")),
                    isUnlocked = cursor.getInt(cursor.getColumnIndexOrThrow("is_unlocked")) == 1,
                    unlockedDate = cursor.getString(cursor.getColumnIndexOrThrow("unlocked_date")),
                    iconName = cursor.getString(cursor.getColumnIndexOrThrow("icon_name"))
                )
            )
        }
        cursor.close()
        _badgesFlow.value = list
    }

    suspend fun refreshChallenges() = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM book_reading_challenges", null)
        val list = mutableListOf<ReadingChallenge>()
        while (cursor.moveToNext()) {
            list.add(
                ReadingChallenge(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    targetDays = cursor.getInt(cursor.getColumnIndexOrThrow("target_days")),
                    currentDays = cursor.getInt(cursor.getColumnIndexOrThrow("current_days")),
                    rewardPoints = cursor.getInt(cursor.getColumnIndexOrThrow("reward_points")),
                    isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1
                )
            )
        }
        cursor.close()
        _challengesFlow.value = list
    }

    // Support Tickets
    suspend fun refreshTickets() = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM book_support_tickets ORDER BY id DESC", null)
        val tickets = mutableListOf<SupportTicket>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val ticketNumber = cursor.getString(cursor.getColumnIndexOrThrow("ticket_number"))
            val subject = cursor.getString(cursor.getColumnIndexOrThrow("subject"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            val priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))

            // Messages
            val mCursor = db.rawQuery(
                "SELECT * FROM book_ticket_messages WHERE ticket_id = ? ORDER BY id ASC",
                arrayOf(id.toString())
            )
            val messages = mutableListOf<TicketMessage>()
            while (mCursor.moveToNext()) {
                messages.add(
                    TicketMessage(
                        id = mCursor.getLong(mCursor.getColumnIndexOrThrow("id")),
                        senderName = mCursor.getString(mCursor.getColumnIndexOrThrow("sender_name")),
                        message = mCursor.getString(mCursor.getColumnIndexOrThrow("message")),
                        time = mCursor.getString(mCursor.getColumnIndexOrThrow("time")),
                        isAdmin = mCursor.getInt(mCursor.getColumnIndexOrThrow("is_admin")) == 1
                    )
                )
            }
            mCursor.close()

            tickets.add(
                SupportTicket(
                    id = id,
                    ticketNumber = ticketNumber,
                    subject = subject,
                    category = category,
                    priority = priority,
                    status = status,
                    date = date,
                    messages = messages
                )
            )
        }
        cursor.close()
        _supportTicketsFlow.value = tickets
    }

    suspend fun createSupportTicket(subject: String, category: String, message: String): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val ticketNum = "TK-" + (1000..9999).random()
        val cv = ContentValues().apply {
            put("ticket_number", ticketNum)
            put("subject", subject)
            put("category", category)
            put("priority", "Medium")
            put("status", "Open")
            put("date", "Just now")
        }
        val ticketId = db.insert("book_support_tickets", null, cv)
        val mCv = ContentValues().apply {
            put("ticket_id", ticketId)
            put("sender_name", "You")
            put("message", message)
            put("time", "Just now")
            put("is_admin", 0)
        }
        db.insert("book_ticket_messages", null, mCv)
        refreshTickets()
        ticketId
    }

    suspend fun replySupportTicket(ticketId: Long, message: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val mCv = ContentValues().apply {
            put("ticket_id", ticketId)
            put("sender_name", "You")
            put("message", message)
            put("time", "Just now")
            put("is_admin", 0)
        }
        db.insert("book_ticket_messages", null, mCv)

        // Add automated acknowledgment
        val ackCv = ContentValues().apply {
            put("ticket_id", ticketId)
            put("sender_name", "ReadVault Assistant")
            put("message", "Thanks for the update! Our reading support team has received your message and will respond shortly.")
            put("time", "1m ago")
            put("is_admin", 1)
        }
        db.insert("book_ticket_messages", null, ackCv)
        refreshTickets()
    }
}
