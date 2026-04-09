package com.example.flashcardapp.data

import kotlinx.coroutines.flow.Flow

class CardRepository(private val dao: CardDao) {

    // ── Deck ──────────────────────────────────────────
    val allDecks: Flow<List<Deck>> = dao.getAllDecks()

    suspend fun insertDeck(deck: Deck): Long = dao.insertDeck(deck)

    suspend fun deleteDeck(deck: Deck) = dao.deleteDeck(deck)

    suspend fun getDeckById(id: Long): Deck? = dao.getDeckById(id)

    // ── Card ──────────────────────────────────────────
    fun getCardsByDeck(deckId: Long): Flow<List<Card>> = dao.getCardsByDeck(deckId)

    fun getCardCount(deckId: Long): Flow<Int> = dao.getCardCount(deckId)

    // ✅ Cập nhật: Truyền thêm thời gian hiện tại
    fun getDueCardCount(deckId: Long): Flow<Int> = dao.getDueCardCount(deckId, System.currentTimeMillis())

    suspend fun insertCard(card: Card) = dao.insertCard(card)

    suspend fun updateCard(card: Card) = dao.updateCard(card)

    suspend fun deleteCard(card: Card) = dao.deleteCard(card)

    // ✅ Cập nhật: Truyền thêm thời gian hiện tại
    suspend fun getDueCards(deckId: Long): List<Card> = dao.getDueCards(deckId, System.currentTimeMillis())
}