package com.example.flashmind.core.domain.studycoach

import com.example.flashmind.core.model.Deck
import com.example.flashmind.core.model.InsightPriority
import com.example.flashmind.core.model.StudyCoachSnapshot
import com.example.flashmind.core.model.StudyInsight
import com.example.flashmind.core.model.VocabularyCard
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class OnDeviceStudyCoach @Inject constructor() {

    fun analyze(
        deck: Deck?,
        cards: List<VocabularyCard>,
        now: Instant = Instant.now(),
    ): StudyCoachSnapshot {
        if (cards.isEmpty()) {
            return StudyCoachSnapshot(
                focusBand = "Warm start",
                insights = listOf(
                    StudyInsight(
                        title = "Deck is empty",
                        summary = "Add the first cards to unlock local AI coaching and adaptive review insights.",
                        actionLabel = "Create cards",
                        priority = InsightPriority.LOW,
                    ),
                ),
            )
        }

        val urgentCards = cards.count { Duration.between(it.progress.nextReviewAt, now).toHours() >= 12 }
        val hardCards = cards.count { it.progress.easeFactor <= 1.8 || it.progress.repetition <= 1 }
        val starredCards = cards.count(VocabularyCard::isStarred)
        val readinessScore = (
            100 -
                urgentCards * 12 -
                hardCards * 5 +
                starredCards * 2 +
                (deck?.dueCount ?: 0).coerceAtMost(10)
            ).coerceIn(12, 100)

        val focusBand = when {
            readinessScore >= 80 -> "Realtime ready"
            readinessScore >= 55 -> "Focused sprint"
            else -> "Recovery mode"
        }

        val insights = buildList {
            if (urgentCards > 0) {
                add(
                    StudyInsight(
                        title = "Forgetting risk detected",
                        summary = "$urgentCards card(s) are overdue by at least 12 hours. Review them first to cut memory decay.",
                        actionLabel = "Review urgent cards",
                        priority = InsightPriority.HIGH,
                    ),
                )
            }
            if (hardCards > 0) {
                add(
                    StudyInsight(
                        title = "Hard cluster found",
                        summary = "$hardCards card(s) have low ease or very early repetitions. Pair learn mode with short sessions.",
                        actionLabel = "Open learn mode",
                        priority = if (hardCards >= 4) InsightPriority.HIGH else InsightPriority.MEDIUM,
                    ),
                )
            }
            if (starredCards > 0) {
                add(
                    StudyInsight(
                        title = "High-value cards available",
                        summary = "$starredCards starred card(s) are good candidates for a quick booster session before tests.",
                        actionLabel = "Filter starred",
                        priority = InsightPriority.MEDIUM,
                    ),
                )
            }
            if (isEmpty()) {
                add(
                    StudyInsight(
                        title = "Flow is healthy",
                        summary = "No major risk spike found. Keep the current study rhythm and review when the next cards unlock.",
                        actionLabel = "Stay consistent",
                        priority = InsightPriority.LOW,
                    ),
                )
            }
        }

        return StudyCoachSnapshot(
            readinessScore = readinessScore,
            focusBand = focusBand,
            urgentCards = urgentCards,
            hardCards = hardCards,
            starredCards = starredCards,
            insights = insights.take(3),
        )
    }
}
