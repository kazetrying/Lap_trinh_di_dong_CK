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
class LearnModeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCardsUseCase: ObserveCardsUseCase,
) : ViewModel() {

    private val deckId: String = checkNotNull(savedStateHandle["deckId"])
    private val _uiState = MutableStateFlow(LearnModeUiState())
    val uiState: StateFlow<LearnModeUiState> = _uiState.asStateFlow()

    private var allCards: List<VocabularyCard> = emptyList()
    private var cards: List<VocabularyCard> = emptyList()
    private val wrongCardIds = linkedSetOf<String>()
    private var currentIndex = 0
    private var correctCount = 0

    init {
        observeCardsUseCase(deckId)
            .onEach { items ->
                allCards = items
                wrongCardIds.clear()
                rebuildSession(false)
            }
            .launchIn(viewModelScope)
    }

    fun onAnswerChanged(value: String) {
        _uiState.value = _uiState.value.copy(answer = value, feedback = null)
    }

    fun checkAnswer() {
        val card = _uiState.value.currentCard ?: return
        val normalizedAnswer = _uiState.value.answer.trim()
        if (normalizedAnswer.isBlank()) return
        val isCorrect = normalizedAnswer.equals(card.back, ignoreCase = true)
        if (isCorrect) {
            correctCount += 1
        } else {
            wrongCardIds += card.id
        }
        _uiState.value = _uiState.value.copy(
            isAnswerRevealed = true,
            feedback = if (isCorrect) "Dung" else "Dap an dung: ${card.back}",
            correctCount = correctCount,
            wrongCount = wrongCardIds.size,
        )
    }

    fun revealAnswer() {
        val card = _uiState.value.currentCard ?: return
        _uiState.value = _uiState.value.copy(
            isAnswerRevealed = true,
            feedback = "Dap an dung: ${card.back}",
        )
    }

    fun nextCard() {
        currentIndex += 1
        _uiState.value = _uiState.value.copy(
            currentCard = cards.getOrNull(currentIndex),
            answer = "",
            feedback = null,
            isAnswerRevealed = false,
            completedCount = currentIndex.coerceAtMost(cards.size),
            totalCount = cards.size,
            correctCount = correctCount,
            wrongCount = wrongCardIds.size,
        )
    }

    fun restart() {
        rebuildSession(_uiState.value.practiceWrongOnly)
    }

    fun setPracticeWrongOnly(enabled: Boolean) {
        rebuildSession(enabled)
    }

    private fun rebuildSession(practiceWrongOnly: Boolean) {
        cards = if (practiceWrongOnly) {
            allCards.filter { it.id in wrongCardIds }.shuffled()
        } else {
            allCards.shuffled()
        }
        currentIndex = 0
        correctCount = 0
        _uiState.value = LearnModeUiState(
            cards = cards,
            currentCard = cards.firstOrNull(),
            totalCount = cards.size,
            practiceWrongOnly = practiceWrongOnly,
            wrongCount = wrongCardIds.size,
        )
    }
}

data class LearnModeUiState(
    val cards: List<VocabularyCard> = emptyList(),
    val currentCard: VocabularyCard? = null,
    val answer: String = "",
    val feedback: String? = null,
    val isAnswerRevealed: Boolean = false,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val correctCount: Int = 0,
    val practiceWrongOnly: Boolean = false,
    val wrongCount: Int = 0,
)
