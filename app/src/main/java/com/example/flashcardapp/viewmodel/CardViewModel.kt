package com.example.flashcardapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.flashcardapp.data.*
import com.example.flashcardapp.util.applySmTwo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CardViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CardViewModel"
    }

    private val repo   = CardRepository(AppDatabase.getInstance(application).cardDao())
    private val rtRepo = RealtimeRepository()
    private val dao    = AppDatabase.getInstance(application).cardDao()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    val allDecks: StateFlow<List<Deck>> = repo.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (rtRepo.isLoggedIn()) {
            startRealtimeSync()
        }
    }

    fun startRealtimeSync() {
        viewModelScope.launch {
            rtRepo.observeDecksFromCloud()
                .catch { e -> Log.e(TAG, "observeDecks error: ${e.message}") }
                .collect { cloudDecks ->
                    try {
                        dao.clearAllDecks()
                        cloudDecks.forEach { dao.insertDeck(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Auto-sync decks failed: ${e.message}")
                    }
                }
        }
        viewModelScope.launch {
            rtRepo.observeCardsFromCloud()
                .catch { e -> Log.e(TAG, "observeCards error: ${e.message}") }
                .collect { cloudCards ->
                    try {
                        dao.clearAllCards()
                        cloudCards.forEach { dao.insertCard(it) }
                        _currentDeckId?.let { loadDueCards(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Auto-sync cards failed: ${e.message}")
                    }
                }
        }
    }

    private var _currentDeckId: Long? = null

    fun addDeck(name: String, description: String = "") {
        viewModelScope.launch {
            try {
                val id = repo.insertDeck(Deck(name = name, description = description))
                val deck = dao.getDeckById(id)
                deck?.let { rtRepo.syncDeckToCloud(it) }
            } catch (e: Exception) {}
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            try {
                repo.deleteDeck(deck)
                rtRepo.deleteDeckFromCloud(deck.id)
            } catch (e: Exception) {}
        }
    }

    fun getCardCount(deckId: Long): Flow<Int>    = repo.getCardCount(deckId)
    fun getDueCardCount(deckId: Long): Flow<Int> = repo.getDueCardCount(deckId)
    fun getCardsByDeck(deckId: Long): Flow<List<Card>> = repo.getCardsByDeck(deckId)

    fun addCard(deckId: Long, front: String, back: String) {
        viewModelScope.launch {
            try {
                val card = Card(deckId = deckId, front = front, back = back)
                repo.insertCard(card)
                val inserted = dao.getAllCardsOnce()
                    .filter { it.deckId == deckId && it.front == front && it.back == back }
                    .maxByOrNull { it.id }
                inserted?.let { rtRepo.syncCardToCloud(it) }
            } catch (e: Exception) {}
        }
    }

    fun updateCard(card: Card) {
        viewModelScope.launch {
            try {
                repo.updateCard(card)
                rtRepo.syncCardToCloud(card)
            } catch (e: Exception) {}
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            try {
                repo.deleteCard(card)
                rtRepo.deleteCardFromCloud(card.id)
            } catch (e: Exception) {}
        }
    }

    fun pullFromCloud() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            try {
                rtRepo.pullAllData(dao)
                _syncStatus.value = SyncStatus.Success
                _currentDeckId?.let { loadDueCards(it) }
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error(e.message ?: "Lỗi")
            }
        }
    }

    // ✅ FIX: Xóa sạch Cloud trước khi đẩy dữ liệu máy lên để dọn dẹp "thẻ mồ côi"
    fun pushAllToCloud() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            try {
                rtRepo.clearCloudData() // Xóa sạch rác trên Firebase
                val decks = dao.getAllDecksOnce()
                val cards = dao.getAllCardsOnce()
                decks.forEach { rtRepo.syncDeckToCloud(it) }
                cards.forEach { rtRepo.syncCardToCloud(it) }
                _syncStatus.value = SyncStatus.Success
                Log.d(TAG, "pushAllToCloud: Đã dọn rác và đẩy dữ liệu mới thành công")
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error(e.message ?: "Lỗi")
            }
        }
    }

    private val _dueCards      = MutableStateFlow<List<Card>>(emptyList())
    val dueCards: StateFlow<List<Card>> = _dueCards.asStateFlow()
    private val _currentIndex  = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    private val _studyFinished = MutableStateFlow(false)
    val studyFinished: StateFlow<Boolean> = _studyFinished.asStateFlow()

    val currentCard: StateFlow<Card?> = combine(_dueCards, _currentIndex) { cards, idx ->
        cards.getOrNull(idx)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadDueCards(deckId: Long) {
        _currentDeckId = deckId
        viewModelScope.launch {
            val cards = repo.getDueCards(deckId)
            _dueCards.value      = cards
            _currentIndex.value  = 0
            _studyFinished.value = cards.isEmpty()
        }
    }

    fun rateCard(quality: Int) {
        viewModelScope.launch {
            val card    = currentCard.value ?: return@launch
            val updated = applySmTwo(card, quality)
            repo.updateCard(updated)
            rtRepo.syncCardToCloud(updated)

            if (quality < 3) {
                val currentList = _dueCards.value.toMutableList()
                currentList.removeAt(_currentIndex.value)
                currentList.add(updated)
                _dueCards.value = currentList
            } else {
                val nextIndex = _currentIndex.value + 1
                if (nextIndex < _dueCards.value.size) {
                    _currentIndex.value = nextIndex
                } else {
                    _studyFinished.value = true
                }
            }
        }
    }

    fun resetStudy() {
        _studyFinished.value = false
        _dueCards.value      = emptyList()
        _currentIndex.value  = 0
    }
}

sealed class SyncStatus {
    object Idle    : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

class CardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}