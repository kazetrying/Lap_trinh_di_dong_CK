package com.example.flashmind.feature.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashmind.core.domain.usecase.ObserveDecksUseCase
import com.example.flashmind.core.domain.usecase.ObservePendingSyncTasksUseCase
import com.example.flashmind.core.domain.usecase.ObserveStudyAnalyticsUseCase
import com.example.flashmind.core.domain.usecase.RefreshDecksUseCase
import com.example.flashmind.core.domain.usecase.SyncPendingTasksUseCase
import com.example.flashmind.core.domain.usecase.DeleteDeckUseCase
import com.example.flashmind.core.model.Deck
import com.example.flashmind.core.model.StudyAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class DeckListViewModel @Inject constructor(
    observeDecksUseCase: ObserveDecksUseCase,
    observePendingSyncTasksUseCase: ObservePendingSyncTasksUseCase,
    observeStudyAnalyticsUseCase: ObserveStudyAnalyticsUseCase,
    private val refreshDecksUseCase: RefreshDecksUseCase,
    private val syncPendingTasksUseCase: SyncPendingTasksUseCase,
    private val deleteDeckUseCase: DeleteDeckUseCase,
) : ViewModel() {

    private var allDecks: List<Deck> = emptyList()
    private val _uiState = MutableStateFlow(DeckListUiState())
    val uiState: StateFlow<DeckListUiState> = _uiState.asStateFlow()

    init {
        observeDecksUseCase()
            .onEach { decks ->
                allDecks = decks
                _uiState.value = _uiState.value.copy(
                    decks = decks.filterAndSort(_uiState.value.searchQuery, _uiState.value.sortOrder),
                    totalDeckCount = decks.size,
                    totalCardCount = decks.sumOf { it.cardCount },
                    totalDueCount = decks.sumOf { it.dueCount },
                )
            }
            .launchIn(viewModelScope)

        observePendingSyncTasksUseCase()
            .onEach { tasks -> _uiState.value = _uiState.value.copy(pendingSyncCount = tasks.size) }
            .launchIn(viewModelScope)

        observeStudyAnalyticsUseCase()
            .onEach { analytics -> _uiState.value = _uiState.value.copy(analytics = analytics) }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            runCatching { syncPendingTasksUseCase() }
            runCatching { refreshDecksUseCase() }
                .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            decks = allDecks.filterAndSort(query, _uiState.value.sortOrder),
        )
    }

    fun onSortOrderChanged(sortOrder: DeckSortOrder) {
        _uiState.value = _uiState.value.copy(
            sortOrder = sortOrder,
            decks = allDecks.filterAndSort(_uiState.value.searchQuery, sortOrder),
        )
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, error = null)
            runCatching { syncPendingTasksUseCase() }
                .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
            runCatching { refreshDecksUseCase() }
                .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            runCatching { deleteDeckUseCase(deckId) }
                .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
        }
    }
}

data class DeckListUiState(
    val decks: List<Deck> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: DeckSortOrder = DeckSortOrder.NAME_ASC,
    val totalDeckCount: Int = 0,
    val totalCardCount: Int = 0,
    val totalDueCount: Int = 0,
    val analytics: StudyAnalytics = StudyAnalytics(),
    val pendingSyncCount: Int = 0,
    val isSyncing: Boolean = false,
    val error: String? = null,
)

enum class DeckSortOrder {
    NAME_ASC,
    NAME_DESC,
}

private fun List<Deck>.filterAndSort(query: String, sortOrder: DeckSortOrder): List<Deck> {
    val normalizedQuery = query.trim()
    val filtered = if (normalizedQuery.isEmpty()) this else filter { deck ->
        deck.title.contains(normalizedQuery, ignoreCase = true) ||
            deck.description.contains(normalizedQuery, ignoreCase = true)
    }
    return when (sortOrder) {
        DeckSortOrder.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
        DeckSortOrder.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
    }
}
