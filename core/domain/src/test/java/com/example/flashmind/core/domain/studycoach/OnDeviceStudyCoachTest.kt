package com.example.flashmind.core.domain.studycoach

import com.example.flashmind.core.model.Deck
import com.example.flashmind.core.model.InsightPriority
import com.example.flashmind.core.model.StudyProgress
import com.example.flashmind.core.model.VocabularyCard
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnDeviceStudyCoachTest {

    private val coach = OnDeviceStudyCoach()

    @Test
    fun `analyze flags urgent and hard cards`() {
        val now = Instant.parse("2026-04-11T00:00:00Z")
        val snapshot = coach.analyze(
            deck = Deck(id = "deck-1", title = "English", description = "", dueCount = 6, cardCount = 6),
            cards = listOf(
                card("1", true, 1.6, 0, now.minusSeconds(60 * 60 * 20)),
                card("2", false, 1.7, 1, now.minusSeconds(60 * 60 * 14)),
                card("3", true, 2.4, 4, now.plusSeconds(60 * 60 * 3)),
            ),
            now = now,
        )

        assertEquals("Focused sprint", snapshot.focusBand)
        assertEquals(2, snapshot.urgentCards)
        assertEquals(2, snapshot.hardCards)
        assertEquals(2, snapshot.starredCards)
        assertTrue(snapshot.insights.any { it.priority == InsightPriority.HIGH })
    }

    @Test
    fun `analyze returns warm start for empty deck`() {
        val snapshot = coach.analyze(
            deck = Deck(id = "deck-2", title = "Empty", description = "", dueCount = 0, cardCount = 0),
            cards = emptyList(),
        )

        assertEquals("Warm start", snapshot.focusBand)
        assertEquals("Deck is empty", snapshot.insights.first().title)
    }

    private fun card(
        id: String,
        starred: Boolean,
        easeFactor: Double,
        repetition: Int,
        nextReviewAt: Instant,
    ): VocabularyCard {
        return VocabularyCard(
            id = id,
            deckId = "deck-1",
            front = "front-$id",
            back = "back-$id",
            isStarred = starred,
            progress = StudyProgress(
                repetition = repetition,
                intervalDays = 1,
                easeFactor = easeFactor,
                nextReviewAt = nextReviewAt,
                lastReviewedAt = null,
            ),
        )
    }
}
