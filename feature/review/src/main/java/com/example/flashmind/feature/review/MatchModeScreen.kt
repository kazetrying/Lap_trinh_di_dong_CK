package com.example.flashmind.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchModeScreen(
    state: State<MatchModeUiState>,
    onBack: () -> Unit,
    onSelectPrompt: (String) -> Unit,
    onSelectAnswer: (String) -> Unit,
    onRestart: () -> Unit,
) {
    val uiState by state
    val remainingPairs = uiState.remainingPairs

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyBackgroundBrush),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                StudyHeader(
                    title = "Ghép cặp",
                    subtitle = "Đã ghép ${uiState.matchedIds.size} / ${uiState.pairs.size}",
                    onBack = onBack,
                )
            }
            item {
                StudyProgressCard(title = "Cặp còn lại", value = remainingPairs.size.toString())
            }
            if (uiState.pairs.isEmpty()) {
                item {
                    StudyPanel {
                        Text("Cần ít nhất 1 thẻ để bắt đầu chế độ ghép cặp.", color = StudyTextColor)
                    }
                }
            } else if (remainingPairs.isEmpty()) {
                item {
                    StudyPanel {
                        Text("Bạn đã ghép đúng toàn bộ cặp.", color = StudyTextColor, style = MaterialTheme.typography.bodyLarge)
                        StudyPrimaryAction(label = "Chơi lại", onClick = onRestart)
                    }
                }
            } else {
                uiState.lastResult?.let {
                    item {
                        Text(
                            text = it,
                            color = if (it == "Ghép đúng") StudyTextColor else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                item {
                    StudyPanel {
                        Text("Chọn từ hoặc câu hỏi", style = MaterialTheme.typography.titleLarge, color = StudyTextColor)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            remainingPairs.shuffled().forEach { pair ->
                                StudyPrimaryAction(label = pair.prompt, onClick = { onSelectPrompt(pair.id) })
                            }
                        }
                    }
                }
                item {
                    StudyPanel {
                        Text("Chọn nghĩa tương ứng", style = MaterialTheme.typography.titleLarge, color = StudyTextColor)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            remainingPairs.shuffled().forEach { pair ->
                                StudyPrimaryAction(label = pair.answer, onClick = { onSelectAnswer(pair.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}
