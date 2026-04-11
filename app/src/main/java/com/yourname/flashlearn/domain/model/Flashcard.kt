package com.yourname.flashlearn.domain.model

data class Flashcard(
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val interval: Int = 1,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val totalReviews: Int = 0,
    val correctReviews: Int = 0
) {
    val accuracyPercent: Int
        get() = if (totalReviews == 0) 0
        else (correctReviews * 100 / totalReviews)
}