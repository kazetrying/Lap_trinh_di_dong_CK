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
class TestModeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCardsUseCase: ObserveCardsUseCase,
) : ViewModel() {

    private val deckId: String = checkNotNull(savedStateHandle["deckId"])
    private val _uiState = MutableStateFlow(TestModeUiState())
    val uiState: StateFlow<TestModeUiState> = _uiState.asStateFlow()

    private var allCards: List<VocabularyCard> = emptyList()
    private var questions: List<TestQuestion> = emptyList()
    private val wrongCardIds = linkedSetOf<String>()
    private var currentIndex = 0
    private var score = 0

    init {
        observeCardsUseCase(deckId)
            .onEach { cards ->
                allCards = cards
                wrongCardIds.clear()
                rebuildSession(false)
            }
            .launchIn(viewModelScope)
    }

    fun submitAnswer(selectedAnswer: String) {
        val question = _uiState.value.currentQuestion ?: return
        val isCorrect = selectedAnswer == question.correctAnswer
        if (isCorrect) score += 1
        if (!isCorrect) wrongCardIds += question.cardId
        _uiState.value = _uiState.value.copy(
            selectedAnswer = selectedAnswer,
            answerState = if (isCorrect) AnswerState.CORRECT else AnswerState.WRONG,
            score = score,
            wrongCount = wrongCardIds.size,
        )
    }

    fun nextQuestion() {
        if (_uiState.value.currentQuestion == null) return
        currentIndex += 1
        _uiState.value = _uiState.value.copy(
            currentQuestion = questions.getOrNull(currentIndex),
            currentQuestionNumber = if (questions.getOrNull(currentIndex) == null) 0 else currentIndex + 1,
            selectedAnswer = null,
            answerState = AnswerState.IDLE,
            score = score,
        )
    }

    fun restart() {
        rebuildSession(_uiState.value.practiceWrongOnly)
    }

    fun setPracticeWrongOnly(enabled: Boolean) {
        rebuildSession(enabled)
    }

    private fun rebuildSession(practiceWrongOnly: Boolean) {
        currentIndex = 0
        score = 0
        val source = if (practiceWrongOnly) allCards.filter { it.id in wrongCardIds } else allCards
        questions = source.toQuestions()
        _uiState.value = TestModeUiState(
            questions = questions,
            currentQuestion = questions.firstOrNull(),
            currentQuestionNumber = if (questions.isEmpty()) 0 else 1,
            score = score,
            practiceWrongOnly = practiceWrongOnly,
            wrongCount = wrongCardIds.size,
        )
    }
}

data class TestModeUiState(
    val questions: List<TestQuestion> = emptyList(),
    val currentQuestion: TestQuestion? = null,
    val currentQuestionNumber: Int = 0,
    val selectedAnswer: String? = null,
    val answerState: AnswerState = AnswerState.IDLE,
    val score: Int = 0,
    val practiceWrongOnly: Boolean = false,
    val wrongCount: Int = 0,
)

data class TestQuestion(
    val cardId: String,
    val prompt: String,
    val correctAnswer: String,
    val options: List<String>,
)

enum class AnswerState {
    IDLE,
    CORRECT,
    WRONG,
}

private fun List<VocabularyCard>.toQuestions(): List<TestQuestion> {
    if (size < 2) return emptyList()
    val shuffledCards = shuffled()
    return shuffledCards.map { card ->
        val wrongAnswers = shuffledCards
            .filter { it.id != card.id }
            .map { it.back }
            .distinct()
            .shuffled()
            .take(3)
        TestQuestion(
            cardId = card.id,
            prompt = card.front,
            correctAnswer = card.back,
            options = (wrongAnswers + card.back).shuffled(),
        )
    }
}
