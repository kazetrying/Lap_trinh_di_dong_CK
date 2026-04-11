package com.yourname.flashlearn

import com.yourname.flashlearn.data.local.entity.FlashcardEntity
import com.yourname.flashlearn.domain.usecase.SM2Algorithm
import org.junit.Assert.*
import org.junit.Test

class SM2AlgorithmTest {

    private fun newCard() = FlashcardEntity(
        id = 1L, deckId = 1L,
        front = "Hello", back = "Xin chào",
        repetitions = 0, easeFactor = 2.5,
        interval = 1,
        nextReviewDate = System.currentTimeMillis()
    )

    @Test
    fun `first correct review sets repetitions to 1`() {
        val result = SM2Algorithm.calculateNextReview(newCard(), quality = 4)
        assertEquals(1, result.newRepetitions)
    }

    @Test
    fun `second correct review sets interval to 6`() {
        val card = newCard().copy(repetitions = 1, interval = 1)
        val result = SM2Algorithm.calculateNextReview(card, quality = 4)
        assertEquals(6, result.newInterval)
    }

    @Test
    fun `failed review resets repetitions to 0`() {
        val card = newCard().copy(repetitions = 5, interval = 30)
        val result = SM2Algorithm.calculateNextReview(card, quality = 1)
        assertEquals(0, result.newRepetitions)
    }

    @Test
    fun `easy answer increases ease factor`() {
        val result = SM2Algorithm.calculateNextReview(newCard(), quality = 5)
        assertTrue(result.newEaseFactor > 2.5)
    }

    @Test
    fun `hard answer decreases ease factor`() {
        val result = SM2Algorithm.calculateNextReview(newCard(), quality = 3)
        assertTrue(result.newEaseFactor < 2.5)
    }

    @Test
    fun `ease factor never drops below 1_3`() {
        var card = newCard()
        repeat(20) {
            val result = SM2Algorithm.calculateNextReview(card, quality = 0)
            card = card.copy(easeFactor = result.newEaseFactor)
        }
        assertTrue(card.easeFactor >= 1.3)
    }

    @Test
    fun `invalid quality throws exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            SM2Algorithm.calculateNextReview(newCard(), quality = 6)
        }
    }
}