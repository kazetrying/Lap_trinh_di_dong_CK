package com.yourname.flashlearn.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [ForeignKey(
        entity = DeckEntity::class,
        parentColumns = ["id"],
        childColumns = ["deckId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("deckId")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val interval: Int = 1,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val lastReviewDate: Long? = null,
    val totalReviews: Int = 0,
    val correctReviews: Int = 0
)
val note: String = ""  // Ghi chú cho thẻ
