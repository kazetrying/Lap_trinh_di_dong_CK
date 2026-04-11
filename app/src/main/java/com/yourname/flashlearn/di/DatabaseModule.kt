package com.yourname.flashlearn.di

import android.content.Context
import androidx.room.Room
import com.yourname.flashlearn.data.local.dao.DeckDao
import com.yourname.flashlearn.data.local.dao.FlashcardDao
import com.yourname.flashlearn.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "flashlearn_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDeckDao(db: AppDatabase): DeckDao = db.deckDao()

    @Provides
    fun provideFlashcardDao(db: AppDatabase): FlashcardDao = db.flashcardDao()
}