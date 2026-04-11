package com.yourname.flashlearn.data.repository

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import com.yourname.flashlearn.data.local.dao.DeckDao
import com.yourname.flashlearn.data.local.dao.FlashcardDao
import com.yourname.flashlearn.data.local.entity.DeckEntity
import com.yourname.flashlearn.data.local.entity.FlashcardEntity
import com.yourname.flashlearn.domain.model.Deck
import com.yourname.flashlearn.domain.model.Flashcard
import com.yourname.flashlearn.domain.repository.FlashcardRepository
import com.yourname.flashlearn.domain.usecase.SM2Algorithm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FlashcardRepositoryImpl @Inject constructor(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao
) : FlashcardRepository {

    override fun searchCards(deckId: Long, query: String): Flow<List<Flashcard>> =
        flashcardDao.searchCards(deckId, query).map { list -> list.map { it.toDomain() } }

    override fun getAllDecks(): Flow<List<Deck>> =
        deckDao.getAllDecks().flatMapLatest { decks ->
            if (decks.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    decks.map { deck ->
                        combine(
                            deckDao.getCardCount(deck.id),
                            deckDao.getDueCardCount(deck.id)
                        ) { cardCount, dueCount ->
                            deck.toDomain(cardCount = cardCount, dueCount = dueCount)
                        }
                    }
                ) { it.toList() }
            }
        }

    override suspend fun getDeckById(id: Long): Deck? =
        deckDao.getDeckById(id)?.toDomain()

    override suspend fun insertDeck(deck: Deck): Long =
        deckDao.insertDeck(deck.toEntity())

    override suspend fun updateDeck(deck: Deck) =
        deckDao.updateDeck(deck.toEntity())

    override suspend fun deleteDeck(deck: Deck) =
        deckDao.deleteDeck(deck.toEntity())

    override fun getCardsByDeck(deckId: Long): Flow<List<Flashcard>> =
        flashcardDao.getCardsByDeck(deckId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDueCards(deckId: Long, limit: Int): List<Flashcard> =
        flashcardDao.getDueCards(deckId, limit = limit).map { it.toDomain() }

    // ← THÊM: Lấy tất cả thẻ không phụ thuộc ngày
    override suspend fun getCardsByDeckOnce(deckId: Long): List<Flashcard> =
        flashcardDao.getCardsByDeckOnce(deckId).map { it.toDomain() }

    override suspend fun insertCard(card: Flashcard): Long =
        flashcardDao.insertCard(card.toEntity())

    override suspend fun insertCards(cards: List<Flashcard>) =
        flashcardDao.insertCards(cards.map { it.toEntity() })

    override suspend fun updateCard(card: Flashcard) =
        flashcardDao.updateCard(card.toEntity())

    override suspend fun deleteCard(card: Flashcard) =
        flashcardDao.deleteCard(card.toEntity())

    override suspend fun submitReview(cardId: Long, quality: Int) {
        val card = flashcardDao.getCardById(cardId) ?: return
        val updated = SM2Algorithm.applyReviewResult(card, quality)
        flashcardDao.updateCard(updated)
    }

    override fun getTotalDueCount(): Flow<Int> =
        flashcardDao.getTotalDueCount()

    // --- Mappers ---
    private fun DeckEntity.toDomain(
        cardCount: Int = 0,
        dueCount: Int = 0
    ) = Deck(
        id = id, title = title, description = description,
        coverColor = coverColor, language = language,
        cardCount = cardCount, dueCount = dueCount,
        createdAt = createdAt
    )

    private fun Deck.toEntity() = DeckEntity(
        id = id, title = title, description = description,
        coverColor = coverColor, language = language, createdAt = createdAt
    )

    private fun FlashcardEntity.toDomain() = Flashcard(
        id = id, deckId = deckId, front = front, back = back,
        repetitions = repetitions, easeFactor = easeFactor,
        interval = interval, nextReviewDate = nextReviewDate,
        totalReviews = totalReviews, correctReviews = correctReviews
    )

    private fun Flashcard.toEntity() = FlashcardEntity(
        id = id, deckId = deckId, front = front, back = back,
        repetitions = repetitions, easeFactor = easeFactor,
        interval = interval, nextReviewDate = nextReviewDate,
        totalReviews = totalReviews, correctReviews = correctReviews
    )
}