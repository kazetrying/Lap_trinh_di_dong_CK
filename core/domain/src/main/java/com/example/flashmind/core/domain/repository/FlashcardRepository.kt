package com.example.flashmind.core.domain.repository

import com.example.flashmind.core.model.Deck
import com.example.flashmind.core.model.ImportCardDraft
import com.example.flashmind.core.model.PendingSyncTask
import com.example.flashmind.core.model.ReviewGrade
import com.example.flashmind.core.model.StudyAnalytics
import com.example.flashmind.core.model.VocabularyCard
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    fun observeDecks(): Flow<List<Deck>>
    fun observeDeck(deckId: String): Flow<Deck?>
    fun observeCards(deckId: String): Flow<List<VocabularyCard>>
    fun observeDueCards(deckId: String): Flow<List<VocabularyCard>>
    fun observePendingSyncTasks(): Flow<List<PendingSyncTask>>
    fun observeStudyAnalytics(): Flow<StudyAnalytics>
    suspend fun createDeck(title: String, description: String)
    suspend fun updateDeck(deckId: String, title: String, description: String)
    suspend fun importDeck(title: String, description: String, cards: List<ImportCardDraft>)
    suspend fun deleteDeck(deckId: String)
    suspend fun createCard(
        deckId: String,
        front: String,
        back: String,
        pronunciation: String?,
        exampleSentence: String?,
        imageUrl: String?,
    )
    suspend fun updateCard(
        cardId: String,
        front: String,
        back: String,
        pronunciation: String?,
        exampleSentence: String?,
        imageUrl: String?,
    )
    suspend fun toggleCardStar(cardId: String)
    suspend fun deleteCard(cardId: String)
    suspend fun submitReview(cardId: String, grade: ReviewGrade)
    suspend fun refreshDecks()
    suspend fun refreshDueCards(deckId: String)
    suspend fun countDueCards(): Int
    suspend fun syncPendingTasks()
}
