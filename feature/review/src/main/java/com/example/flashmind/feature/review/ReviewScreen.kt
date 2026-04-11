package com.example.flashmind.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.flashmind.core.model.ReviewGrade
internal val StudyBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF4F1EA),
        Color(0xFFE9E2D6),
        Color(0xFFDCE9E5),
    ),
)

private val StudyHeaderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF102A43),
        Color(0xFF183C5A),
        Color(0xFF1B666F),
    ),
)

private val StudyProgressCardColor = Color(0xFFF2E6CE)
private val StudyPanelColor = Color(0xFFFFFCF6)
internal val StudyTextColor = Color(0xFF111111)
private val StudyMutedText = Color(0xFF61584C)

@Composable
fun ReviewScreen(
    state: State<ReviewUiState>,
    onGrade: (ReviewGrade) -> Unit,
    onBack: () -> Unit,
    onRevealAnswer: () -> Unit,
) {
    val uiState by state
    val card = uiState.currentCard

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
            item {
                StudyHeader(
                    title = "Ôn tập",
                    subtitle = "Đã ôn ${uiState.reviewedCount} thẻ",
                    onBack = onBack,
                )
            }
            item {
                StudyProgressCard(
                    title = "Thẻ còn lại",
                    value = (card?.let { uiState.cards.size } ?: 0).toString(),
                )
            }
            uiState.error?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (card == null) {
                item {
                    StudyPanel {
                        Text(
                            text = if (uiState.sessionTotal > 0) {
                                "Phiên học hoàn tất. Bạn đã ôn ${uiState.reviewedCount} thẻ."
                            } else {
                                "Hiện không có thẻ nào đến hạn."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = StudyTextColor,
                        )
                    }
                }
            } else {
                item {
                    StudyPanel {
                        Text(
                            text = "${uiState.reviewedCount + 1}/${maxOf(uiState.sessionTotal, uiState.cards.size)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = StudyMutedText,
                        )
                        Text(
                            text = "Câu hỏi",
                            style = MaterialTheme.typography.labelLarge,
                            color = StudyTextColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = card.front,
                            style = MaterialTheme.typography.headlineMedium,
                            color = StudyTextColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (uiState.revealAnswer) {
                            StudyDivider()
                            Text(card.back, style = MaterialTheme.typography.titleLarge, color = StudyTextColor)
                            card.pronunciation?.let {
                                Text(it, style = MaterialTheme.typography.bodyLarge, color = StudyMutedText)
                            }
                            card.exampleSentence?.let {
                                Text(it, style = MaterialTheme.typography.bodyLarge, color = StudyMutedText)
                            }
                        } else {
                            StudyPrimaryAction(label = "Hiện đáp án", onClick = onRevealAnswer)
                        }
                    }
                }
                if (uiState.revealAnswer) {
                    item {
                        StudyActionGrid(
                            actions = listOf(
                                "Lại" to { onGrade(ReviewGrade.AGAIN) },
                                "Khó" to { onGrade(ReviewGrade.HARD) },
                                "Tốt" to { onGrade(ReviewGrade.GOOD) },
                                "Dễ" to { onGrade(ReviewGrade.EASY) },
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun StudyHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudyHeaderBrush)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StudyHeaderAction(label = "Quay lại", onClick = onBack)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFFFFCF6),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE7F0F2),
            )
        }
    }
}

@Composable
internal fun StudyProgressCard(
    title: String,
    value: String,
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = StudyProgressCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = StudyTextColor)
            Text(value, style = MaterialTheme.typography.headlineLarge, color = StudyTextColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun StudyPanel(
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StudyPanelColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
internal fun StudyPrimaryAction(
    label: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFFFFCF6),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun StudyActionGrid(
    actions: List<Pair<String, () -> Unit>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowActions.forEach { (label, onClick) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onClick),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(100.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = StudyTextColor,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (rowActions.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun StudyOptionList(
    options: List<Pair<String, () -> Unit>>,
    enabled: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { (label, onClick) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled, onClick = onClick),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(32.dp)
                            .background(
                                color = Color(0xFF8A6BFF),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(100.dp),
                            ),
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (enabled) StudyTextColor else StudyMutedText,
                    )
                }
            }
        }
    }
}

@Composable
internal fun StudyInlineAction(
    label: String,
    onClick: () -> Unit,
    color: Color = StudyTextColor,
) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun StudyHeaderAction(
    label: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, color = StudyTextColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun StudyDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE5DDEA)),
    )
}
