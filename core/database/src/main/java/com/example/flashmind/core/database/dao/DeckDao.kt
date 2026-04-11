package com.example.flashmind.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.flashmind.core.database.entity.CardEntity
import com.example.flashmind.core.database.entity.DeckEntity
import com.example.flashmind.core.database.entity.PendingSyncEntity
import com.example.flashmind.core.database.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query(
        """
        SELECT d.id, d.title, d.description,
        COUNT(CASE WHEN c.nextReviewAt <= :nowEpochMillis THEN 1 END) AS dueCount,
        COUNT(c.id) AS cardCount
        FROM decks d
        LEFT JOIN cards c ON c.deckId = d.id
        GROUP BY d.id
        ORDER BY d.title
        """,
    )
    fun observeDeckSummaries(nowEpochMillis: Long): Flow<List<DeckSummaryRow>>

    @Query(
        """
        SELECT d.id, d.title, d.description,
        COUNT(CASE WHEN c.nextReviewAt <= :nowEpochMillis THEN 1 END) AS dueCount,
        COUNT(c.id) AS cardCount
        FROM decks d
        LEFT JOIN cards c ON c.deckId = d.id
        WHERE d.id = :deckId
        GROUP BY d.id
        LIMIT 1
        """,
    )
    fun observeDeck(deckId: String, nowEpochMillis: Long): Flow<DeckSummaryRow?>

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY front")
    fun observeCards(deckId: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND nextReviewAt <= :nowEpochMillis ORDER BY nextReviewAt")
    fun observeDueCards(deckId: String, nowEpochMillis: Long): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDecks(decks: List<DeckEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCards(cards: List<CardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPendingSyncTasks(tasks: List<PendingSyncEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewHistory(entry: ReviewHistoryEntity)

    @Query("SELECT * FROM pending_sync_tasks ORDER BY createdAt")
    fun observePendingSyncTasks(): Flow<List<PendingSyncEntity>>

    @Query("SELECT * FROM review_history ORDER BY reviewedAt DESC")
    fun observeReviewHistory(): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT * FROM pending_sync_tasks ORDER BY createdAt")
    suspend fun getPendingSyncTasks(): List<PendingSyncEntity>

    @Query("DELETE FROM pending_sync_tasks WHERE id = :taskId")
    suspend fun deletePendingSyncTask(taskId: String)

    @Query("UPDATE cards SET isStarred = :isStarred WHERE id = :cardId")
    suspend fun updateCardStar(cardId: String, isStarred: Boolean)

    @Query("UPDATE cards SET repetition = :repetition, intervalDays = :intervalDays, easeFactor = :easeFactor, nextReviewAt = :nextReviewAt, lastReviewedAt = :lastReviewedAt WHERE id = :cardId")
    suspend fun updateProgress(
        cardId: String,
        repetition: Int,
        intervalDays: Int,
        easeFactor: Double,
        nextReviewAt: Long,
        lastReviewedAt: Long?,
    )

    @Query("SELECT * FROM cards WHERE id = :cardId LIMIT 1")
    suspend fun getCard(cardId: String): CardEntity?

    @Query("SELECT * FROM decks WHERE id = :deckId LIMIT 1")
    suspend fun getDeckEntity(deckId: String): DeckEntity?

    @Query("DELETE FROM cards WHERE id = :cardId")
    suspend fun deleteCard(cardId: String)

    @Query("DELETE FROM cards WHERE deckId = :deckId")
    suspend fun deleteCardsByDeck(deckId: String)

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeck(deckId: String)

    @Query("SELECT COUNT(*) FROM cards WHERE nextReviewAt <= :nowEpochMillis")
    suspend fun countDueCards(nowEpochMillis: Long): Int

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun countDecks(): Int

    @Query("DELETE FROM decks")
    suspend fun clearDecks()

    @Query("DELETE FROM cards")
    suspend fun clearCards()

    @Transaction
    suspend fun replaceAll(decks: List<DeckEntity>, cards: List<CardEntity>) {
        clearCards()
        clearDecks()
        upsertDecks(decks)
        upsertCards(cards)
    }

    @Transaction
    suspend fun deleteDeckWithCards(deckId: String) {
        deleteCardsByDeck(deckId)
        deleteDeck(deckId)
    }
}

data class DeckSummaryRow(
    val id: String,
    val title: String,
    val description: String,
    val dueCount: Int,
    val cardCount: Int,
)
