package com.yourname.flashlearn.domain.repository

import com.yourname.flashlearn.domain.model.Deck
import com.yourname.flashlearn.domain.model.Flashcard
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    fun getAllDecks(): Flow<List<Deck>>
    suspend fun getDeckById(id: Long): Deck?
    suspend fun insertDeck(deck: Deck): Long
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(deck: Deck)

    fun getCardsByDeck(deckId: Long): Flow<List<Flashcard>>
    suspend fun getDueCards(deckId: Long, limit: Int = 20): List<Flashcard>
    suspend fun insertCard(card: Flashcard): Long
    suspend fun insertCards(cards: List<Flashcard>)
    suspend fun updateCard(card: Flashcard)
    suspend fun deleteCard(card: Flashcard)

    suspend fun submitReview(cardId: Long, quality: Int)
    fun getTotalDueCount(): Flow<Int>
    fun searchCards(deckId: Long, query: String): Flow<List<Flashcard>>
    suspend fun getCardsByDeckOnce(deckId: Long): List<Flashcard>
}