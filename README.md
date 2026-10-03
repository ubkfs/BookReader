# ReadVault — Android Native E-Book Reader & Library

ReadVault is a modern, high-fidelity native Android e-book reading platform built with Kotlin and Jetpack Compose. Rewritten from the original ReadVault web/PHP system, it preserves all core domain features while taking full advantage of Android hardware, including native Text-to-Speech (TTS), offline local database caching, synthetic ambient audio focus generators, adaptive layout typography, and goal tracking.

## Core Features Ported

1. **E-Book Reader Experience**
   - **Immersive Typography**: Serif, Modern Sans, Dyslexic-friendly, and Monospace font styling with live size adjustments (12–30 sp), line spacing, and page margins.
   - **Themes**: Light, Warm Sepia, Night / Dark Slate, and High Contrast.
   - **Text-to-Speech (TTS)**: Built-in Android `TextToSpeech` engine integration with speech speed adjustment (0.75x–1.5x), pitch control, pause/resume/stop, and visual paragraph narration tracking.
   - **Annotations**:
     - Color-coded highlights (Yellow, Green, Blue, Pink, Orange).
     - Page bookmarks with chapter title tags and notes.
     - Notes drawer with quotes and jump-to-page navigation.
   - **Focus Mode & Ambient Audio**:
     - Synthetic offline PCM sound generator (`AudioTrack`) providing Gentle Rain, Ocean Waves, White Noise, and Forest Breeze without external audio dependencies.
     - Pomodoro reading countdown timer (25 min Pomodoro, 45 min Deep Read, 60 min Marathon).
   - **Hardware Wake Lock**: Screen stays awake automatically during active reading sessions.

2. **Personal Library & Vault**
   - Browse catalog with filtering by reading status ("All", "Currently Reading", "My Vault", "Finished") and categories ("Classics", "Philosophy", "Sci-Fi", "Strategy", "Personal").
   - Real-time search by title, author, and genre.
   - Book Upload / Import dialog to add custom e-books, articles, or notes into the local library.
   - Pre-seeded classic literature with chapters (*The Great Gatsby*, *Meditations*, *The Time Machine*, *Frankenstein*, *The Art of War*).

3. **Vocabulary Builder & Interactive Flashcards**
   - Save unfamiliar words from books with definitions and context quotes.
   - Filter words by mastery level (*Learning*, *Familiar*, *Mastered*).
   - Interactive 3D flip Flashcards review mode to practice and test vocabulary.

4. **Reading Goals, Streaks & Gamification**
   - Daily reading time target and pages target with real-time visual progress rings.
   - Reading streak tracking with streak flame counter.
   - Yearly reading challenge tracker.
   - Reading session logging (duration, pages read, timestamp).
   - Achievement badges system (*First Explorer*, *Consistency Champion*, *Night Owl*, *Thoughtful Reader*, *Lexicon Master*, etc.).
   - Multi-day reading challenges with progress tracking.

5. **Memberships & SaaS Plans**
   - Tiered plans (*Free*, *ReadVault Pro*, *Annual Member*, *Lifetime Patron*) with feature matrix.
   - Coupon promo code redemption system (e.g., `READVAULT20`, `STUDENT`).
   - Household family sharing entitlement preview.

6. **Support Desk & Help Center**
   - Support tickets management with status tags (*Open*, *Resolved*).
   - Ticket creation and real-time conversation messaging thread with automated support assistant replies.

## Architecture

- **UI Framework**: 100% Jetpack Compose using Material Design 3 (M3).
- **Architecture**: MVVM with Kotlin Coroutines and StateFlow.
- **Persistence**: SQLite database via `ReadVaultDatabaseHelper` and `ReadVaultRepository` on `Dispatchers.IO`.
- **Audio & Media**: Android `TextToSpeech` and synthetic PCM `AudioTrack` ambient audio generator.
- **Minimum SDK**: Android 8.0 (API 26) / Target SDK 36.
