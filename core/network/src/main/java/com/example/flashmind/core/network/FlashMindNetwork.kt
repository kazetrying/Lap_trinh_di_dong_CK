package com.example.flashmind.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocketListener
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Inject

interface FlashMindApi {
    @POST("v1/auth/register")
    suspend fun register(@Body request: AuthRequestDto): AuthResponseDto

    @POST("v1/auth/login")
    suspend fun login(@Body request: AuthRequestDto): AuthResponseDto

    @GET("v1/decks")
    suspend fun getDecks(): List<DeckDto>

    @POST("v1/decks")
    suspend fun createDeck(@Body request: CreateDeckRequestDto)

    @POST("v1/decks/{deckId}")
    suspend fun updateDeck(
        @Path("deckId") deckId: String,
        @Body request: UpdateDeckRequestDto,
    )

    @POST("v1/decks/{deckId}/cards")
    suspend fun createCard(
        @Path("deckId") deckId: String,
        @Body request: CreateCardRequestDto,
    )

    @POST("v1/cards/{cardId}")
    suspend fun updateCard(
        @Path("cardId") cardId: String,
        @Body request: UpdateCardRequestDto,
    )

    @POST("v1/cards/{cardId}/star")
    suspend fun updateCardStar(
        @Path("cardId") cardId: String,
        @Body request: UpdateCardStarRequestDto,
    )

    @POST("v1/decks/{deckId}/delete")
    suspend fun deleteDeck(@Path("deckId") deckId: String)

    @POST("v1/cards/{cardId}/delete")
    suspend fun deleteCard(@Path("cardId") cardId: String)

    @GET("v1/decks/{deckId}/due-cards")
    suspend fun getDueCards(@Path("deckId") deckId: String): List<CardDto>

    @POST("v1/reviews")
    suspend fun submitReview(@Body request: ReviewRequestDto)

    @POST("v1/audio/google-tts")
    suspend fun synthesize(@Body request: TextToSpeechRequestDto): TextToSpeechResponseDto
}

class FlashMindSocket @Inject constructor(
    private val client: OkHttpClient,
) {
    fun connect(listener: WebSocketListener) {
        val request = Request.Builder()
            .url("wss://api.flashmind.dev/v1/sync")
            .build()
        client.newWebSocket(request, listener)
    }
}

fun provideFlashMindApi(client: OkHttpClient = OkHttpClient.Builder().build()): FlashMindApi {
    val json = Json { ignoreUnknownKeys = true }
    return Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FlashMindApi::class.java)
}

@Serializable
data class AuthRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    val email: String,
    val token: String,
)

@Serializable
data class DeckDto(
    val id: String,
    val title: String,
    val description: String,
)

@Serializable
data class CardDto(
    val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val pronunciation: String? = null,
    val exampleSentence: String? = null,
    val audioUrl: String? = null,
    val repetition: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewAt: Long,
    val lastReviewedAt: Long? = null,
)

@Serializable
data class ReviewRequestDto(
    val cardId: String,
    val grade: Int,
    val reviewedAt: Long,
    val repetition: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val nextReviewAt: Long,
)

@Serializable
data class CreateDeckRequestDto(
    val deckId: String,
    val title: String,
    val description: String,
)

@Serializable
data class UpdateDeckRequestDto(
    val title: String,
    val description: String,
)

@Serializable
data class CreateCardRequestDto(
    val cardId: String,
    val front: String,
    val back: String,
    val pronunciation: String? = null,
    val exampleSentence: String? = null,
)

@Serializable
data class UpdateCardRequestDto(
    val front: String,
    val back: String,
    val pronunciation: String? = null,
    val exampleSentence: String? = null,
)

@Serializable
data class UpdateCardStarRequestDto(
    val isStarred: Boolean,
)

@Serializable
data class TextToSpeechRequestDto(
    val text: String,
    val languageCode: String = "en-US",
)

@Serializable
data class TextToSpeechResponseDto(
    val audioUrl: String,
)
