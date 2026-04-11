package com.example.flashmind.core.domain.spacedrepetition

import com.example.flashmind.core.model.ReviewGrade
import com.example.flashmind.core.model.StudyProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sm2SchedulerTest {

    private val scheduler = Sm2Scheduler()

    @Test
    fun `again should reset repetition and keep minimum ease factor`() {
        val initial = StudyProgress(
            repetition = 4,
            intervalDays = 16,
            easeFactor = 1.35,
        )

        val result = scheduler.review(initial, ReviewGrade.AGAIN)

        assertEquals(0, result.updatedProgress.repetition)
        assertEquals(1, result.updatedProgress.intervalDays)
        assertTrue(result.updatedProgress.easeFactor >= 1.3)
    }

    @Test
    fun `good should move first repetition to one day`() {
        val result = scheduler.review(StudyProgress(), ReviewGrade.GOOD)

        assertEquals(1, result.updatedProgress.repetition)
        assertEquals(1, result.updatedProgress.intervalDays)
    }
}
