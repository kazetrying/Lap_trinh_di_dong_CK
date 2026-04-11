package com.example.flashmind.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashmind.core.domain.usecase.ObserveCardsUseCase
import com.example.flashmind.core.model.VocabularyCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class MatchModeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCardsUseCase: ObserveCardsUseCase,
) : ViewModel() {

    private val deckId: String = checkNotNull(savedStateHandle["deckId"])
    private val _uiState = MutableStateFlow(MatchModeUiState())
    val uiState: StateFlow<MatchModeUiState> = _uiState.asStateFlow()

    private var pairs: List<MatchPair> = emptyList()
    private var selectedPromptId: String? = null
    private var selectedAnswerId: String? = null

    init {
        observeCardsUseCase(deckId)
            .onEach { cards ->
                pairs = cards.take(8).map { MatchPair(id = it.id, prompt = it.front, answer = it.back) }
                resetState()
            }
            .launchIn(viewModelScope)
    }

    fun selectPrompt(id: String) {
        selectedPromptId = id
        evaluate()
    }

    fun selectAnswer(id: String) {
        selectedAnswerId = id
        evaluate()
    }

    fun restart() {
        pairs = pairs.shuffled()
        resetState()
    }

    private fun evaluate() {
        val promptId = selectedPromptId ?: run {
            publishState()
            return
        }
        val answerId = selectedAnswerId ?: run {
            publishState()
            return
        }
        val matched = promptId == answerId
        val currentMatched = if (matched) _uiState.value.matchedIds + promptId else _uiState.value.matchedIds
        selectedPromptId = null
        selectedAnswerId = null
        _uiState.value = _uiState.value.copy(
            pairs = pairs,
            matchedIds = currentMatched,
            selectedPromptId = null,
            selectedAnswerId = null,
            lastResult = if (matched) "Matched" else "Try again",
        )
    }

    private fun resetState() {
        selectedPromptId = null
        selectedAnswerId = null
        _uiState.value = MatchModeUiState(pairs = pairs)
    }

    private fun publishState() {
        _uiState.value = _uiState.value.copy(
            pairs = pairs,
            selectedPromptId = selectedPromptId,
            selectedAnswerId = selectedAnswerId,
        )
    }
}

data class MatchModeUiState(
    val pairs: List<MatchPair> = emptyList(),
    val matchedIds: Set<String> = emptySet(),
    val selectedPromptId: String? = null,
    val selectedAnswerId: String? = null,
    val lastResult: String? = null,
) {
    val remainingPairs: List<MatchPair>
        get() = pairs.filterNot { matchedIds.contains(it.id) }
}

data class MatchPair(
    val id: String,
    val prompt: String,
    val answer: String,
)
