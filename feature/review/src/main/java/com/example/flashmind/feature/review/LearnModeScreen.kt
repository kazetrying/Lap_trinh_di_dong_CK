package com.example.flashmind.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LearnModeScreen(
    state: State<LearnModeUiState>,
    onBack: () -> Unit,
    onPracticeWrongOnlyChanged: (Boolean) -> Unit,
    onAnswerChanged: (String) -> Unit,
    onCheckAnswer: () -> Unit,
    onRevealAnswer: () -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
) {
    val uiState by state
    val card = uiState.currentCard

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
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
                    title = "Học",
                    subtitle = "Dung ${uiState.correctCount} / ${uiState.totalCount}",
                    onBack = onBack,
                )
            }
            item {
                StudyProgressCard(
                    title = "Thẻ hiện tại",
                    value = if (card == null) "0" else "${uiState.completedCount + 1}",
                )
            }
            if (card == null) {
                item {
                    StudyPanel {
                        Text(
                            text = if (uiState.totalCount == 0) {
                                "Cần ít nhất 1 thẻ để bắt đầu chế độ học."
                            } else {
                                "Bạn đã hoàn thành phiên học."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = StudyTextColor,
                        )
                        if (uiState.wrongCount > 0) {
                            StudyInlineAction(
                                label = if (uiState.practiceWrongOnly) "Dang hoc tu sai" else "Chi hoc ${uiState.wrongCount} tu sai",
                                onClick = { onPracticeWrongOnlyChanged(!uiState.practiceWrongOnly) },
                            )
                        }
                        if (uiState.totalCount > 0) {
                            StudyPrimaryAction(label = "Học lại", onClick = onRestart)
                        }
                    }
                }
            } else {
                item {
                    StudyPanel {
                        Text(
                            text = "Câu hỏi",
                            style = MaterialTheme.typography.labelLarge,
                            color = StudyTextColor,
                        )
                        Text(
                            text = card.front,
                            style = MaterialTheme.typography.headlineMedium,
                            color = StudyTextColor,
                        )
                        if (uiState.wrongCount > 0) {
                            StudyInlineAction(
                                label = if (uiState.practiceWrongOnly) "Tat che do tu sai" else "Hoc rieng ${uiState.wrongCount} tu sai",
                                onClick = { onPracticeWrongOnlyChanged(!uiState.practiceWrongOnly) },
                            )
                        }
                        OutlinedTextField(
                            value = uiState.answer,
                            onValueChange = onAnswerChanged,
                            label = { Text("Nhập nghĩa") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        uiState.feedback?.let { feedback ->
                            Text(
                                text = feedback,
                                color = if (feedback == "Dung") StudyTextColor else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        if (uiState.isAnswerRevealed) {
                            StudyPrimaryAction(label = "Thẻ tiếp theo", onClick = onNext)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StudyPrimaryAction(label = "Kiểm tra", onClick = onCheckAnswer)
                                StudyInlineAction(label = "Hiện đáp án", onClick = onRevealAnswer)
                            }
                        }
                    }
                }
            }
        }
    }
}
