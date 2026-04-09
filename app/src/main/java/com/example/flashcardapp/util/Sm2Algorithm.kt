package com.example.flashcardapp.util

import com.example.flashcardapp.data.Card
import java.util.concurrent.TimeUnit

/**
 * SuperMemo SM-2 Algorithm
 *
 * quality:
 *   0 = Quên hoàn toàn
 *   1 = Nhớ sai, nhưng nhìn đáp án thấy quen
 *   2 = Nhớ sai, nhưng đáp án dễ nhớ lại
 *   3 = Nhớ đúng, nhưng khó
 *   4 = Nhớ đúng, sau chút do dự
 *   5 = Nhớ đúng hoàn hảo
 */
fun applySmTwo(card: Card, quality: Int): Card {
    require(quality in 0..5) { "Quality phải từ 0 đến 5" }

    val newRepetition: Int
    val newInterval: Int
    val newEaseFactor: Float

    if (quality < 3) {
        // Trả lời sai → reset về đầu
        newRepetition = 0
        newInterval = 1
        newEaseFactor = card.easeFactor
    } else {
        // Trả lời đúng → tính interval mới
        newRepetition = card.repetition + 1
        newInterval = when (card.repetition) {
            0    -> 1
            1    -> 6
            else -> (card.interval * card.easeFactor).toInt()
        }
        // EF mới: EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
        val delta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)
        newEaseFactor = maxOf(1.3f, (card.easeFactor + delta).toFloat())
    }

    val nextReview = System.currentTimeMillis() +
            TimeUnit.DAYS.toMillis(newInterval.toLong())

    return card.copy(
        interval      = newInterval,
        repetition    = newRepetition,
        easeFactor    = newEaseFactor,
        nextReviewDate = nextReview
    )
}