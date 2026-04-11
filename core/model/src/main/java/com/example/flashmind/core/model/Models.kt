package com.example.flashmind.core.model

import java.time.Instant

data class Deck(
    val id: String,
    val title: String,
    val description: String,
    val dueCount: Int = 0,
    val cardCount: Int = 0,
)

data class VocabularyCard(
    val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val pronunciation: String? = null,
    val exampleSentence: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val isStarred: Boolean = false,
    val progress: StudyProgress,
)

data class StudyProgress(
    val repetition: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewAt: Instant = Instant.now(),
    val lastReviewedAt: Instant? = null,
)

enum class ReviewGrade(val score: Int) {
    AGAIN(0),
    HARD(3),
    GOOD(4),
    EASY(5),
}

data class ReviewResult(
    val updatedProgress: StudyProgress,
    val isLapse: Boolean,
)

data class PendingSyncTask(
    val id: String,
    val type: SyncTaskType,
    val payload: String,
    val createdAt: Instant,
)

data class StudyAnalytics(
    val totalReviews: Int = 0,
    val todayReviews: Int = 0,
    val currentStreakDays: Int = 0,
)

data class StudyCoachSnapshot(
    val readinessScore: Int = 100,
    val focusBand: String = "Balanced",
    val urgentCards: Int = 0,
    val hardCards: Int = 0,
    val starredCards: Int = 0,
    val insights: List<StudyInsight> = emptyList(),
)

data class StudyInsight(
    val title: String,
    val summary: String,
    val actionLabel: String,
    val priority: InsightPriority,
)

enum class InsightPriority {
    HIGH,
    MEDIUM,
    LOW,
}

data class ImportCardDraft(
    val front: String,
    val back: String,
    val pronunciation: String? = null,
    val exampleSentence: String? = null,
    val imageUrl: String? = null,
    val isStarred: Boolean = false,
)

enum class SyncTaskType {
    REVIEW,
    CREATE_DECK,
    UPDATE_DECK,
    CREATE_CARD,
    UPDATE_CARD,
    DELETE_CARD,
    DELETE_DECK,
    TOGGLE_CARD_STAR,
}
