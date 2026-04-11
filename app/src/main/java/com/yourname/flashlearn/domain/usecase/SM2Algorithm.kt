package com.yourname.flashlearn.domain.usecase

import com.yourname.flashlearn.data.local.entity.FlashcardEntity
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToInt

object SM2Algorithm {

    private const val MIN_EASE_FACTOR = 1.3

    data class ReviewResult(
        val newRepetitions: Int,
        val newEaseFactor: Double,
        val newInterval: Int,
        val nextReviewDate: Long
    )

    fun calculateNextReview(card: FlashcardEntity, quality: Int): ReviewResult {
        require(quality in 0..5) { "Quality must be between 0 and 5" }
        return if (quality < 3) handleFailed(card) else handleSuccess(card, quality)
    }

    private fun handleFailed(card: FlashcardEntity): ReviewResult {
        val newEase = max(MIN_EASE_FACTOR, card.easeFactor - 0.2)
        return ReviewResult(
            newRepetitions = 0,
            newEaseFactor = newEase,
            newInterval = 1,
            nextReviewDate = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)
        )
    }

    private fun handleSuccess(card: FlashcardEntity, quality: Int): ReviewResult {
        val easeDelta = when (quality) {
            5 -> 0.15
            4 -> 0.0
            else -> -0.15
        }
        val newEase = max(MIN_EASE_FACTOR, card.easeFactor + easeDelta)
        val newReps = card.repetitions + 1
        val newInterval = when (newReps) {
            1 -> 1
            2 -> 6
            else -> (card.interval * newEase).roundToInt()
        }
        return ReviewResult(
            newRepetitions = newReps,
            newEaseFactor = newEase,
            newInterval = newInterval,
            nextReviewDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(newInterval.toLong())
        )
    }

    fun applyReviewResult(card: FlashcardEntity, quality: Int): FlashcardEntity {
        val result = calculateNextReview(card, quality)
        return card.copy(
            repetitions = result.newRepetitions,
            easeFactor = result.newEaseFactor,
            interval = result.newInterval,
            nextReviewDate = result.nextReviewDate,
            lastReviewDate = System.currentTimeMillis(),
            totalReviews = card.totalReviews + 1,
            correctReviews = if (quality >= 3) card.correctReviews + 1 else card.correctReviews
        )
    }

    enum class CardStatus { NEW, LEARNING, DUE, MATURE }

    fun getCardStatus(card: FlashcardEntity): CardStatus = when {
        card.repetitions == 0 -> CardStatus.NEW
        card.nextReviewDate <= System.currentTimeMillis() -> CardStatus.DUE
        card.interval >= 21 -> CardStatus.MATURE
        else -> CardStatus.LEARNING
    }
}