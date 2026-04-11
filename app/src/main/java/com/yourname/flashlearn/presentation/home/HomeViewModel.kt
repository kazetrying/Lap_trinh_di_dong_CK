package com.yourname.flashlearn.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.flashlearn.domain.model.Deck
import com.yourname.flashlearn.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    val decks: StateFlow<List<Deck>> = repository.getAllDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDueCount: StateFlow<Int> = repository.getTotalDueCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun createDeck(title: String, description: String, color: String = "#4CAF50") {
        viewModelScope.launch {
            repository.insertDeck(Deck(title = title, description = description, coverColor = color))
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch { repository.deleteDeck(deck) }
    }
}