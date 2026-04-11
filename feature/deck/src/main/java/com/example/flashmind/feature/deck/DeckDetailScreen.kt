package com.example.flashmind.feature.deck

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.flashmind.core.model.VocabularyCard
import org.json.JSONArray
import org.json.JSONObject

internal val DeckTextColor = Color(0xFF111111)

internal val DeckScreenBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF4F1EA),
        Color(0xFFEAE3D7),
        Color(0xFFDDE9E5),
    ),
)

internal val HeaderBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF102A43),
        Color(0xFF173853),
        Color(0xFF1B666F),
    ),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeckDetailScreen(
    state: State<DeckDetailUiState>,
    onStartReview: (String) -> Unit,
    onStartTest: (String) -> Unit,
    onStartLearn: (String) -> Unit,
    onStartMatch: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onAddCard: () -> Unit,
    onEditCard: (VocabularyCard) -> Unit,
    onDeleteCard: (String) -> Unit,
    onToggleCardStar: (String) -> Unit,
    onBack: () -> Unit,
    onEditDeck: () -> Unit,
    onDeckTitleChanged: (String) -> Unit,
    onDeckDescriptionChanged: (String) -> Unit,
    onFrontChanged: (String) -> Unit,
    onBackChanged: (String) -> Unit,
    onPronunciationChanged: (String) -> Unit,
    onExampleChanged: (String) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onCardSearchQueryChanged: (String) -> Unit,
    onCardFilterChanged: (CardFilter) -> Unit,
    onSaveDeck: () -> Unit,
    onDismissDeckEditor: () -> Unit,
    onSaveCard: () -> Unit,
    onDismissEditor: () -> Unit,
) {
    val uiState by state
    val deck = uiState.deck
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (deck != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(30.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HeaderBackground)
                                .padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            TextAction(label = "Quay lại", onClick = onBack, color = Color(0xFFFFFCF6))
                            Text(
                                text = deck.title,
                                style = MaterialTheme.typography.displaySmall,
                                color = Color(0xFFFFFCF6),
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = buildString {
                                    if (deck.description.isNotBlank()) {
                                        append(deck.description)
                                        append("\n")
                                    }
                                    append("${deck.cardCount} thẻ - ${deck.dueCount} thẻ đến hạn")
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFE7F0F2),
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                HeaderActionChip(label = "Thêm thẻ", onClick = onAddCard, filled = true)
                                HeaderActionChip(label = "Trợ lý AI", onClick = { onOpenChat(deck.id) }, filled = true)
                                HeaderActionChip(label = "Sửa bộ thẻ", onClick = onEditDeck)
                                HeaderActionChip(
                                    label = "Chia sẻ",
                                    onClick = {
                                        val payload = createDeckExportJson(deck.title, deck.description, uiState.cards)
                                        context.startActivity(
                                            Intent.createChooser(
                                                Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, payload)
                                                },
                                                "Chia sẻ bộ thẻ",
                                            ),
                                        )
                                    },
                                )
                                HeaderActionChip(
                                    label = "Sao chép JSON",
                                    onClick = {
                                        clipboardManager.setText(
                                            AnnotatedString(createDeckExportJson(deck.title, deck.description, uiState.cards)),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
                if (uiState.isDeckEditorVisible) {
                    item {
                        DeckMetaEditorPanel(
                            uiState = uiState,
                            onDeckTitleChanged = onDeckTitleChanged,
                            onDeckDescriptionChanged = onDeckDescriptionChanged,
                            onSaveDeck = onSaveDeck,
                            onDismissDeckEditor = onDismissDeckEditor,
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = uiState.cardSearchQuery,
                        onValueChange = onCardSearchQueryChanged,
                        label = { Text("Tìm thẻ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                    )
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterAction("Tất cả", uiState.cardFilter == CardFilter.ALL) { onCardFilterChanged(CardFilter.ALL) }
                        FilterAction("Chỉ thẻ đến hạn", uiState.cardFilter == CardFilter.DUE) { onCardFilterChanged(CardFilter.DUE) }
                        FilterAction("Chỉ thẻ gắn sao", uiState.cardFilter == CardFilter.STARRED) { onCardFilterChanged(CardFilter.STARRED) }
                    }
                }
                item {
                    DeckSectionCard(title = "Chế độ học") {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            TextAction(label = "Ôn tập", onClick = { onStartReview(deck.id) }, color = DeckTextColor)
                            TextAction(label = "Học", onClick = { onStartLearn(deck.id) }, color = DeckTextColor)
                            TextAction(label = "Kiểm tra", onClick = { onStartTest(deck.id) }, color = DeckTextColor)
                            TextAction(label = "Ghép cặp", onClick = { onStartMatch(deck.id) }, color = DeckTextColor)
                        }
                    }
                }
            }

            if (uiState.isEditorVisible) {
                item {
                    DeckEditorPanel(
                        uiState = uiState,
                        onFrontChanged = onFrontChanged,
                        onBackChanged = onBackChanged,
                        onPronunciationChanged = onPronunciationChanged,
                        onExampleChanged = onExampleChanged,
                        onImageUrlChanged = onImageUrlChanged,
                        onSaveCard = onSaveCard,
                        onDismissEditor = onDismissEditor,
                    )
                }
            }

            if (uiState.cards.isEmpty()) {
                item {
                    DeckMessageCard(
                        text = if (uiState.cardSearchQuery.isBlank()) {
                            "Chưa có thẻ nào. Hãy thêm thẻ đầu tiên để bắt đầu học."
                        } else {
                            "Không có thẻ nào khớp với \"${uiState.cardSearchQuery}\"."
                        },
                    )
                }
            } else {
                items(uiState.cards, key = { it.id }) { card ->
                    DeckCardItem(
                        card = card,
                        onEditCard = onEditCard,
                        onDeleteCard = onDeleteCard,
                        onToggleCardStar = onToggleCardStar,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeckMetaEditorPanel(
    uiState: DeckDetailUiState,
    onDeckTitleChanged: (String) -> Unit,
    onDeckDescriptionChanged: (String) -> Unit,
    onSaveDeck: () -> Unit,
    onDismissDeckEditor: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Sửa bộ thẻ",
                style = MaterialTheme.typography.headlineSmall,
                color = DeckTextColor,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = uiState.deckTitle,
                onValueChange = onDeckTitleChanged,
                label = { Text("Tên bộ thẻ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            )
            OutlinedTextField(
                value = uiState.deckDescription,
                onValueChange = onDeckDescriptionChanged,
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(18.dp),
            )
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextAction(label = "Lưu bộ thẻ", onClick = onSaveDeck, color = DeckTextColor)
                TextAction(label = "Hủy", onClick = onDismissDeckEditor, color = DeckTextColor)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DeckEditorPanel(
    uiState: DeckDetailUiState,
    onFrontChanged: (String) -> Unit,
    onBackChanged: (String) -> Unit,
    onPronunciationChanged: (String) -> Unit,
    onExampleChanged: (String) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onSaveCard: () -> Unit,
    onDismissEditor: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (uiState.isEditing) "Sửa thẻ" else "Tạo thẻ",
                style = MaterialTheme.typography.headlineSmall,
                color = DeckTextColor,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = uiState.front,
                onValueChange = onFrontChanged,
                label = { Text("Mặt trước") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            )
            OutlinedTextField(
                value = uiState.back,
                onValueChange = onBackChanged,
                label = { Text("Mặt sau") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            )
            OutlinedTextField(
                value = uiState.pronunciation,
                onValueChange = onPronunciationChanged,
                label = { Text("Phát âm") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            )
            OutlinedTextField(
                value = uiState.exampleSentence,
                onValueChange = onExampleChanged,
                label = { Text("Câu ví dụ") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(18.dp),
            )
            OutlinedTextField(
                value = uiState.imageUrl,
                onValueChange = onImageUrlChanged,
                label = { Text("Link ảnh") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            )
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextAction(label = "Lưu", onClick = onSaveCard)
                TextAction(label = "Hủy", onClick = onDismissEditor)
            }
        }
    }
}

@Composable
private fun DeckMessageCard(text: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = DeckTextColor,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DeckCardItem(
    card: VocabularyCard,
    onEditCard: (VocabularyCard) -> Unit,
    onDeleteCard: (String) -> Unit,
    onToggleCardStar: (String) -> Unit,
) {
    var isFrontVisible by remember(card.id) { mutableStateOf(true) }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(74.dp)
                    .background(
                        color = if (card.isStarred) Color(0xFFFF8A5B) else Color(0xFF8A6BFF),
                        shape = RoundedCornerShape(100.dp),
                    ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                card.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = card.front,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    )
                }
                Text(
                    text = if (isFrontVisible) card.front else card.back,
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeckTextColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (isFrontVisible) card.back else card.front,
                    style = MaterialTheme.typography.titleMedium,
                    color = DeckTextColor,
                )
                card.pronunciation?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = DeckTextColor)
                }
                card.exampleSentence?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = DeckTextColor)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextAction(
                        label = if (isFrontVisible) "Dao mat" else "Ve mat truoc",
                        onClick = { isFrontVisible = !isFrontVisible },
                        color = DeckTextColor,
                    )
                    TextAction(
                        label = if (card.isStarred) "Bỏ sao" else "Gắn sao",
                        onClick = { onToggleCardStar(card.id) },
                        color = DeckTextColor,
                    )
                    TextAction(label = "Sửa", onClick = { onEditCard(card) }, color = DeckTextColor)
                    TextAction(label = "Xóa", onClick = { onDeleteCard(card.id) }, color = DeckTextColor)
                }
            }
        }
    }
}

@Composable
private fun DeckSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = DeckTextColor,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun FilterAction(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        modifier = Modifier.clickable(onClick = onClick),
        style = MaterialTheme.typography.titleMedium,
        color = DeckTextColor,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        textDecoration = if (selected) TextDecoration.Underline else TextDecoration.None,
    )
}

@Composable
internal fun TextAction(
    label: String,
    onClick: () -> Unit,
    color: Color = DeckTextColor,
) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun HeaderActionChip(
    label: String,
    onClick: () -> Unit,
    filled: Boolean = false,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (filled) Color(0xFFF0D9A6) else Color(0x22FFFFFF),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            color = if (filled) DeckTextColor else Color(0xFFFFFCF6),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun createDeckExportJson(
    title: String,
    description: String,
    cards: List<VocabularyCard>,
): String {
    val cardsJson = JSONArray()
    cards.forEach { card ->
        cardsJson.put(
            JSONObject()
                .put("front", card.front)
                .put("back", card.back)
                .put("pronunciation", card.pronunciation ?: "")
                .put("exampleSentence", card.exampleSentence ?: "")
                .put("imageUrl", card.imageUrl ?: "")
                .put("isStarred", card.isStarred),
        )
    }
    return JSONObject()
        .put("title", title)
        .put("description", description)
        .put("cards", cardsJson)
        .toString(2)
}
