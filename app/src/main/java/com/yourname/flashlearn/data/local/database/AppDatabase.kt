package com.yourname.flashlearn.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yourname.flashlearn.data.local.dao.DeckDao
import com.yourname.flashlearn.data.local.dao.FlashcardDao
import com.yourname.flashlearn.data.local.entity.DeckEntity
import com.yourname.flashlearn.data.local.entity.FlashcardEntity

@Database(
    entities = [DeckEntity::class, FlashcardEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao
}