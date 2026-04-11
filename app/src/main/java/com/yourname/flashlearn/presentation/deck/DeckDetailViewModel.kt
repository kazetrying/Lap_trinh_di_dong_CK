package com.yourname.flashlearn.presentation.deck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.flashlearn.domain.model.Flashcard
import com.yourname.flashlearn.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val repository: FlashcardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val deckId: Long = checkNotNull(savedStateHandle["deckId"])
    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val cards: StateFlow<List<Flashcard>> = searchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.getCardsByDeck(deckId)
            else repository.searchCards(deckId, query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) { searchQuery.value = query }

    fun addCard(front: String, back: String) {
        viewModelScope.launch {
            repository.insertCard(Flashcard(deckId = deckId, front = front, back = back))
        }
    }

    fun deleteCard(card: Flashcard) {
        viewModelScope.launch { repository.deleteCard(card) }
    }

    fun updateCard(card: Flashcard) {
        viewModelScope.launch { repository.updateCard(card) }
    }
}