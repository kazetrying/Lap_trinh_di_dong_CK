package com.example.flashmind.core.data.repository

import com.example.flashmind.core.database.dao.DeckDao
import com.example.flashmind.core.database.dao.DeckSummaryRow
import com.example.flashmind.core.database.entity.CardEntity
import com.example.flashmind.core.database.entity.DeckEntity
import com.example.flashmind.core.database.entity.PendingSyncEntity
import com.example.flashmind.core.domain.repository.FlashcardRepository
import com.example.flashmind.core.domain.spacedrepetition.Sm2Scheduler
import com.example.flashmind.core.model.Deck
import com.example.flashmind.core.model.ImportCardDraft
import com.example.flashmind.core.model.PendingSyncTask
import com.example.flashmind.core.model.ReviewGrade
import com.example.flashmind.core.model.StudyAnalytics
import com.example.flashmind.core.model.StudyProgress
import com.example.flashmind.core.model.SyncTaskType
import com.example.flashmind.core.model.VocabularyCard
import com.example.flashmind.core.network.CardDto
import com.example.flashmind.core.network.CreateCardRequestDto
import com.example.flashmind.core.network.CreateDeckRequestDto
import com.example.flashmind.core.network.DeckDto
import com.example.flashmind.core.network.FlashMindApi
import com.example.flashmind.core.network.ReviewRequestDto
import com.example.flashmind.core.network.UpdateDeckRequestDto
import com.example.flashmind.core.network.UpdateCardStarRequestDto
import com.example.flashmind.core.network.UpdateCardRequestDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class OfflineFirstFlashcardRepository @Inject constructor(
    private val api: FlashMindApi,
    private val deckDao: DeckDao,
    private val scheduler: Sm2Scheduler,
) : FlashcardRepository {

    override fun observeDeck(deckId: String): Flow<Deck?> =
        reviewClockFlow().flatMapLatest { now ->
            deckDao.observeDeck(deckId, now)
        }.map { it?.toModel() }

    override fun observeCards(deckId: String): Flow<List<VocabularyCard>> =
        deckDao.observeCards(deckId).map { cards -> cards.map(CardEntity::toModel) }

    override fun observePendingSyncTasks(): Flow<List<PendingSyncTask>> =
        deckDao.observePendingSyncTasks().map { tasks -> tasks.map(PendingSyncEntity::toModel) }

    override fun observeStudyAnalytics(): Flow<StudyAnalytics> =
        deckDao.observeReviewHistory().map { history ->
            history.toStudyAnalytics()
        }

    override suspend fun createDeck(title: String, description: String) {
        val deckId = "deck-${UUID.randomUUID()}"
        val now = Instant.now()
        deckDao.upsertDecks(
            listOf(
                DeckEntity(
                    id = deckId,
                    title = title,
                    description = description,
                ),
            ),
        )
        deckDao.upsertCards(
            listOf(
                CardEntity(
                    id = "card-${UUID.randomUUID()}",
                    deckId = deckId,
                    front = "starter",
                    back = "a newly created placeholder card",
                    pronunciation = null,
                    exampleSentence = "Replace this with your own vocabulary.",
                    imageUrl = null,
                    audioUrl = null,
                    isStarred = false,
                    repetition = 0,
                    intervalDays = 0,
                    easeFactor = 2.5,
                    nextReviewAt = now.toEpochMilli(),
                    lastReviewedAt = null,
                ),
            ),
        )
        enqueueSyncTask(
            type = SyncTaskType.CREATE_DECK,
            payload = buildJsonObject {
                put("deckId", deckId)
                put("title", title)
                put("description", description)
            }.toString(),
        )
    }

    override suspend fun updateDeck(deckId: String, title: String, description: String) {
        val current = deckDao.getDeckEntity(deckId) ?: return
        deckDao.upsertDecks(
            listOf(
                current.copy(
                    title = title,
                    description = description,
                ),
            ),
        )
        enqueueSyncTask(
            type = SyncTaskType.UPDATE_DECK,
            payload = buildJsonObject {
                put("deckId", deckId)
                put("title", title)
                put("description", description)
            }.toString(),
        )
    }

    override suspend fun importDeck(
        title: String,
        description: String,
        cards: List<ImportCardDraft>,
    ) {
        val deckId = "deck-${UUID.randomUUID()}"
        deckDao.upsertDecks(
            listOf(
                DeckEntity(
                    id = deckId,
                    title = title,
                    description = description,
                ),
            ),
        )
        val now = Instant.now().toEpochMilli()
        deckDao.upsertCards(
            cards.map { card ->
                CardEntity(
                    id = "card-${UUID.randomUUID()}",
                    deckId = deckId,
                    front = card.front,
                    back = card.back,
                    pronunciation = card.pronunciation,
                    exampleSentence = card.exampleSentence,
                    imageUrl = card.imageUrl,
                    audioUrl = null,
                    isStarred = card.isStarred,
                    repetition = 0,
                    intervalDays = 0,
                    easeFactor = 2.5,
                    nextReviewAt = now,
                    lastReviewedAt = null,
                )
            },
        )
    }

    override suspend fun deleteDeck(deckId: String) {
        deckDao.deleteDeckWithCards(deckId)
        enqueueSyncTask(
            type = SyncTaskType.DELETE_DECK,
            payload = buildJsonObject {
                put("deckId", deckId)
            }.toString(),
        )
    }

    override suspend fun createCard(
        deckId: String,
        front: String,
        back: String,
        pronunciation: String?,
        exampleSentence: String?,
        imageUrl: String?,
    ) {
        val cardId = "card-${UUID.randomUUID()}"
        deckDao.upsertCards(
            listOf(
                CardEntity(
                    id = cardId,
                    deckId = deckId,
                    front = front,
                    back = back,
                    pronunciation = pronunciation,
                    exampleSentence = exampleSentence,
                    imageUrl = imageUrl,
                    audioUrl = null,
                    isStarred = false,
                    repetition = 0,
                    intervalDays = 0,
                    easeFactor = 2.5,
                    nextReviewAt = Instant.now().toEpochMilli(),
                    lastReviewedAt = null,
                ),
            ),
        )
        enqueueSyncTask(
            type = SyncTaskType.CREATE_CARD,
            payload = buildJsonObject {
                put("cardId", cardId)
                put("deckId", deckId)
                put("front", front)
                put("back", back)
                pronunciation?.let { put("pronunciation", it) }
                exampleSentence?.let { put("exampleSentence", it) }
            }.toString(),
        )
    }

    override suspend fun updateCard(
        cardId: String,
        front: String,
        back: String,
        pronunciation: String?,
        exampleSentence: String?,
        imageUrl: String?,
    ) {
        val current = deckDao.getCard(cardId) ?: return
        deckDao.upsertCards(
            listOf(
                current.copy(
                    front = front,
                    back = back,
                    pronunciation = pronunciation,
                    exampleSentence = exampleSentence,
                    imageUrl = imageUrl,
                ),
            ),
        )
        enqueueSyncTask(
            type = SyncTaskType.UPDATE_CARD,
            payload = buildJsonObject {
                put("cardId", cardId)
                put("front", front)
                put("back", back)
                pronunciation?.let { put("pronunciation", it) }
                exampleSentence?.let { put("exampleSentence", it) }
            }.toString(),
        )
    }

    override suspend fun deleteCard(cardId: String) {
        deckDao.deleteCard(cardId)
        enqueueSyncTask(
            type = SyncTaskType.DELETE_CARD,
            payload = buildJsonObject {
                put("cardId", cardId)
            }.toString(),
        )
    }

    override suspend fun toggleCardStar(cardId: String) {
        val current = deckDao.getCard(cardId) ?: return
        val updatedValue = !current.isStarred
        deckDao.updateCardStar(cardId, updatedValue)
        runCatching {
            api.updateCardStar(
                cardId = cardId,
                request = UpdateCardStarRequestDto(isStarred = updatedValue),
            )
        }.onFailure {
            enqueueSyncTask(
                type = SyncTaskType.TOGGLE_CARD_STAR,
                payload = buildJsonObject {
                    put("cardId", cardId)
                    put("isStarred", updatedValue)
                }.toString(),
            )
        }
    }

    override fun observeDecks(): Flow<List<Deck>> =
        reviewClockFlow().flatMapLatest { now ->
            deckDao.observeDeckSummaries(now)
        }.map { rows ->
            rows.map(DeckSummaryRow::toModel)
        }

    override fun observeDueCards(deckId: String): Flow<List<VocabularyCard>> =
        reviewClockFlow().flatMapLatest { now ->
            deckDao.observeDueCards(deckId, now)
        }.map { cards ->
            cards.map(CardEntity::toModel)
        }

    override suspend fun submitReview(cardId: String, grade: ReviewGrade) {
        val card = deckDao.getCard(cardId) ?: return
        val result = scheduler.review(card.toModel().progress, grade)
        val request = ReviewRequestDto(
            cardId = cardId,
            grade = grade.score,
            reviewedAt = Instant.now().toEpochMilli(),
            repetition = result.updatedProgress.repetition,
            intervalDays = result.updatedProgress.intervalDays,
            easeFactor = result.updatedProgress.easeFactor,
            nextReviewAt = result.updatedProgress.nextReviewAt.toEpochMilli(),
        )
        deckDao.updateProgress(
            cardId = cardId,
            repetition = result.updatedProgress.repetition,
            intervalDays = result.updatedProgress.intervalDays,
            easeFactor = result.updatedProgress.easeFactor,
            nextReviewAt = result.updatedProgress.nextReviewAt.toEpochMilli(),
            lastReviewedAt = result.updatedProgress.lastReviewedAt?.toEpochMilli(),
        )
        deckDao.insertReviewHistory(
            com.example.flashmind.core.database.entity.ReviewHistoryEntity(
                id = "review-${UUID.randomUUID()}",
                cardId = card.id,
                deckId = card.deckId,
                grade = grade.score,
                reviewedAt = request.reviewedAt,
            ),
        )
        runCatching { api.submitReview(request) }
            .onFailure {
                enqueueSyncTask(
                    type = SyncTaskType.REVIEW,
                    payload = buildJsonObject {
                        put("cardId", request.cardId)
                        put("grade", request.grade)
                        put("reviewedAt", request.reviewedAt)
                        put("repetition", request.repetition)
                        put("intervalDays", request.intervalDays)
                        put("easeFactor", request.easeFactor)
                        put("nextReviewAt", request.nextReviewAt)
                    }.toString(),
                )
            }
    }

    override suspend fun refreshDecks() {
        val decks = runCatching { api.getDecks() }
            .getOrElse {
                if (deckDao.countDecks() == 0) {
                    deckDao.replaceAll(
                        decks = SampleData.decks(),
                        cards = SampleData.cards(),
                    )
                }
                return
            }
        val dueCards = decks.flatMap { deck ->
            runCatching { api.getDueCards(deck.id) }.getOrDefault(emptyList())
        }
        deckDao.replaceAll(
            decks = decks.map(DeckDto::toEntity),
            cards = dueCards.map(CardDto::toEntity),
        )
    }

    override suspend fun refreshDueCards(deckId: String) {
        val cards = runCatching { api.getDueCards(deckId) }
            .getOrElse {
                deckDao.upsertCards(SampleData.cards().filter { card -> card.deckId == deckId })
                return
            }
        deckDao.upsertCards(cards.map(CardDto::toEntity))
    }

    override suspend fun countDueCards(): Int {
        return deckDao.countDueCards(Instant.now().toEpochMilli())
    }

    override suspend fun syncPendingTasks() {
        deckDao.getPendingSyncTasks().forEach { task ->
            val synced = when (task.type.toSyncTaskType()) {
                SyncTaskType.REVIEW -> runCatching { syncReviewTask(task) }.isSuccess
                SyncTaskType.CREATE_DECK -> runCatching { syncCreateDeckTask(task) }.isSuccess
                SyncTaskType.UPDATE_DECK -> runCatching { syncUpdateDeckTask(task) }.isSuccess
                SyncTaskType.CREATE_CARD -> runCatching { syncCreateCardTask(task) }.isSuccess
                SyncTaskType.UPDATE_CARD -> runCatching { syncUpdateCardTask(task) }.isSuccess
                SyncTaskType.DELETE_CARD -> runCatching { syncDeleteCardTask(task) }.isSuccess
                SyncTaskType.DELETE_DECK -> runCatching { syncDeleteDeckTask(task) }.isSuccess
                SyncTaskType.TOGGLE_CARD_STAR -> runCatching { syncToggleCardStarTask(task) }.isSuccess
            }
            if (synced) {
                deckDao.deletePendingSyncTask(task.id)
            }
        }
    }

    private suspend fun syncReviewTask(task: PendingSyncEntity) {
        val payload = parsePayload(task.payload)
        api.submitReview(
            ReviewRequestDto(
                cardId = payload.string("cardId"),
                grade = payload.int("grade"),
                reviewedAt = payload.long("reviewedAt"),
                repetition = payload.int("repetition"),
                intervalDays = payload.int("intervalDays"),
                easeFactor = payload.double("easeFactor"),
                nextReviewAt = payload.long("nextReviewAt"),
            ),
        )
    }

    private suspend fun syncCreateDeckTask(task: PendingSyncEntity) {
        val payload = parsePayload(task.payload)
        api.createDeck(
            CreateDeckRequestDto(
                deckId = payload.string("deckId"),
                title = payload.string("title"),
                description = payload.string("description"),
            ),
        )
    }

    private suspend fun syncUpdateDeckTask(task: PendingSyncEntity) {
        val payload = parsePayload(task.payload)
        api.updateDeck(
            deckId = payload.string("deckId"),
            request = UpdateDeckRequestDto(
                title = payload.string("title"),
                description = payload.string("description"),
            ),
        )
    }

    private suspend fun syncCreateCardTask(task: PendingSyncEntity) {
        val payload = parsePayload(task.payload)
        api.createCard(
            deckId = payload.string("deckId"),
            request = CreateCardRequestDto(
                cardId = payload.string("cardId"),
                front = payload.string("front"),
                back = payload.string("back"),
                pronunciation = payload.optionalString("pronunciation"),
                exampleSentence = payload.optionalString("exampleSentence"),
            ),
        )
    }

    private suspend fun syncUpdateCardTask(task: PendingSyncEntity) {
        val payload = parsePayload(task.payload)
        api.updateCard(
            cardId = payload.string("cardId"),
            request = UpdateCardRequestDto(
                front = payload.string("front"),
                back = payload.string("back"),
                pronunciation = payload.optionalString("pronunciation"),
                exampleSentence = payload.optionalString("exampleSentence"),
            ),
        )
    }

    private suspend fun syncDeleteCardTask(task: PendingSyncEntity) {
        val payload = parsePayload(task.payload)
        api.deleteCard(payload.string("cardId"))
    }

    private suspend fun syncDeleteDeckTask(task: PendingSyncEntity) {
        val payload = parsePayload(task.payload)
        api.deleteDeck(payload.string("deckId"))
    }

    private suspend fun syncToggleCardStarTask(task: PendingSyncEntity) {
        val payload = parsePayload(task.payload)
        api.updateCardStar(
            cardId = payload.string("cardId"),
            request = UpdateCardStarRequestDto(
                isStarred = payload.boolean("isStarred"),
            ),
        )
    }

    private suspend fun enqueueSyncTask(type: SyncTaskType, payload: String) {
        deckDao.upsertPendingSyncTasks(
            listOf(
                PendingSyncEntity(
                    id = "sync-${UUID.randomUUID()}",
                    type = type.name,
                    payload = payload,
                    createdAt = Instant.now().toEpochMilli(),
                ),
            ),
        )
    }

    private fun reviewClockFlow(intervalMillis: Long = 60_000L): Flow<Long> = flow {
        while (true) {
            emit(Instant.now().toEpochMilli())
            delay(intervalMillis)
        }
    }
}

