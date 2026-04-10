package com.example.flashcardapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RealtimeRepository {

    companion object {
        private const val TAG = "RealtimeRepository"
        private const val DATABASE_URL =
            "https://flashcardapp-227a0-default-rtdb.asia-southeast1.firebasedatabase.app" 
    }

    private val db   = FirebaseDatabase.getInstance(DATABASE_URL).reference
    private val auth = FirebaseAuth.getInstance()

    private val userId get() = auth.currentUser?.uid ?: ""

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun syncDeckToCloud(deck: Deck) {
        if (userId.isBlank()) return
        try {
            db.child("users").child(userId).child("decks")
                .child(deck.id.toString())
                .setValue(deck.toMap())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "syncDeckToCloud FAILED: ${e.message}")
        }
    }

    // ✅ FIX: Sửa query xóa card khi xóa deck (Dùng Long thay vì Double)
    suspend fun deleteDeckFromCloud(deckId: Long) {
        if (userId.isBlank()) return
        try {
            // 1. Xóa bộ thẻ
            db.child("users").child(userId).child("decks")
                .child(deckId.toString())
                .removeValue().await()
            
            // 2. Tìm và xóa tất cả thẻ thuộc bộ thẻ này
            val snapshot = db.child("users").child(userId).child("cards")
                .get().await()
            
            snapshot.children.forEach { child ->
                val cDeckId = child.child("deckId").value as? Long
                if (cDeckId == deckId) {
                    child.ref.removeValue().await()
                }
            }
            Log.d(TAG, "deleteDeckFromCloud: Đã xóa deck $deckId và các thẻ liên quan")
        } catch (e: Exception) {
            Log.e(TAG, "deleteDeckFromCloud FAILED: ${e.message}")
        }
    }

    suspend fun getDecksFromCloud(): List<Deck> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snapshot = db.child("users").child(userId).child("decks").get().await()
            snapshot.children.mapNotNull { child ->
                val map = child.value as? Map<*, *> ?: return@mapNotNull null
                Deck(
                    id          = (map["id"] as? Long) ?: 0L,
                    name        = (map["name"] as? String) ?: "",
                    description = (map["description"] as? String) ?: "",
                    createdAt   = (map["createdAt"] as? Long) ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun observeDecksFromCloud(): Flow<List<Deck>> = callbackFlow {
        if (userId.isBlank()) { trySend(emptyList()); close(); return@callbackFlow }
        val ref = db.child("users").child(userId).child("decks")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val decks = snapshot.children.mapNotNull { child ->
                    val map = child.value as? Map<*, *> ?: return@mapNotNull null
                    Deck(
                        id          = (map["id"] as? Long) ?: 0L,
                        name        = (map["name"] as? String) ?: "",
                        description = (map["description"] as? String) ?: "",
                        createdAt   = (map["createdAt"] as? Long) ?: 0L
                    )
                }
                trySend(decks)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeCardsFromCloud(): Flow<List<Card>> = callbackFlow {
        if (userId.isBlank()) { trySend(emptyList()); close(); return@callbackFlow }
        val ref = db.child("users").child(userId).child("cards")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cards = snapshot.children.mapNotNull { child ->
                    val map = child.value as? Map<*, *> ?: return@mapNotNull null
                    Card(
                        id             = (map["id"] as? Long) ?: 0L,
                        deckId         = (map["deckId"] as? Long) ?: 0L,
                        front          = (map["front"] as? String) ?: "",
                        back           = (map["back"] as? String) ?: "",
                        interval       = ((map["interval"] as? Long) ?: 1L).toInt(),
                        repetition     = ((map["repetition"] as? Long) ?: 0L).toInt(),
                        easeFactor     = ((map["easeFactor"] as? Double) ?: 2.5).toFloat(),
                        nextReviewDate = (map["nextReviewDate"] as? Long) ?: System.currentTimeMillis(),
                        createdAt      = (map["createdAt"] as? Long) ?: System.currentTimeMillis()
                    )
                }
                trySend(cards)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun syncCardToCloud(card: Card) {
        if (userId.isBlank()) return
        try {
            db.child("users").child(userId).child("cards")
                .child(card.id.toString())
                .setValue(card.toMap())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "syncCardToCloud FAILED: ${e.message}")
        }
    }

    suspend fun deleteCardFromCloud(cardId: Long) {
        if (userId.isBlank()) return
        try {
            db.child("users").child(userId).child("cards")
                .child(cardId.toString())
                .removeValue().await()
        } catch (e: Exception) {
            Log.e(TAG, "deleteCardFromCloud FAILED: ${e.message}")
        }
    }

    suspend fun getCardsFromCloud(): List<Card> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snapshot = db.child("users").child(userId).child("cards").get().await()
            snapshot.children.mapNotNull { child ->
                val map = child.value as? Map<*, *> ?: return@mapNotNull null
                Card(
                    id             = (map["id"] as? Long) ?: 0L,
                    deckId         = (map["deckId"] as? Long) ?: 0L,
                    front          = (map["front"] as? String) ?: "",
                    back           = (map["back"] as? String) ?: "",
                    interval       = ((map["interval"] as? Long) ?: 1L).toInt(),
                    repetition     = ((map["repetition"] as? Long) ?: 0L).toInt(),
                    easeFactor     = ((map["easeFactor"] as? Double) ?: 2.5).toFloat(),
                    nextReviewDate = (map["nextReviewDate"] as? Long) ?: System.currentTimeMillis(),
                    createdAt      = (map["createdAt"] as? Long) ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun pullAllData(dao: CardDao) {
        if (userId.isBlank()) return
        val decks = getDecksFromCloud()
        val cards = getCardsFromCloud()
        decks.forEach { dao.insertDeck(it) }
        cards.forEach { dao.insertCard(it) }
    }

    // ✅ MỚI: Xóa sạch cloud để chuẩn bị Push dữ liệu chuẩn từ máy lên
    suspend fun clearCloudData() {
        if (userId.isBlank()) return
        db.child("users").child(userId).removeValue().await()
    }
}

fun Deck.toMap(): Map<String, Any> = mapOf(
    "id"          to id,
    "name"        to name,
    "description" to description,
    "createdAt"   to createdAt
)

fun Card.toMap(): Map<String, Any> = mapOf(
    "id"             to id,
    "deckId"         to deckId,
    "front"          to front,
    "back"           to back,
    "interval"       to interval,
    "repetition"     to repetition,
    "easeFactor"     to easeFactor.toDouble(),
    "nextReviewDate" to nextReviewDate,
    "createdAt"      to createdAt
)