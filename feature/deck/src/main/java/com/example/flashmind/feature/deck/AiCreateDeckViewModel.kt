package com.example.flashmind.feature.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashmind.core.domain.usecase.ImportDeckUseCase
import com.example.flashmind.core.model.ImportCardDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AiCreateDeckViewModel @Inject constructor(
    private val importDeckUseCase: ImportDeckUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiCreateDeckUiState())
    val uiState: StateFlow<AiCreateDeckUiState> = _uiState.asStateFlow()

    fun onTopicChanged(value: String) {
        _uiState.value = _uiState.value.copy(topic = value, error = null)
    }

    fun createDeck(onDone: () -> Unit) {
        val topic = _uiState.value.topic.trim()
        if (topic.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Nhập chủ đề trước khi tạo deck.")
            return
        }
        viewModelScope.launch {
            runCatching {
                val generated = generateDeck(topic)
                importDeckUseCase(
                    title = generated.title,
                    description = generated.description,
                    cards = generated.cards,
                )
            }.onSuccess {
                onDone()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    private fun generateDeck(topic: String): GeneratedDeck {
        val normalized = topic.lowercase()
        val templates = when {
            normalized.contains("du lịch") || normalized.contains("travel") -> travelDeck()
            normalized.contains("công sở") || normalized.contains("work") || normalized.contains("business") -> workDeck()
            normalized.contains("giao tiếp") || normalized.contains("daily") || normalized.contains("conversation") -> communicationDeck()
            normalized.contains("food") || normalized.contains("ăn") || normalized.contains("ẩm thực") -> foodDeck()
            else -> genericDeck(topic)
        }
        return GeneratedDeck(
            title = "AI deck: ${topic.replaceFirstChar { it.uppercase() }}",
            description = "Bộ thẻ được tạo tự động theo chủ đề \"$topic\".",
            cards = templates,
        )
    }

    private fun communicationDeck() = listOf(
        draft("greet", "chào hỏi", "To greet someone politely.", "She greeted the teacher warmly."),
        draft("introduce", "giới thiệu", "Let me introduce myself first.", "He introduced his friend to the class."),
        draft("request", "yêu cầu", "I have a small request for you.", "She made a request politely."),
        draft("respond", "phản hồi", "Please respond as soon as possible.", "He responded with a smile."),
        draft("agree", "đồng ý", "I agree with your idea.", "They agreed on the plan."),
        draft("disagree", "không đồng ý", "I disagree with that conclusion.", "She disagreed respectfully."),
        draft("explain", "giải thích", "Can you explain this word again?", "The tutor explained the rule clearly."),
        draft("repeat", "lặp lại", "Could you repeat that sentence?", "He repeated the answer slowly."),
        draft("conversation", "cuộc hội thoại", "We practiced a short conversation.", "The conversation was friendly."),
        draft("confident", "tự tin", "Try to speak in a confident voice.", "She sounded more confident today."),
        draft("hesitate", "do dự", "Don't hesitate to ask questions.", "He hesitated before answering."),
        draft("fluent", "trôi chảy", "She is becoming more fluent in English.", "He gave a fluent presentation."),
    )

    private fun travelDeck() = listOf(
        draft("passport", "hộ chiếu", "Keep your passport safe.", "My passport is in my backpack."),
        draft("boarding pass", "thẻ lên máy bay", "Show your boarding pass at the gate.", "I saved the boarding pass on my phone."),
        draft("luggage", "hành lý", "My luggage is too heavy.", "Her luggage arrived late."),
        draft("reservation", "đặt chỗ", "I confirmed the hotel reservation.", "They made a reservation online."),
        draft("itinerary", "lịch trình", "Our itinerary includes three cities.", "The itinerary changed yesterday."),
        draft("departure", "khởi hành", "The departure time is 8 p.m.", "Check the departure board."),
        draft("arrival", "đến nơi", "The arrival gate changed.", "Our arrival was delayed."),
        draft("customs", "hải quan", "We went through customs quickly.", "Customs asked one question."),
        draft("currency", "tiền tệ", "I need local currency.", "The exchange office changed my currency."),
        draft("souvenir", "quà lưu niệm", "She bought a souvenir for her brother.", "This market sells cheap souvenirs."),
        draft("map", "bản đồ", "Use the map to find the station.", "The map helped us a lot."),
        draft("guide", "hướng dẫn viên", "The guide explained the history well.", "Our guide was very helpful."),
    )

    private fun workDeck() = listOf(
        draft("meeting", "cuộc họp", "The meeting starts at nine.", "We joined the meeting on time."),
        draft("deadline", "hạn chót", "The deadline is next Monday.", "They missed the deadline."),
        draft("report", "báo cáo", "I finished the weekly report.", "She sent the report early."),
        draft("schedule", "lịch làm việc", "Let's check the schedule first.", "His schedule is full today."),
        draft("client", "khách hàng", "The client requested a revision.", "We met the client yesterday."),
        draft("proposal", "đề xuất", "Her proposal was accepted.", "The team reviewed the proposal."),
        draft("budget", "ngân sách", "This project needs a larger budget.", "They stayed within budget."),
        draft("strategy", "chiến lược", "We need a better strategy.", "Their strategy worked well."),
        draft("feedback", "phản hồi", "Thanks for your feedback.", "Constructive feedback is useful."),
        draft("update", "cập nhật", "Please update the document.", "He sent a quick update."),
        draft("negotiate", "thương lượng", "They negotiated the price carefully.", "She can negotiate well."),
        draft("priority", "ưu tiên", "This task is our top priority.", "Safety is the first priority."),
    )

    private fun foodDeck() = listOf(
        draft("ingredient", "nguyên liệu", "This soup has simple ingredients.", "Fresh ingredients improve the taste."),
        draft("recipe", "công thức", "I followed the recipe carefully.", "Her recipe is easy to copy."),
        draft("flavor", "hương vị", "The flavor is rich and sweet.", "This sauce adds more flavor."),
        draft("spicy", "cay", "The noodles are too spicy for me.", "He likes spicy food."),
        draft("bitter", "đắng", "The coffee tastes bitter.", "Some vegetables are slightly bitter."),
        draft("sweet", "ngọt", "This dessert is very sweet.", "She prefers sweet drinks."),
        draft("sour", "chua", "The soup is a little sour.", "That fruit tastes sour."),
        draft("fried", "chiên", "I ordered fried rice.", "They served fried chicken."),
        draft("boiled", "luộc", "Boiled eggs are easy to prepare.", "She cooked boiled vegetables."),
        draft("grilled", "nướng", "We ate grilled fish for dinner.", "The grilled meat smelled great."),
        draft("portion", "khẩu phần", "The portion is too large.", "They served a small portion."),
        draft("delicious", "ngon", "The meal was delicious.", "Everything on the table looked delicious."),
    )

    private fun genericDeck(topic: String) = listOf(
        draft("$topic basics", "kiến thức nền tảng về $topic", "Let's start with the basics of $topic.", "He reviewed the basics before moving on."),
        draft("$topic goal", "mục tiêu về $topic", "Set a clear goal for learning $topic.", "Her main goal is steady progress."),
        draft("$topic practice", "luyện tập về $topic", "Daily practice improves $topic.", "Regular practice builds confidence."),
        draft("$topic vocabulary", "từ vựng về $topic", "This deck covers useful $topic vocabulary.", "They learned topic vocabulary in context."),
        draft("$topic example", "ví dụ về $topic", "A good example makes $topic easier.", "The teacher gave a simple example."),
        draft("$topic strategy", "chiến lược cho $topic", "Choose a strategy that fits your level.", "Their strategy saved time."),
        draft("$topic challenge", "thử thách trong $topic", "Every learner faces a challenge in $topic.", "She overcame the main challenge."),
        draft("$topic progress", "tiến bộ về $topic", "Track your progress every week.", "His progress is easy to see."),
        draft("$topic habit", "thói quen học $topic", "A strong habit makes learning easier.", "Reading daily became a habit."),
        draft("$topic mistake", "lỗi thường gặp trong $topic", "Learn from each mistake in $topic.", "The mistake helped him improve."),
        draft("$topic review", "ôn tập về $topic", "Review helps you remember $topic longer.", "They set time aside for review."),
        draft("$topic confidence", "sự tự tin với $topic", "Confidence grows with repetition.", "She answered with confidence."),
    )

    private fun draft(front: String, back: String, example: String, sentence: String) = ImportCardDraft(
        front = front,
        back = back,
        exampleSentence = "$example $sentence",
        imageUrl = null,
        isStarred = false,
    )
}

data class AiCreateDeckUiState(
    val topic: String = "",
    val error: String? = null,
)

private data class GeneratedDeck(
    val title: String,
    val description: String,
    val cards: List<ImportCardDraft>,
)
