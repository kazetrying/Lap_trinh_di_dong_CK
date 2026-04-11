package com.example.flashmind.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashmind.core.domain.usecase.ObserveDueCardsUseCase
import com.example.flashmind.core.domain.usecase.RefreshDueCardsUseCase
import com.example.flashmind.core.domain.usecase.SubmitReviewUseCase
import com.example.flashmind.core.model.ReviewGrade
import com.example.flashmind.core.model.VocabularyCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class ReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDueCardsUseCase: ObserveDueCardsUseCase,
    private val refreshDueCardsUseCase: RefreshDueCardsUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase,
) : ViewModel() {

    private val deckId: String = checkNotNull(savedStateHandle["deckId"])
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()
    private var currentIndex = 0
    private var sessionTotal = 0
    private var reviewedCount = 0

    init {
        observeDueCardsUseCase(deckId)
            .onEach { cards ->
                currentIndex = currentIndex.coerceAtMost((cards.size - 1).coerceAtLeast(0))
                if (cards.isNotEmpty() && sessionTotal == 0) {
                    sessionTotal = cards.size
                }
                _uiState.value = _uiState.value.copy(
                    cards = cards,
                    currentCard = cards.getOrNull(currentIndex),
                    revealAnswer = false,
                    reviewedCount = reviewedCount,
                    sessionTotal = sessionTotal,
                )
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            runCatching { refreshDueCardsUseCase(deckId) }
                .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
        }
    }

    fun submitGrade(grade: ReviewGrade) {
        val currentCard = _uiState.value.currentCard ?: return
        viewModelScope.launch {
            runCatching { submitReviewUseCase(currentCard.id, grade) }
                .onSuccess {
                    reviewedCount += 1
                    val cards = _uiState.value.cards
                    _uiState.value = _uiState.value.copy(
                        currentCard = cards.getOrNull(currentIndex),
                        revealAnswer = false,
                        reviewedCount = reviewedCount,
                        sessionTotal = maxOf(sessionTotal, reviewedCount + cards.size),
                    )
                }
                .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
        }
    }

    fun revealAnswer() {
        _uiState.value = _uiState.value.copy(revealAnswer = true)
    }
}

data class ReviewUiState(
    val cards: List<VocabularyCard> = emptyList(),
    val currentCard: VocabularyCard? = null,
    val revealAnswer: Boolean = false,
    val reviewedCount: Int = 0,
    val sessionTotal: Int = 0,
    val error: String? = null,
)
