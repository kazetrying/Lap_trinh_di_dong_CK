package com.example.flashmind.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.flashmind.core.data.repository.OfflineFirstFlashcardRepository
import com.example.flashmind.core.database.FlashMindDatabase
import com.example.flashmind.core.database.dao.DeckDao
import com.example.flashmind.core.domain.repository.FlashcardRepository
import com.example.flashmind.core.network.FlashMindApi
import com.example.flashmind.core.network.provideFlashMindApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFlashcardRepository(
        repository: OfflineFirstFlashcardRepository,
    ): FlashcardRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidersModule {
    private const val AUTH_PREFS = "flashmind_auth_headers"
    private const val AUTH_TOKEN_KEY = "auth_token"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FlashMindDatabase =
        Room.databaseBuilder(
            context,
            FlashMindDatabase::class.java,
            "flashmind.db",
        ).addMigrations(
            FlashMindDatabase.MIGRATION_2_3,
            FlashMindDatabase.MIGRATION_3_4,
            FlashMindDatabase.MIGRATION_4_5,
        )
            .build()

    @Provides
    fun provideDeckDao(database: FlashMindDatabase): DeckDao = database.deckDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = context
                    .getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
                    .getString(AUTH_TOKEN_KEY, null)
                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }.build()
                chain.proceed(request)
            }
            .build()

    @Provides
    @Singleton
    fun provideApi(client: OkHttpClient): FlashMindApi = provideFlashMindApi(client)
}