private fun DeckSummaryRow.toModel() = Deck(
    id = id,
    title = title,
    description = description,
    dueCount = dueCount,
    cardCount = cardCount,
)

private fun CardEntity.toModel() = VocabularyCard(
    id = id,
    deckId = deckId,
    front = front,
    back = back,
    pronunciation = pronunciation,
    exampleSentence = exampleSentence,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    isStarred = isStarred,
    progress = StudyProgress(
        repetition = repetition,
        intervalDays = intervalDays,
        easeFactor = easeFactor,
        nextReviewAt = Instant.ofEpochMilli(nextReviewAt),
        lastReviewedAt = lastReviewedAt?.let(Instant::ofEpochMilli),
    ),
)

private fun DeckDto.toEntity() = DeckEntity(
    id = id,
    title = title,
    description = description,
)

private fun CardDto.toEntity() = CardEntity(
    id = id,
    deckId = deckId,
    front = front,
    back = back,
    pronunciation = pronunciation,
    exampleSentence = exampleSentence,
    imageUrl = null,
    audioUrl = audioUrl,
    isStarred = false,
    repetition = repetition,
    intervalDays = intervalDays,
    easeFactor = easeFactor,
    nextReviewAt = nextReviewAt,
    lastReviewedAt = lastReviewedAt,
)

