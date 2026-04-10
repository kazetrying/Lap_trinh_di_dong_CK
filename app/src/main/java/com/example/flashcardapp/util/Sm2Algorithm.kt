package com.example.flashcardapp.util

import com.example.flashcardapp.data.Card
import java.util.concurrent.TimeUnit

fun applySmTwo(card: Card, quality: Int): Card {
    val newRepetition: Int
    val newInterval: Int
    val newEaseFactor: Float
    val nextReview: Long

    if (quality < 3) {
        // ❌ TRẢ LỜI SAI HOẶC QUÊN
        newRepetition = 0
        newInterval = 0
        newEaseFactor = card.easeFactor
        
        // Phân biệt Quên và Khó
        val minutes = if (quality == 0) 1L else 5L
        nextReview = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)
    } else {
        // ✅ TRẢ LỜI ĐÚNG
        newRepetition = card.repetition + 1
        newInterval = when (newRepetition) {
            1    -> 1
            2    -> 6
            else -> (card.interval * card.easeFactor).toInt()
        }
        
        // Tăng Ease Factor mạnh hơn nếu chọn "Dễ" (quality 5)
        val delta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)
        newEaseFactor = maxOf(1.3f, (card.easeFactor + delta).toFloat())
        
        nextReview = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(newInterval.toLong())
    }

    return card.copy(
        interval       = newInterval,
        repetition     = newRepetition,
        easeFactor     = newEaseFactor,
        nextReviewDate = nextReview
    )
}