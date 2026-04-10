package com.example.flashcardapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    // ── Deck ──────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck): Long

    @Delete
    suspend fun deleteDeck(deck: Deck)

    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<Deck>>

    // ✅ FIX: Thêm hàm suspend để lấy tất cả deck 1 lần (dùng khi push lên cloud)
    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    suspend fun getAllDecksOnce(): List<Deck>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: Long): Deck?

    // ── Card ──────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY createdAt DESC")
    fun getCardsByDeck(deckId: Long): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND nextReviewDate <= :now ORDER BY nextReviewDate ASC")
    suspend fun getDueCards(deckId: Long, now: Long = System.currentTimeMillis()): List<Card>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    fun getCardCount(deckId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND nextReviewDate <= :now")
    fun getDueCardCount(deckId: Long, now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE nextReviewDate <= :now")
    suspend fun getAllDueCardsCount(now: Long): Int

    // ✅ FIX: Thêm hàm lấy tất cả card 1 lần (dùng khi push lên cloud)
    @Query("SELECT * FROM cards ORDER BY createdAt DESC")
    suspend fun getAllCardsOnce(): List<Card>

    // ── Xóa theo ID (dùng khi sync realtime phát hiện xóa từ cloud) ──
    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: Long)

    @Query("DELETE FROM cards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: Long)

    @Query("DELETE FROM cards WHERE deckId = :deckId")
    suspend fun deleteCardsByDeckId(deckId: Long)

    // ── Thay toàn bộ dữ liệu local bằng danh sách từ cloud ──
    @Query("DELETE FROM decks")
    suspend fun clearAllDecks()

    @Query("DELETE FROM cards")
    suspend fun clearAllCards()
}