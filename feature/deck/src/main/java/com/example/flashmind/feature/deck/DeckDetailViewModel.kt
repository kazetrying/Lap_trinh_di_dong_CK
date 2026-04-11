package com.example.flashmind.feature.deck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashmind.core.domain.studycoach.OnDeviceStudyCoach
import com.example.flashmind.core.domain.usecase.CreateCardUseCase
import com.example.flashmind.core.domain.usecase.DeleteCardUseCase
import com.example.flashmind.core.domain.usecase.DeleteDeckUseCase
import com.example.flashmind.core.domain.usecase.ObserveCardsUseCase
import com.example.flashmind.core.domain.usecase.ObserveDeckUseCase
import com.example.flashmind.core.domain.usecase.ToggleCardStarUseCase
import com.example.flashmind.core.domain.usecase.UpdateDeckUseCase
import com.example.flashmind.core.domain.usecase.UpdateCardUseCase
import com.example.flashmind.core.model.Deck
import com.example.flashmind.core.model.StudyCoachSnapshot
import com.example.flashmind.core.model.VocabularyCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDeckUseCase: ObserveDeckUseCase,
    observeCardsUseCase: ObserveCardsUseCase,
    private val createCardUseCase: CreateCardUseCase,
    private val updateCardUseCase: UpdateCardUseCase,
    private val updateDeckUseCase: UpdateDeckUseCase,
    private val toggleCardStarUseCase: ToggleCardStarUseCase,
    private val deleteCardUseCase: DeleteCardUseCase,
    private val deleteDeckUseCase: DeleteDeckUseCase,
    private val onDeviceStudyCoach: OnDeviceStudyCoach,
) : ViewModel() {

    private val deckId: String = checkNotNull(savedStateHandle["deckId"])
    private var allCards: List<VocabularyCard> = emptyList()
    private val _uiState = MutableStateFlow(DeckDetailUiState())
    val uiState: StateFlow<DeckDetailUiState> = _uiState.asStateFlow()

    init {
        combine(
            observeDeckUseCase(deckId),
            observeCardsUseCase(deckId),
        ) { deck, cards ->
            deck to cards
        }.onEach { (deck, cards) ->
            allCards = cards
            val filteredCards = cards.filterCards(
                query = _uiState.value.cardSearchQuery,
                cardFilter = _uiState.value.cardFilter,
            )
            _uiState.value = _uiState.value.copy(
                deck = deck,
                cards = filteredCards,
                deckTitle = if (_uiState.value.isDeckEditorVisible) _uiState.value.deckTitle else deck?.title.orEmpty(),
                deckDescription = if (_uiState.value.isDeckEditorVisible) _uiState.value.deckDescription else deck?.description.orEmpty(),
                isEditing = _uiState.value.editingCardId != null,
                coachSnapshot = onDeviceStudyCoach.analyze(deck = deck, cards = cards),
            )
        }.launchIn(viewModelScope)
    }

    fun onDeckTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(deckTitle = value)
    }

    fun onDeckDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(deckDescription = value)
    }

    fun onFrontChanged(value: String) {
        _uiState.value = _uiState.value.copy(front = value)
    }

    fun onBackChanged(value: String) {
        _uiState.value = _uiState.value.copy(back = value)
    }

    fun onPronunciationChanged(value: String) {
        _uiState.value = _uiState.value.copy(pronunciation = value)
    }

    fun onExampleChanged(value: String) {
        _uiState.value = _uiState.value.copy(exampleSentence = value)
    }

    fun onImageUrlChanged(value: String) {
        _uiState.value = _uiState.value.copy(imageUrl = value)
    }

    fun onCardSearchQueryChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            cardSearchQuery = value,
            cards = allCards.filterCards(
                query = value,
                cardFilter = _uiState.value.cardFilter,
            ),
        )
    }

    fun onCardFilterChanged(filter: CardFilter) {
        _uiState.value = _uiState.value.copy(
            cardFilter = filter,
            cards = allCards.filterCards(
                query = _uiState.value.cardSearchQuery,
                cardFilter = filter,
            ),
        )
    }

    fun startCreateCard() {
        _uiState.value = _uiState.value.copy(
            editingCardId = null,
            front = "",
            back = "",
            pronunciation = "",
            exampleSentence = "",
            imageUrl = "",
            error = null,
            isEditorVisible = true,
            isEditing = false,
        )
    }

    fun startEditCard(card: VocabularyCard) {
        _uiState.value = _uiState.value.copy(
            editingCardId = card.id,
            front = card.front,
            back = card.back,
            pronunciation = card.pronunciation.orEmpty(),
            exampleSentence = card.exampleSentence.orEmpty(),
            imageUrl = card.imageUrl.orEmpty(),
            error = null,
            isEditorVisible = true,
            isEditing = true,
        )
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(
            isEditorVisible = false,
            error = null,
        )
    }

    fun startEditDeck() {
        val deck = _uiState.value.deck ?: return
        _uiState.value = _uiState.value.copy(
            deckTitle = deck.title,
            deckDescription = deck.description,
            isDeckEditorVisible = true,
            error = null,
        )
    }

    fun dismissDeckEditor() {
        _uiState.value = _uiState.value.copy(
            isDeckEditorVisible = false,
            error = null,
        )
    }

    fun saveDeck() {
        val state = _uiState.value
        if (state.deckTitle.isBlank()) {
            _uiState.value = state.copy(error = "Tên bộ thẻ là bắt buộc.")
            return
        }
        viewModelScope.launch {
            runCatching {
                updateDeckUseCase(
                    deckId = deckId,
                    title = state.deckTitle.trim(),
                    description = state.deckDescription.trim(),
                )
            }.onSuccess {
                dismissDeckEditor()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    fun saveCard() {
        val state = _uiState.value
        if (state.front.isBlank() || state.back.isBlank()) {
            _uiState.value = state.copy(error = "Mặt trước và mặt sau là bắt buộc.")
            return
        }
        viewModelScope.launch {
            runCatching {
                val pronunciation = state.pronunciation.ifBlank { null }
                val exampleSentence = state.exampleSentence.ifBlank { null }
                val imageUrl = state.imageUrl.ifBlank { null }
                if (state.editingCardId == null) {
                    createCardUseCase(
                        deckId = deckId,
                        front = state.front.trim(),
                        back = state.back.trim(),
                        pronunciation = pronunciation,
                        exampleSentence = exampleSentence,
                        imageUrl = imageUrl,
                    )
                } else {
                    updateCardUseCase(
                        cardId = state.editingCardId,
                        front = state.front.trim(),
                        back = state.back.trim(),
                        pronunciation = pronunciation,
                        exampleSentence = exampleSentence,
                        imageUrl = imageUrl,
                    )
                }
            }.onSuccess {
                dismissEditor()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            runCatching { deleteCardUseCase(cardId) }
                .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
        }
    }

    fun toggleCardStar(cardId: String) {
        viewModelScope.launch {
            runCatching { toggleCardStarUseCase(cardId) }
                .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
        }
    }

    fun deleteDeck(onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { deleteDeckUseCase(deckId) }
                .onSuccess { onDone() }
                .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
        }
    }
}

data class DeckDetailUiState(
    val deck: Deck? = null,
    val cards: List<VocabularyCard> = emptyList(),
    val coachSnapshot: StudyCoachSnapshot = StudyCoachSnapshot(),
    val cardSearchQuery: String = "",
    val cardFilter: CardFilter = CardFilter.ALL,
    val deckTitle: String = "",
    val deckDescription: String = "",
    val front: String = "",
    val back: String = "",
    val pronunciation: String = "",
    val exampleSentence: String = "",
    val imageUrl: String = "",
    val editingCardId: String? = null,
    val isDeckEditorVisible: Boolean = false,
    val isEditorVisible: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null,
)

enum class CardFilter {
    ALL,
    DUE,
    STARRED,
}

private fun List<VocabularyCard>.filterCards(
    query: String,
    cardFilter: CardFilter,
): List<VocabularyCard> {
    val normalizedQuery = query.trim()
    return this.filter { card ->
        val matchesFilter = when (cardFilter) {
            CardFilter.ALL -> true
            CardFilter.DUE -> !card.progress.nextReviewAt.isAfter(java.time.Instant.now())
            CardFilter.STARRED -> card.isStarred
        }
        val matchesQuery = normalizedQuery.isEmpty() ||
            card.front.contains(normalizedQuery, ignoreCase = true) ||
            card.back.contains(normalizedQuery, ignoreCase = true) ||
            card.pronunciation.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            card.exampleSentence.orEmpty().contains(normalizedQuery, ignoreCase = true)
        matchesFilter && matchesQuery
    }
}
