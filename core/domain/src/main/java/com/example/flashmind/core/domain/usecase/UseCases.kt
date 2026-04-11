package com.example.flashmind.core.domain.usecase

import com.example.flashmind.core.domain.repository.FlashcardRepository
import com.example.flashmind.core.model.ReviewGrade
import javax.inject.Inject

class ObserveDecksUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    operator fun invoke() = repository.observeDecks()
}

class ObserveDeckUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    operator fun invoke(deckId: String) = repository.observeDeck(deckId)
}

class ObserveCardsUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    operator fun invoke(deckId: String) = repository.observeCards(deckId)
}

class ObserveDueCardsUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    operator fun invoke(deckId: String) = repository.observeDueCards(deckId)
}

class ObservePendingSyncTasksUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    operator fun invoke() = repository.observePendingSyncTasks()
}

class ObserveStudyAnalyticsUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    operator fun invoke() = repository.observeStudyAnalytics()
}

class SubmitReviewUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(cardId: String, grade: ReviewGrade) {
        repository.submitReview(cardId, grade)
    }
}

class RefreshDecksUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke() = repository.refreshDecks()
}

class CreateDeckUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(title: String, description: String) {
        repository.createDeck(title, description)
    }
}

class UpdateDeckUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(deckId: String, title: String, description: String) {
        repository.updateDeck(deckId, title, description)
    }
}

class ImportDeckUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        cards: List<com.example.flashmind.core.model.ImportCardDraft>,
    ) {
        repository.importDeck(title, description, cards)
    }
}

class RefreshDueCardsUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(deckId: String) = repository.refreshDueCards(deckId)
}

class DeleteDeckUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(deckId: String) = repository.deleteDeck(deckId)
}

class CreateCardUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(
        deckId: String,
        front: String,
        back: String,
        pronunciation: String?,
        exampleSentence: String?,
        imageUrl: String?,
    ) = repository.createCard(deckId, front, back, pronunciation, exampleSentence, imageUrl)
}

class UpdateCardUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(
        cardId: String,
        front: String,
        back: String,
        pronunciation: String?,
        exampleSentence: String?,
        imageUrl: String?,
    ) = repository.updateCard(cardId, front, back, pronunciation, exampleSentence, imageUrl)
}

class DeleteCardUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(cardId: String) = repository.deleteCard(cardId)
}

class ToggleCardStarUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(cardId: String) = repository.toggleCardStar(cardId)
}

class CountDueCardsUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke(): Int = repository.countDueCards()
}

class SyncPendingTasksUseCase @Inject constructor(
    private val repository: FlashcardRepository,
) {
    suspend operator fun invoke() = repository.syncPendingTasks()
}
