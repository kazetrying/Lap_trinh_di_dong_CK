package com.example.flashmind.core.database

import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.flashmind.core.database.dao.DeckDao
import com.example.flashmind.core.database.entity.CardEntity
import com.example.flashmind.core.database.entity.DeckEntity
import com.example.flashmind.core.database.entity.PendingSyncEntity
import com.example.flashmind.core.database.entity.ReviewHistoryEntity
import com.example.flashmind.core.database.util.InstantConverter

@Database(
    entities = [DeckEntity::class, CardEntity::class, PendingSyncEntity::class, ReviewHistoryEntity::class],
    version = 5,
    exportSchema = false,
)
@TypeConverters(InstantConverter::class)
abstract class FlashMindDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE cards ADD COLUMN isStarred INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS review_history (
                        id TEXT NOT NULL PRIMARY KEY,
                        cardId TEXT NOT NULL,
                        deckId TEXT NOT NULL,
                        grade INTEGER NOT NULL,
                        reviewedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE cards ADD COLUMN imageUrl TEXT",
                )
            }
        }
    }
}
