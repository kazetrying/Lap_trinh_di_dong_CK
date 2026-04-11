package com.yourname.flashlearn.data.local.dao

import androidx.room.*
import com.yourname.flashlearn.data.local.entity.DeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Query("SELECT * FROM decks ORDER BY updatedAt DESC")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Long): DeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    fun getCardCount(deckId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId AND nextReviewDate <= :now")
    fun getDueCardCount(deckId: Long, now: Long = System.currentTimeMillis()): Flow<Int>
}