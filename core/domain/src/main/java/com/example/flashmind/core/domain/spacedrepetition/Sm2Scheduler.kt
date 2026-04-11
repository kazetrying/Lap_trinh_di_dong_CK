package com.example.flashmind.core.domain.spacedrepetition

import com.example.flashmind.core.model.ReviewGrade
import com.example.flashmind.core.model.ReviewResult
import com.example.flashmind.core.model.StudyProgress
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class Sm2Scheduler @Inject constructor() {

    fun review(progress: StudyProgress, grade: ReviewGrade): ReviewResult {
        val now = Instant.now()
        if (grade.score < 3) {
            return ReviewResult(
                updatedProgress = progress.copy(
                    repetition = 0,
                    intervalDays = 1,
                    easeFactor = (progress.easeFactor - 0.2).coerceAtLeast(1.3),
                    lastReviewedAt = now,
                    nextReviewAt = now.plus(1, ChronoUnit.DAYS),
                ),
                isLapse = true,
            )
        }

        val nextEase = calculateEaseFactor(progress.easeFactor, grade.score)
        val nextRepetition = progress.repetition + 1
        val nextInterval = when (nextRepetition) {
            1 -> 1
            2 -> 6
            else -> (progress.intervalDays * nextEase).toInt().coerceAtLeast(progress.intervalDays + 1)
        }

        return ReviewResult(
            updatedProgress = progress.copy(
                repetition = nextRepetition,
                intervalDays = nextInterval,
                easeFactor = nextEase,
                lastReviewedAt = now,
                nextReviewAt = now.plus(nextInterval.toLong(), ChronoUnit.DAYS),
            ),
            isLapse = false,
        )
    }

    private fun calculateEaseFactor(current: Double, quality: Int): Double {
        val updated = current + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        return updated.coerceAtLeast(1.3)
    }
}