private fun PendingSyncEntity.toModel() = PendingSyncTask(
    id = id,
    type = type.toSyncTaskType(),
    payload = payload,
    createdAt = Instant.ofEpochMilli(createdAt),
)

private fun String.toSyncTaskType(): SyncTaskType =
    runCatching { SyncTaskType.valueOf(this) }.getOrDefault(SyncTaskType.REVIEW)

private fun OfflineFirstFlashcardRepository.parsePayload(raw: String): JsonObject =
    Json { ignoreUnknownKeys = true }.parseToJsonElement(raw).jsonObject

private fun JsonObject.string(key: String): String =
    this[key]?.jsonPrimitive?.content.orEmpty()

private fun JsonObject.optionalString(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

private fun JsonObject.int(key: String): Int =
    this[key]?.jsonPrimitive?.intOrNull ?: 0

private fun JsonObject.long(key: String): Long =
    this[key]?.jsonPrimitive?.longOrNull ?: 0L

private fun JsonObject.double(key: String): Double =
    this[key]?.jsonPrimitive?.doubleOrNull ?: 0.0

private fun JsonObject.boolean(key: String): Boolean =
    this[key]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false

private fun List<com.example.flashmind.core.database.entity.ReviewHistoryEntity>.toStudyAnalytics(): StudyAnalytics {
    if (isEmpty()) return StudyAnalytics()
    val zone = ZoneId.systemDefault()
    val reviewedDays = map { Instant.ofEpochMilli(it.reviewedAt).atZone(zone).toLocalDate() }.toSet()
    val today = LocalDate.now(zone)
    var streak = 0
    var cursor = today
    while (reviewedDays.contains(cursor)) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    val todayReviews = count { Instant.ofEpochMilli(it.reviewedAt).atZone(zone).toLocalDate() == today }
    return StudyAnalytics(
        totalReviews = size,
        todayReviews = todayReviews,
        currentStreakDays = streak,
    )
}


