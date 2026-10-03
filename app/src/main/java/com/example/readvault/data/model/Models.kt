package com.example.readvault.data.model

data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val category: String,
    val totalPages: Int,
    val currentPage: Int = 1,
    val description: String = "",
    val rating: Float = 4.8f,
    val fileType: String = "EPUB",
    val readingStatus: String = "not_started", // "not_started", "reading", "finished"
    val coverStartColor: Long = 0xFF1E3A8AL,
    val coverEndColor: Long = 0xFF0F172AL,
    val chapters: List<Chapter> = emptyList(),
    val isVaultBook: Boolean = false,
    val isOfflineAvailable: Boolean = true
) {
    val progressPercent: Int
        get() = if (totalPages > 0) ((currentPage.toFloat() / totalPages) * 100).toInt().coerceIn(0, 100) else 0

    val estimatedMinutesLeft: Int
        get() = ((totalPages - currentPage).coerceAtLeast(0) * 1.8f).toInt()
}

data class Chapter(
    val id: Long,
    val bookId: Long,
    val chapterIndex: Int,
    val title: String,
    val content: String
)

data class Bookmark(
    val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int,
    val chapterTitle: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Highlight(
    val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int,
    val selectedText: String,
    val colorHex: String = "#FDE047", // Yellow, Green, Blue, Pink, Orange, Purple
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Note(
    val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int,
    val noteText: String,
    val quoteText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class VocabularyWord(
    val id: Long = 0,
    val bookId: Long = 0,
    val word: String,
    val definition: String,
    val contextSentence: String = "",
    val masteryLevel: String = "Learning", // "Learning", "Familiar", "Mastered"
    val createdAt: Long = System.currentTimeMillis()
)

data class ReadingGoal(
    val dailyMinutesGoal: Int = 30,
    val dailyPagesGoal: Int = 20,
    val yearlyBooksGoal: Int = 24,
    val minutesReadToday: Int = 22,
    val pagesReadToday: Int = 14,
    val booksReadThisYear: Int = 8,
    val currentStreakDays: Int = 5,
    val longestStreakDays: Int = 18,
    val lastReadDate: String = ""
)

data class ReadingSession(
    val id: Long = 0,
    val bookId: Long,
    val bookTitle: String,
    val durationMinutes: Int,
    val pagesRead: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class Badge(
    val code: String,
    val title: String,
    val description: String,
    val points: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null,
    val iconName: String = "emoji_events"
)

data class ReadingChallenge(
    val id: Long,
    val title: String,
    val description: String,
    val targetDays: Int,
    val currentDays: Int,
    val rewardPoints: Int,
    val isCompleted: Boolean = false
)

data class SubscriptionPlan(
    val code: String,
    val name: String,
    val priceUsd: Double,
    val billingPeriod: String, // "Free Forever", "3 Days", "/month", "/quarter", "/year", "One-Time"
    val description: String,
    val bookLimit: String,
    val offlineLimit: String,
    val features: List<String>,
    val isCurrent: Boolean = false,
    val isPopular: Boolean = false
)

data class SupportTicket(
    val id: Long,
    val ticketNumber: String,
    val subject: String,
    val category: String,
    val priority: String, // "Low", "Medium", "High"
    val status: String, // "Open", "Waiting Reply", "Resolved"
    val date: String,
    val messages: List<TicketMessage> = emptyList()
)

data class TicketMessage(
    val id: Long,
    val senderName: String,
    val message: String,
    val time: String,
    val isAdmin: Boolean = false
)

enum class ReaderThemeMode {
    LIGHT, SEPIA, NIGHT, HIGH_CONTRAST
}

enum class ReaderFontFamily {
    SERIF, SANS, MONOSPACE, DYSLEXIC
}

enum class ReaderLayoutMode {
    PAGE, SCROLL
}

data class ReaderPreferences(
    val themeMode: ReaderThemeMode = ReaderThemeMode.SEPIA,
    val fontFamily: ReaderFontFamily = ReaderFontFamily.SERIF,
    val layoutMode: ReaderLayoutMode = ReaderLayoutMode.PAGE,
    val fontSizeSp: Float = 17f,
    val lineHeightMultiplier: Float = 1.6f,
    val horizontalMarginDp: Float = 20f,
    val keepAwake: Boolean = true,
    val autoScroll: Boolean = false,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f
)
