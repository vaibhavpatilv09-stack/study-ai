package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SubjectCategory(val displayName: String, val colorHex: Long) {
    BIOLOGY("Biology & Life Sciences", 0xFF10B981),
    CHEMISTRY("Chemistry & Matter", 0xFF06B6D4),
    HISTORY("World History & Civilization", 0xFFF59E0B),
    COMPUTER_SCIENCE("Computer Science & AI", 0xFF6366F1),
    PHYSICS("Physics & Mechanics", 0xFFEC4899),
    MATHEMATICS("Calculus & Algebra", 0xFF8B5CF6)
}

enum class MasteryStatus {
    LOCKED,
    IN_PROGRESS,
    REVIEWING,
    MASTERED
}

@Entity(tableName = "flashcard_decks")
data class StudyDeck(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: SubjectCategory,
    val cardCount: Int = 0,
    val masteredCount: Int = 0,
    val bannerColor: Long = 0xFF6366F1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey val id: String,
    val deckId: String,
    val frontText: String,
    val backText: String,
    val hint: String = "",
    val category: SubjectCategory,
    val masteryLevel: Int = 0, // 0 to 5
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val easeFactor: Float = 2.5f,
    val lastReviewed: Long = 0L,
    val nextReviewDue: Long = System.currentTimeMillis()
)

@Entity(tableName = "knowledge_nodes")
data class KnowledgeNode(
    @PrimaryKey val id: String,
    val topicTitle: String,
    val subtitle: String,
    val category: SubjectCategory,
    val masteryScore: Int = 0, // 0 to 100%
    val status: MasteryStatus = MasteryStatus.LOCKED,
    val prerequisites: List<String> = emptyList(), // Node IDs
    val keyTakeaways: List<String> = emptyList(),
    val flashcardCount: Int = 10,
    val estimatedMinutes: Int = 15,
    val chapterOrder: Int = 1
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey val id: String,
    val subject: SubjectCategory,
    val durationMinutes: Int,
    val focusScore: Int, // 0 to 100
    val flashcardsReviewed: Int,
    val cardsMastered: Int,
    val sessionType: String = "Pomodoro Focus",
    val notesTaken: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val role: String, // "user", "model", "system"
    val text: String,
    val imageBase64: String? = null,
    val thinkingProcess: String? = null,
    val citations: List<CitationSource> = emptyList(),
    val isFastMode: Boolean = false,
    val isThinkingMode: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CitationSource(
    val title: String,
    val url: String,
    val snippet: String
)

@Entity(tableName = "user_stats")
data class UserProfileStats(
    @PrimaryKey val userId: String = "local_user",
    val displayName: String = "Alex Rivera",
    val email: String = "alex.rivera@studymate.ai",
    val isGoogleUser: Boolean = false,
    val streakDays: Int = 12,
    val totalStudyMinutes: Int = 840,
    val flashcardsMastered: Int = 248,
    val overallMasteryPercentage: Int = 78,
    val currentLevel: Int = 4,
    val xpPoints: Int = 3450,
    val dailyGoalMinutes: Int = 45,
    val todayMinutes: Int = 32
)

@Entity(tableName = "study_documents")
data class StudyDocument(
    @PrimaryKey val id: String,
    val title: String,
    val category: SubjectCategory,
    val rawText: String,
    val aiSummary: String,
    val keyConcepts: List<String> = emptyList(),
    val generatedQuestions: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val iconName: String,
    val isRead: Boolean = false
)
