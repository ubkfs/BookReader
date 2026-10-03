package com.example.readvault.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.readvault.data.model.*

class ReadVaultDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "readvault.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Books table
        db.execSQL(
            """
            CREATE TABLE book_books (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                category TEXT NOT NULL,
                total_pages INTEGER NOT NULL,
                current_page INTEGER DEFAULT 1,
                description TEXT,
                rating REAL DEFAULT 4.8,
                file_type TEXT DEFAULT 'EPUB',
                reading_status TEXT DEFAULT 'not_started',
                cover_start_color INTEGER DEFAULT -14798695,
                cover_end_color INTEGER DEFAULT -15788226,
                is_vault_book INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        // Chapters table
        db.execSQL(
            """
            CREATE TABLE book_chapters (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                chapter_index INTEGER NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Bookmarks table
        db.execSQL(
            """
            CREATE TABLE book_bookmarks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                page_number INTEGER NOT NULL,
                chapter_title TEXT NOT NULL,
                note TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Highlights table
        db.execSQL(
            """
            CREATE TABLE book_highlights (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                page_number INTEGER NOT NULL,
                selected_text TEXT NOT NULL,
                color_hex TEXT NOT NULL,
                note TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Notes table
        db.execSQL(
            """
            CREATE TABLE book_notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                page_number INTEGER NOT NULL,
                note_text TEXT NOT NULL,
                quote_text TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Vocabulary table
        db.execSQL(
            """
            CREATE TABLE book_vocabulary (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER DEFAULT 0,
                word TEXT NOT NULL,
                definition TEXT NOT NULL,
                context_sentence TEXT,
                mastery_level TEXT DEFAULT 'Learning',
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Reading goals table
        db.execSQL(
            """
            CREATE TABLE book_reading_goals (
                id INTEGER PRIMARY KEY,
                daily_minutes_goal INTEGER DEFAULT 30,
                daily_pages_goal INTEGER DEFAULT 20,
                yearly_books_goal INTEGER DEFAULT 24,
                minutes_read_today INTEGER DEFAULT 22,
                pages_read_today INTEGER DEFAULT 14,
                books_read_this_year INTEGER DEFAULT 8,
                current_streak_days INTEGER DEFAULT 5,
                longest_streak_days INTEGER DEFAULT 18,
                last_read_date TEXT
            )
            """.trimIndent()
        )

        // Reading sessions table
        db.execSQL(
            """
            CREATE TABLE book_reading_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                book_title TEXT NOT NULL,
                duration_minutes INTEGER NOT NULL,
                pages_read INTEGER NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Badges table
        db.execSQL(
            """
            CREATE TABLE book_badges (
                code TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                points INTEGER NOT NULL,
                is_unlocked INTEGER DEFAULT 0,
                unlocked_date TEXT,
                icon_name TEXT DEFAULT 'emoji_events'
            )
            """.trimIndent()
        )

        // Reading Challenges table
        db.execSQL(
            """
            CREATE TABLE book_reading_challenges (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                target_days INTEGER NOT NULL,
                current_days INTEGER NOT NULL,
                reward_points INTEGER NOT NULL,
                is_completed INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        // Support tickets table
        db.execSQL(
            """
            CREATE TABLE book_support_tickets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ticket_number TEXT NOT NULL,
                subject TEXT NOT NULL,
                category TEXT NOT NULL,
                priority TEXT NOT NULL,
                status TEXT NOT NULL,
                date TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Ticket messages table
        db.execSQL(
            """
            CREATE TABLE book_ticket_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ticket_id INTEGER NOT NULL,
                sender_name TEXT NOT NULL,
                message TEXT NOT NULL,
                time TEXT NOT NULL,
                is_admin INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS book_books")
        db.execSQL("DROP TABLE IF EXISTS book_chapters")
        db.execSQL("DROP TABLE IF EXISTS book_bookmarks")
        db.execSQL("DROP TABLE IF EXISTS book_highlights")
        db.execSQL("DROP TABLE IF EXISTS book_notes")
        db.execSQL("DROP TABLE IF EXISTS book_vocabulary")
        db.execSQL("DROP TABLE IF EXISTS book_reading_goals")
        db.execSQL("DROP TABLE IF EXISTS book_reading_sessions")
        db.execSQL("DROP TABLE IF EXISTS book_badges")
        db.execSQL("DROP TABLE IF EXISTS book_reading_challenges")
        db.execSQL("DROP TABLE IF EXISTS book_support_tickets")
        db.execSQL("DROP TABLE IF EXISTS book_ticket_messages")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        // Seed Reading Goals
        val goalValues = ContentValues().apply {
            put("id", 1)
            put("daily_minutes_goal", 30)
            put("daily_pages_goal", 25)
            put("yearly_books_goal", 20)
            put("minutes_read_today", 26)
            put("pages_read_today", 18)
            put("books_read_this_year", 7)
            put("current_streak_days", 6)
            put("longest_streak_days", 19)
            put("last_read_date", "Today")
        }
        db.insert("book_reading_goals", null, goalValues)

        // Seed Badges
        val badges = listOf(
            arrayOf("first_book", "First Explorer", "Open and read your first chapter.", "10", "1", "May 12", "menu_book"),
            arrayOf("seven_day_streak", "Consistency Champion", "Read for 7 consecutive days in ReadVault.", "100", "0", null, "local_fire_department"),
            arrayOf("night_owl", "Night Owl", "Finish a reading session after midnight.", "40", "1", "June 2", "dark_mode"),
            arrayOf("note_taker", "Thoughtful Reader", "Save 10 annotations or notes in your books.", "50", "1", "June 14", "edit_note"),
            arrayOf("vocabulary_master", "Lexicon Master", "Master 15 vocabulary words with flashcards.", "75", "0", null, "school"),
            arrayOf("speed_reader", "Deep Immersion", "Read for over 45 minutes in Focus Mode.", "60", "1", "June 20", "timer"),
            arrayOf("book_finisher", "Book Finisher", "Read a book from cover to cover.", "120", "0", null, "military_tech")
        )
        for (b in badges) {
            val cv = ContentValues().apply {
                put("code", b[0])
                put("title", b[1])
                put("description", b[2])
                put("points", b[3]?.toInt() ?: 0)
                put("is_unlocked", b[4]?.toInt() ?: 0)
                put("unlocked_date", b[5])
                put("icon_name", b[6])
            }
            db.insert("book_badges", null, cv)
        }

        // Seed Challenges
        val challenges = listOf(
            arrayOf("Summer Reading Sprint", "Read at least 20 minutes daily for 14 days straight.", "14", "9", "150", "0"),
            arrayOf("Philosophy Deep Dive", "Complete 3 philosophy or self-help chapters this month.", "3", "2", "80", "0"),
            arrayOf("Vocabulary Builder", "Save and master 10 new words from your current read.", "10", "6", "100", "0")
        )
        for (c in challenges) {
            val cv = ContentValues().apply {
                put("title", c[0])
                put("description", c[1])
                put("target_days", c[2].toInt())
                put("current_days", c[3].toInt())
                put("reward_points", c[4].toInt())
                put("is_completed", c[5].toInt())
            }
            db.insert("book_reading_challenges", null, cv)
        }

        // Seed Vocabulary
        val vocabulary = listOf(
            arrayOf("ephemeral", "Lasting for a very short time; transitory; fleeting.", "The beauty of the cherry blossoms felt sublime yet wonderfully ephemeral.", "Mastered"),
            arrayOf("ineffable", "Too great or extreme to be expressed or described in words.", "Looking across the shimmering moonlit bay gave him an ineffable sense of longing.", "Familiar"),
            arrayOf("stoicism", "The endurance of pain or hardship without display of feelings or complaint.", "Marcus Aurelius reminded himself that true stoicism begins with internal peace.", "Learning"),
            arrayOf("serendipity", "The occurrence and development of events by chance in a happy or beneficial way.", "Finding this forgotten notebook in the old library was pure serendipity.", "Learning"),
            arrayOf("lucid", "Expressed clearly; easy to understand; bright or luminous.", "Her explanation of quantum mechanics was astonishingly lucid.", "Familiar")
        )
        for (v in vocabulary) {
            val cv = ContentValues().apply {
                put("book_id", 1)
                put("word", v[0])
                put("definition", v[1])
                put("context_sentence", v[2])
                put("mastery_level", v[3])
                put("created_at", System.currentTimeMillis() - 86400000L * (1..5).random())
            }
            db.insert("book_vocabulary", null, cv)
        }

        // Seed Support Tickets
        val tId = db.insert("book_support_tickets", null, ContentValues().apply {
            put("ticket_number", "TK-8421")
            put("subject", "Sync progress across offline devices")
            put("category", "Sync & Offline")
            put("priority", "Medium")
            put("status", "Resolved")
            put("date", "Yesterday, 3:15 PM")
        })
        db.insert("book_ticket_messages", null, ContentValues().apply {
            put("ticket_id", tId)
            put("sender_name", "You")
            put("message", "How do I ensure my reading position is cached when traveling offline?")
            put("time", "3:15 PM")
            put("is_admin", 0)
        })
        db.insert("book_ticket_messages", null, ContentValues().apply {
            put("ticket_id", tId)
            put("sender_name", "ReadVault Support")
            put("message", "Hi there! ReadVault automatically caches your books, bookmarks and notes locally. Your progress will instantly sync as soon as you reconnect to internet.")
            put("time", "3:40 PM")
            put("is_admin", 1)
        })

        // Seed Books with Chapters
        seedBooks(db)
    }

    private fun seedBooks(db: SQLiteDatabase) {
        // Book 1: The Great Gatsby
        val gatsbyId = db.insert("book_books", null, ContentValues().apply {
            put("title", "The Great Gatsby")
            put("author", "F. Scott Fitzgerald")
            put("category", "Classics")
            put("total_pages", 48)
            put("current_page", 12)
            put("description", "A timeless portrait of the Roaring Twenties, jazz, ambition, romance, and the elusive pursuit of the American Dream in West Egg.")
            put("rating", 4.9)
            put("file_type", "EPUB")
            put("reading_status", "reading")
            put("cover_start_color", 0xFF1E3A8AL)
            put("cover_end_color", 0xFF0F172AL)
            put("is_vault_book", 1)
        })

        insertChapter(
            db, gatsbyId, 1, "Chapter I: West Egg & The Green Light",
            """In my younger and more vulnerable years my father gave me some advice that I’ve been turning over in my mind ever since.

"Whenever you feel like criticizing anyone," he told me, "just remember that all the people in this world haven’t had the advantages that you’ve had."

He didn’t say any more, but we’ve always been unusually communicative in a reserved way, and I understood that he meant a great deal more than that. In consequence, I’m inclined to reserve all judgements, a habit that has opened up many curious natures to me and also made me the victim of not a few veteran bores.

When I came back from the East last autumn I felt that I wanted the world to be in uniform and at a sort of moral attention forever; I wanted no more riotous excursions with privileged glimpses into the human heart. Only Gatsby, the man who gives his name to this book, was exempt from my reaction—Gatsby, who represented everything for which I have an unaffected scorn.

If personality is an unbroken series of successful gestures, then there was something gorgeous about him, some heightened sensitivity to the promises of life, as if he were related to one of those intricate machines that register earthquakes ten thousand miles away. It was an extraordinary gift for hope, a romantic readiness such as I have never found in any other person and which it is not likely I shall ever find again.

I bought a dozen volumes on banking and credit and investment securities, and they stood on my shelf in red and gold like new money from the mint, promising to unfold the shining secrets that only Midas and Morgan and Maecenas knew. And I had the high intention of reading many other books besides."""
        )

        insertChapter(
            db, gatsbyId, 2, "Chapter II: The Valley of Ashes",
            """About half-way between West Egg and New York the motorroad hastily joins the railroad and runs beside it for a quarter of a mile, so as to shrink away from a certain desolate area of land. This is a valley of ashes—a fantastic farm where ashes grow like wheat into ridges and hills and grotesque gardens; where ashes take the forms of houses and chimneys and rising smoke and, finally, with a transcendent effort, of men who move dimly and already crumbling through the powdery air.

Above the gray land and the spasms of bleak dust which drift endlessly over it, you perceive, after a moment, the eyes of Doctor T. J. Eckleburg. The eyes of Doctor T. J. Eckleburg are blue and gigantic—their retinas are one yard high. They look out of no face, but, instead, from a pair of enormous yellow spectacles which pass over a non-existent nose.

Evidently some wild wag of an oculist set them there to fatten his practice in the borough of Queens, and then sank down himself into eternal blindness, or forgot them and moved away. But his eyes, dimmed a little by many paintless days, under sun and rain, brood on over the solemn dumping ground."""
        )

        // Book 2: Meditations
        val medId = db.insert("book_books", null, ContentValues().apply {
            put("title", "Meditations")
            put("author", "Marcus Aurelius")
            put("category", "Philosophy")
            put("total_pages", 64)
            put("current_page", 28)
            put("description", "Private reflections and personal writings of the Roman Emperor on Stoic philosophy, resilience, mortality, duty, and finding inner peace.")
            put("rating", 4.95)
            put("file_type", "EPUB")
            put("reading_status", "reading")
            put("cover_start_color", 0xFFD97706L)
            put("cover_end_color", 0xFF78350FL)
            put("is_vault_book", 1)
        })

        insertChapter(
            db, medId, 1, "Book Two: On Daily Conduct and Inner Citadel",
            """When you wake up in the morning, tell yourself: The people I deal with today will be meddling, ungrateful, arrogant, dishonest, jealous, and surly. They are like this because they cannot distinguish good from evil. But I have seen the beauty of good, and the ugliness of evil, and have recognized that the wrongdoer has a nature related to my own—not of the same blood or birth, but the same mind, and possessing a share of the divine.

None of them can hurt me. No one can implicate me in ugliness. Nor can I feel angry at my relative, or hate him. We were made to work together like feet, hands, and eyes, like the two rows of the teeth, upper and lower. To obstruct each other is unnatural. To feel anger at someone, to turn your back on him: these are obstructions.

Whatever this is that I am, it is a little flesh and breath, and the ruling part. Despise the flesh: blood and bones and a network, a jumble of nerves, veins, and arteries. Consider the breath: wind, always changing, expelled and sucked back in. The third part is the master. Put your books aside; don't distract yourself; it is not permitted."""
        )

        insertChapter(
            db, medId, 2, "Book Four: The Tranquility of the Soul",
            """People look for retreats for themselves, in the country, by the coast, or in the hills. There is nowhere that a person can find a more peaceful and trouble-free retreat than in his own mind. So constantly give yourself this retreat, and renew yourself. Let your basic principles be brief and fundamental, the kind that will at once wash away all sorrow and send you back without irritation to the life to which you must return.

Keep reminding yourself: what is it about things that irritates you? The badness of men? Recall the conclusion that rational beings exist for one another's sake, that tolerance is part of justice, and that wrong is not done deliberately.

Think of how many who spent their lives in mutual hatred, suspicion, and fighting are now dead and turned to dust. Stop, then, and consider this: the universe is change; life is opinion."""
        )

        // Book 3: The Time Machine
        val timeId = db.insert("book_books", null, ContentValues().apply {
            put("title", "The Time Machine")
            put("author", "H.G. Wells")
            put("category", "Sci-Fi")
            put("total_pages", 36)
            put("current_page", 1)
            put("description", "A classic speculative voyage through fourth-dimensional physics into the far distant future of humanity, Eloi and Morlocks.")
            put("rating", 4.7)
            put("file_type", "EPUB")
            put("reading_status", "not_started")
            put("cover_start_color", 0xFF059669L)
            put("cover_end_color", 0xFF064E3BL)
            put("is_vault_book", 1)
        })

        insertChapter(
            db, timeId, 1, "Chapter 1: The Fourth Dimension",
            """The Time Traveller (for so it will be convenient to speak of him) was expounding a recondite matter to us. His grey eyes shone and twinkled, and his usually pale face was flushed and animated. The fire burnt brightly, and the soft radiance of the incandescent lights in the lilies of silver caught the bubbles that flashed and passed in our glasses.

"You must follow me carefully. I shall have to controvert one or two ideas that are almost universally accepted. The geometry, for instance, they taught you at school is founded on a misconception."

"Is not that rather a large thing to expect us to begin upon?" said Filby, an argumentative person with red hair.

"I do not mean to ask you to accept anything without reasonable ground for it. You will soon admit as much as I need from you. You know of course that a mathematical line, a line of thickness nil, has no real existence. They taught you that? Neither has a mathematical plane. These things are mere abstractions."

"Clearly," the Medical Man agreed.

"Nor, having only length, breadth, and thickness, can a cube have a real existence."

"There I object," said Filby. "Of course a solid body may exist. All real things—"

"So most people think. But wait a moment. Can an instantaneous cube exist?"

"Don't follow you," said Filby.

"Can a cube that does not exist for any space of time, have an actual existence?"
Filby became pensive. "Clearly," the Time Traveller proceeded, "any real body must have extension in four directions: it must have Length, Breadth, Thickness, and—Duration."""
        )

        // Book 4: Frankenstein
        val frankId = db.insert("book_books", null, ContentValues().apply {
            put("title", "Frankenstein")
            put("author", "Mary Shelley")
            put("category", "Classics")
            put("total_pages", 52)
            put("current_page", 52)
            put("description", "Victor Frankenstein unravels the secret of life, unleashing a creature that struggles with solitude, rejection, and humanity.")
            put("rating", 4.85)
            put("file_type", "PDF")
            put("reading_status", "finished")
            put("cover_start_color", 0xFF4C1D95L)
            put("cover_end_color", 0xFF1E1B4BL)
            put("is_vault_book", 1)
        })

        insertChapter(
            db, frankId, 1, "Letter I & Chapter I",
            """You will rejoice to hear that no disaster has accompanied the commencement of an enterprise which you have regarded with such evil forebodings. I arrived here yesterday, and my first task is to assure my dear sister of my welfare and increasing confidence in the success of my undertaking.

I am already far north of London, and as I walk in the streets of Petersburgh, I feel a cold northern breeze play upon my cheeks, which braces my nerves and fills me with delight. Do you understand this feeling? This breeze, which has travelled from the regions towards which I am advancing, gives me a foretaste of those icy climes.

I try in vain to be persuaded that the pole is the seat of frost and desolation; it ever presents itself to my imagination as the region of beauty and delight. There, Margaret, the sun is for ever visible, its broad disk just skirting the horizon and diffusing a perpetual splendour."""
        )

        // Book 5: The Art of War
        val warId = db.insert("book_books", null, ContentValues().apply {
            put("title", "The Art of War")
            put("author", "Sun Tzu")
            put("category", "Strategy")
            put("total_pages", 30)
            put("current_page", 1)
            put("description", "Ancient military treatise attributed to Sun Tzu, composed of 13 chapters addressing strategy, tactics, psychological insight, and discipline.")
            put("rating", 4.8)
            put("file_type", "EPUB")
            put("reading_status", "not_started")
            put("cover_start_color", 0xFFB91C1CL)
            put("cover_end_color", 0xFF7F1D1DL)
            put("is_vault_book", 0)
        })

        insertChapter(
            db, warId, 1, "Chapter I: Laying Plans",
            """Sun Tzu said: The art of war is of vital importance to the State. It is a matter of life and death, a road either to safety or to ruin. Hence it is a subject of inquiry which can on no account be neglected.

The art of war, then, is governed by five constant factors, to be taken into account in one's deliberations, when seeking to determine the conditions obtaining in the field.

These are: (1) The Moral Law; (2) Heaven; (3) Earth; (4) The Commander; (5) Method and discipline.

The Moral Law causes the people to be in complete accord with their ruler, so that they will follow him regardless of their lives, undismayed by any danger.

Heaven signifies night and day, cold and heat, times and seasons. Earth comprises distances, great and small; danger and security; open ground and narrow passes; the chances of life and death.

All warfare is based on deception. Hence, when able to attack, we must seem unable; when using our forces, we must seem inactive; when we are near, we must make the enemy believe we are far away; when far away, we must make him believe we are near."""
        )

        // Seed some sample Bookmarks & Highlights for Gatsby
        db.insert("book_bookmarks", null, ContentValues().apply {
            put("book_id", gatsbyId)
            put("page_number", 4)
            put("chapter_title", "Chapter I: West Egg & The Green Light")
            put("note", "Nick's reflection on reserving judgment.")
            put("created_at", System.currentTimeMillis() - 172800000L)
        })

        db.insert("book_highlights", null, ContentValues().apply {
            put("book_id", gatsbyId)
            put("page_number", 4)
            put("selected_text", "In my younger and more vulnerable years my father gave me some advice that I’ve been turning over in my mind ever since.")
            put("color_hex", "#FDE047") // Yellow
            put("note", "Iconic opening line on empathy and perspective.")
            put("created_at", System.currentTimeMillis() - 172800000L)
        })

        db.insert("book_notes", null, ContentValues().apply {
            put("book_id", gatsbyId)
            put("page_number", 5)
            put("note_text", "The tension between moral reservation and fascination with Gatsby's romantic readiness.")
            put("quote_text", "an extraordinary gift for hope, a romantic readiness")
            put("created_at", System.currentTimeMillis() - 150000000L)
        })

        // Seed Reading Sessions
        db.insert("book_reading_sessions", null, ContentValues().apply {
            put("book_id", gatsbyId)
            put("book_title", "The Great Gatsby")
            put("duration_minutes", 24)
            put("pages_read", 8)
            put("timestamp", System.currentTimeMillis() - 86400000L)
        })

        db.insert("book_reading_sessions", null, ContentValues().apply {
            put("book_id", medId)
            put("book_title", "Meditations")
            put("duration_minutes", 32)
            put("pages_read", 10)
            put("timestamp", System.currentTimeMillis() - 172800000L)
        })
    }

    private fun insertChapter(
        db: SQLiteDatabase,
        bookId: Long,
        index: Int,
        title: String,
        content: String
    ) {
        val cv = ContentValues().apply {
            put("book_id", bookId)
            put("chapter_index", index)
            put("title", title)
            put("content", content)
        }
        db.insert("book_chapters", null, cv)
    }
}
