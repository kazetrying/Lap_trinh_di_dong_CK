package com.yourname.flashlearn.presentation.study

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.flashlearn.domain.model.Flashcard
import com.yourname.flashlearn.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudyUiState(
    val currentCard: Flashcard? = null,
    val isFlipped: Boolean = false,
    val studiedCards: Int = 0,
    val totalCards: Int = 0,
    val progress: Int = 0,
    val isFinished: Boolean = false,
    val again: Int = 0,
    val good: Int = 0,
    val quizChoices: List<String> = emptyList(),
    val correctAnswer: String = ""
)

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val repository: FlashcardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val cards = mutableListOf<Flashcard>()
    private var currentIndex = 0
    private var timerJob: Job? = null

    init { loadCards() }

    private fun loadCards() {
        viewModelScope.launch {
            // Lấy thẻ đến hạn trước
            var due = repository.getDueCards(deckId)

            // Nếu không có thẻ đến hạn → lấy TẤT CẢ thẻ trong deck
            if (due.isEmpty()) {
                due = repository.getCardsByDeckOnce(deckId)
            }

            cards.addAll(due)
            if (cards.isNotEmpty()) {
                _uiState.value = StudyUiState(
                    currentCard = cards.first(),
                    totalCards = cards.size
                )
                generateQuizChoices(cards.first())
                startTimer()
            } else {
                _uiState.value = StudyUiState(isFinished = true)
            }
        }
    }

    private fun generateQuizChoices(currentCard: Flashcard) {
        val correct = currentCard.back
        val others = cards
            .filter { it.id != currentCard.id }
            .map { it.back }
            .shuffled()
            .take(3)

        // Nếu không đủ 3 đáp án khác → dùng đáp án giả
        val fakeAnswers = listOf("không biết", "từ khác", "từ vựng")
        val wrongAnswers = if (others.size < 3) {
            (others + fakeAnswers).take(3)
        } else others

        val choices = (wrongAnswers + correct).shuffled()
        _uiState.value = _uiState.value.copy(
            quizChoices = choices,
            correctAnswer = correct
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        _elapsedSeconds.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value++
            }
        }
    }

    private fun stopTimer() { timerJob?.cancel() }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(
            isFlipped = !_uiState.value.isFlipped,
            quizChoices = emptyList() // Ẩn quiz khi lật
        )
    }

    fun submitAnswer(quality: Int) {
        val card = _uiState.value.currentCard ?: return
        stopTimer()
        viewModelScope.launch {
            repository.submitReview(card.id, quality)
            val studied = _uiState.value.studiedCards + 1
            val total = _uiState.value.totalCards
            val newAgain = if (quality < 3) _uiState.value.again + 1 else _uiState.value.again
            val newGood = if (quality >= 3) _uiState.value.good + 1 else _uiState.value.good
            currentIndex++
            if (currentIndex >= cards.size) {
                _uiState.value = _uiState.value.copy(
                    isFinished = true,
                    studiedCards = studied,
                    again = newAgain,
                    good = newGood
                )
            } else {
                val nextCard = cards[currentIndex]
                _uiState.value = _uiState.value.copy(
                    currentCard = nextCard,
                    isFlipped = false,
                    studiedCards = studied,
                    progress = studied * 100 / total,
                    again = newAgain,
                    good = newGood
                )
                generateQuizChoices(nextCard)
                startTimer()
            }
        }
    }
}