package com.example.flashmind.feature.deck

import com.example.flashmind.core.domain.usecase.DeleteDeckUseCase
import com.example.flashmind.core.domain.usecase.ObserveDecksUseCase
import com.example.flashmind.core.domain.usecase.ObservePendingSyncTasksUseCase
import com.example.flashmind.core.domain.usecase.ObserveStudyAnalyticsUseCase
import com.example.flashmind.core.domain.usecase.RefreshDecksUseCase
import com.example.flashmind.core.domain.usecase.SyncPendingTasksUseCase
import com.example.flashmind.core.model.Deck
import com.example.flashmind.core.model.PendingSyncTask
import com.example.flashmind.core.model.StudyAnalytics
import com.example.flashmind.core.model.SyncTaskType
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DeckListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeDecksUseCase: ObserveDecksUseCase = mock()
    private val observePendingSyncTasksUseCase: ObservePendingSyncTasksUseCase = mock()
    private val observeStudyAnalyticsUseCase: ObserveStudyAnalyticsUseCase = mock()
    private val refreshDecksUseCase: RefreshDecksUseCase = mock()
    private val syncPendingTasksUseCase: SyncPendingTasksUseCase = mock()
    private val deleteDeckUseCase: DeleteDeckUseCase = mock()

    private val decksFlow = MutableStateFlow(emptyList<Deck>())
    private val tasksFlow = MutableStateFlow(emptyList<PendingSyncTask>())
    private val analyticsFlow = MutableStateFlow(StudyAnalytics())

    @Before
    fun setUp() {
        whenever(observeDecksUseCase.invoke()).thenReturn(decksFlow)
        whenever(observePendingSyncTasksUseCase.invoke()).thenReturn(tasksFlow)
        whenever(observeStudyAnalyticsUseCase.invoke()).thenReturn(analyticsFlow)
        runTest {
            whenever(refreshDecksUseCase.invoke()).thenAnswer { }
            whenever(syncPendingTasksUseCase.invoke()).thenAnswer { }
            whenever(deleteDeckUseCase.invoke(org.mockito.kotlin.any())).thenAnswer { }
        }
    }

    @Test
    fun `search filters decks and updates aggregate state`() = runTest {
        val viewModel = createViewModel()
        decksFlow.value = listOf(
            Deck(id = "1", title = "English Core", description = "verbs", dueCount = 3, cardCount = 10),
            Deck(id = "2", title = "Japanese", description = "kana", dueCount = 1, cardCount = 5),
        )
        tasksFlow.value = listOf(
            PendingSyncTask(
                id = "sync-1",
                type = SyncTaskType.REVIEW,
                payload = "{}",
                createdAt = Instant.now(),
            ),
        )
        analyticsFlow.value = StudyAnalytics(totalReviews = 20, todayReviews = 4, currentStreakDays = 3)

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.totalDeckCount)
        assertEquals(15, viewModel.uiState.value.totalCardCount)
        assertEquals(4, viewModel.uiState.value.totalDueCount)
        assertEquals(1, viewModel.uiState.value.pendingSyncCount)

        viewModel.onSearchQueryChanged("english")

        assertEquals(listOf("English Core"), viewModel.uiState.value.decks.map { it.title })
    }

    @Test
    fun `syncNow completes and clears syncing flag`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.syncNow()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        verify(syncPendingTasksUseCase, atLeastOnce()).invoke()
        verify(refreshDecksUseCase, atLeastOnce()).invoke()
    }

    private fun createViewModel(): DeckListViewModel {
        return DeckListViewModel(
            observeDecksUseCase = observeDecksUseCase,
            observePendingSyncTasksUseCase = observePendingSyncTasksUseCase,
            observeStudyAnalyticsUseCase = observeStudyAnalyticsUseCase,
            refreshDecksUseCase = refreshDecksUseCase,
            syncPendingTasksUseCase = syncPendingTasksUseCase,
            deleteDeckUseCase = deleteDeckUseCase,
        )
    }
}
