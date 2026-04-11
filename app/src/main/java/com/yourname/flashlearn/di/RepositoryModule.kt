package com.yourname.flashlearn.di

import com.yourname.flashlearn.data.repository.FlashcardRepositoryImpl
import com.yourname.flashlearn.domain.repository.FlashcardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFlashcardRepository(
        impl: FlashcardRepositoryImpl
    ): FlashcardRepository
}