package com.yourname.flashlearn.data.local.dao

import androidx.room.*
import com.yourname.flashlearn.data.local.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY id ASC")
    fun getCardsByDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("""
        SELECT * FROM flashcards 
        WHERE deckId = :deckId AND nextReviewDate <= :now 
        ORDER BY nextReviewDate ASC LIMIT :limit
    """)
    suspend fun getDueCards(
        deckId: Long,
        now: Long = System.currentTimeMillis(),
        limit: Int = 20
    ): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE id = :cardId")
    suspend fun getCardById(cardId: Long): FlashcardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Delete
    suspend fun deleteCard(card: FlashcardEntity)

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewDate <= :now")
    fun getTotalDueCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("""
    SELECT * FROM flashcards 
    WHERE deckId = :deckId 
    AND (front LIKE '%' || :query || '%' OR back LIKE '%' || :query || '%')
    ORDER BY id ASC
""")
    fun searchCards(deckId: Long, query: String): Flow<List<FlashcardEntity>>
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY id ASC")
    suspend fun getCardsByDeckOnce(deckId: Long): List<FlashcardEntity>
}