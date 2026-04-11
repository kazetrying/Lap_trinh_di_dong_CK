package com.example.flashmind.feature.deck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashmind.core.domain.usecase.ObserveCardsUseCase
import com.example.flashmind.core.domain.usecase.ObserveDeckUseCase
import com.example.flashmind.core.model.Deck
import com.example.flashmind.core.model.VocabularyCard
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class DeckChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDeckUseCase: ObserveDeckUseCase,
    observeCardsUseCase: ObserveCardsUseCase,
) : ViewModel() {

    private val deckId: String = checkNotNull(savedStateHandle["deckId"])
    private var currentDeck: Deck? = null
    private var currentCards: List<VocabularyCard> = emptyList()

    private val _uiState = MutableStateFlow(DeckChatUiState())
    val uiState: StateFlow<DeckChatUiState> = _uiState.asStateFlow()

    init {
        combine(
            observeDeckUseCase(deckId),
            observeCardsUseCase(deckId),
        ) { deck, cards -> deck to cards }
            .onEach { (deck, cards) ->
                currentDeck = deck
                currentCards = cards
                val initialMessages = if (_uiState.value.messages.isEmpty() && deck != null) {
                    listOf(
                        ChatMessage(
                            id = "welcome",
                            role = ChatRole.ASSISTANT,
                            content = "Mình là trợ lý AI cho bộ thẻ ${deck.title}. Bạn có thể hỏi ôn tập nhanh, xin câu hỏi kiểm tra, hỏi thẻ khó hoặc yêu cầu tóm tắt bộ thẻ.",
                        ),
                    )
                } else {
                    _uiState.value.messages
                }
                _uiState.value = _uiState.value.copy(
                    deckTitle = deck?.title.orEmpty(),
                    quickPrompts = defaultQuickPrompts(cards),
                    messages = initialMessages,
                )
            }
            .launchIn(viewModelScope)
    }

    fun onPromptChanged(value: String) {
        _uiState.value = _uiState.value.copy(prompt = value)
    }

    fun send() {
        val prompt = _uiState.value.prompt.trim()
        if (prompt.isEmpty()) return
        submitPrompt(prompt)
    }

    fun useQuickPrompt(prompt: String) {
        submitPrompt(prompt)
    }

    private fun submitPrompt(prompt: String) {
        val userMessage = ChatMessage(
            id = "user-${System.nanoTime()}",
            role = ChatRole.USER,
            content = prompt,
        )
        val assistantMessage = ChatMessage(
            id = "assistant-${System.nanoTime()}",
            role = ChatRole.ASSISTANT,
            content = generateAnswer(prompt, currentDeck, currentCards),
        )
        _uiState.value = _uiState.value.copy(
            prompt = "",
            messages = _uiState.value.messages + userMessage + assistantMessage,
        )
    }

    private fun generateAnswer(
        prompt: String,
        deck: Deck?,
        cards: List<VocabularyCard>,
    ): String {
        if (deck == null) return "Bộ thẻ chưa sẵn sàng. Hãy quay lại rồi mở lại chatbot."
        if (cards.isEmpty()) return "Bộ thẻ này chưa có thẻ nào. Hãy thêm vài thẻ trước, mình sẽ tạo câu hỏi và gợi ý ôn tập ngay."

        val query = prompt.lowercase()
        val dueCards = cards.filter { !it.progress.nextReviewAt.isAfter(Instant.now()) }
        val hardCards = cards.sortedBy { it.progress.easeFactor }.take(3)
        val matchedCards = cards.filter { card ->
            query.contains(card.front.lowercase()) ||
                query.contains(card.back.lowercase()) ||
                card.front.lowercase().contains(query) ||
                card.back.lowercase().contains(query)
        }

        return when {
            query.contains("quiz") || query.contains("kiểm tra") || query.contains("test") -> {
                val card = dueCards.firstOrNull() ?: cards.first()
                "Câu hỏi nhanh: \"${card.front}\" nghĩa là gì? Gợi ý: ease=${card.progress.easeFactor}, interval=${card.progress.intervalDays} ngày. Khi bạn tự trả lời xong, mở chế độ Học hoặc Ôn tập để kiểm tra tiếp."
            }

            query.contains("khó") || query.contains("hard") -> {
                buildString {
                    append("Ba thẻ cần ưu tiên:\n")
                    hardCards.forEachIndexed { index, card ->
                        append("${index + 1}. ${card.front} -> ${card.back} (ease ${card.progress.easeFactor})\n")
                    }
                    append("Bạn nên học chế độ Học trước, rồi Ôn tập để kéo ease factor lên.")
                }.trim()
            }

            query.contains("tóm tắt") || query.contains("summary") || query.contains("deck này") -> {
                "Bộ thẻ ${deck.title} hiện có ${cards.size} thẻ, ${dueCards.size} thẻ đến hạn, ${cards.count { it.isStarred }} thẻ gắn sao. Nếu muốn, tôi có thể tạo câu hỏi nhanh hoặc chỉ ra nhóm thẻ khó nhất."
            }

            query.contains("star") || query.contains("gắn sao") || query.contains("ưu tiên") -> {
                val starred = cards.filter { it.isStarred }.take(4)
                if (starred.isEmpty()) {
                    "Bộ thẻ này chưa có thẻ gắn sao. Bạn có thể đánh dấu những thẻ quan trọng rồi hỏi lại để mình gom nhóm ưu tiên."
                } else {
                    "Nhóm thẻ ưu tiên hiện tại: ${starred.joinToString { "\"${it.front}\"" }}."
                }
            }

            matchedCards.isNotEmpty() -> {
                val card = matchedCards.first()
                buildString {
                    append("\"${card.front}\" có nghĩa là \"${card.back}\".")
                    card.pronunciation?.takeIf { it.isNotBlank() }?.let { append(" Phát âm: $it.") }
                    card.exampleSentence?.takeIf { it.isNotBlank() }?.let { append(" Ví dụ: $it") }
                }
            }

            else -> {
                "Tôi có thể giúp theo 4 kiểu: tóm tắt bộ thẻ, tạo câu hỏi nhanh, chỉ ra thẻ khó, hoặc giải nghĩa một thẻ cụ thể. Hãy thử: \"quiz nhanh\", \"thẻ khó nhất\", hoặc tên một từ trong bộ thẻ."
            }
        }
    }

    private fun defaultQuickPrompts(cards: List<VocabularyCard>): List<String> {
        val hasStarred = cards.any { it.isStarred }
        return buildList {
            add("Tóm tắt bộ thẻ này")
            add("Quiz nhanh cho tôi")
            add("Thẻ khó nhất là gì?")
            if (hasStarred) add("Nhóm thẻ ưu tiên")
        }
    }
}

data class DeckChatUiState(
    val deckTitle: String = "",
    val prompt: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val quickPrompts: List<String> = emptyList(),
)

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
)

enum class ChatRole {
    USER,
    ASSISTANT,
}
