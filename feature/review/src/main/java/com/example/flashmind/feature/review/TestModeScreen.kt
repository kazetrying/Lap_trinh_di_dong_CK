package com.example.flashmind.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TestModeScreen(
    state: State<TestModeUiState>,
    onBack: () -> Unit,
    onPracticeWrongOnlyChanged: (Boolean) -> Unit,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
) {
    val uiState by state
    val question = uiState.currentQuestion

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
                    title = "Kiểm tra",
                    subtitle = "Điểm ${uiState.score} / ${uiState.questions.size}",
                    onBack = onBack,
                )
            }
            item {
                StudyProgressCard(
                    title = if (question == null) "Câu hỏi" else "Câu hiện tại",
                    value = if (question == null) uiState.questions.size.toString() else uiState.currentQuestionNumber.toString(),
                )
            }
            if (question == null) {
                item {
                    StudyPanel {
                        Text(
                            text = if (uiState.questions.isEmpty()) {
                                if (uiState.practiceWrongOnly) "Chua co tu sai de on rieng." else "Cần ít nhất 2 thẻ trong bộ này để bắt đầu kiểm tra."
                            } else {
                                "Hoàn thành bài kiểm tra. Điểm cuối: ${uiState.score} / ${uiState.questions.size}"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = StudyTextColor,
                        )
                        if (uiState.questions.isNotEmpty()) {
                            StudyPrimaryAction(label = "Làm lại", onClick = onRestart)
                        }
                        if (uiState.wrongCount > 0) {
                            StudyInlineAction(
                                label = if (uiState.practiceWrongOnly) "Dang lam lai tu sai" else "Chi kiem tra ${uiState.wrongCount} tu sai",
                                onClick = { onPracticeWrongOnlyChanged(!uiState.practiceWrongOnly) },
                            )
                        }
                    }
                }
            } else {
                item {
                    StudyPanel {
                        Text(
                            text = question.prompt,
                            style = MaterialTheme.typography.headlineMedium,
                            color = StudyTextColor,
                        )
                        if (uiState.wrongCount > 0) {
                            StudyInlineAction(
                                label = if (uiState.practiceWrongOnly) "Tat che do tu sai" else "Chi kiem tra ${uiState.wrongCount} tu sai",
                                onClick = { onPracticeWrongOnlyChanged(!uiState.practiceWrongOnly) },
                            )
                        }
                        StudyOptionList(
                            options = question.options.map { option -> option to { onAnswer(option) } },
                            enabled = uiState.selectedAnswer == null,
                        )
                        when (uiState.answerState) {
                            AnswerState.CORRECT -> Text("Chính xác", color = StudyTextColor, style = MaterialTheme.typography.titleMedium)
                            AnswerState.WRONG -> Text(
                                "Sai. Đáp án đúng: ${question.correctAnswer}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            AnswerState.IDLE -> Unit
                        }
                        if (uiState.selectedAnswer != null) {
                            StudyPrimaryAction(label = "Tiếp theo", onClick = onNext)
                        }
                    }
                }
            }
        }
    }
}
