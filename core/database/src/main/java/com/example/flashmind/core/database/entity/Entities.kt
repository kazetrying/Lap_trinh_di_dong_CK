package com.example.flashmind.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
)

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val pronunciation: String?,
    val exampleSentence: String?,
    val imageUrl: String?,
    val audioUrl: String?,
    val isStarred: Boolean,
    val repetition: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val nextReviewAt: Long,
    val lastReviewedAt: Long?,
)

@Entity(tableName = "pending_sync_tasks")
data class PendingSyncEntity(
    @PrimaryKey val id: String,
    val type: String,
    val payload: String,
    val createdAt: Long,
)

@Entity(tableName = "review_history")
data class ReviewHistoryEntity(
    @PrimaryKey val id: String,
    val cardId: String,
    val deckId: String,
    val grade: Int,
    val reviewedAt: Long,
)
