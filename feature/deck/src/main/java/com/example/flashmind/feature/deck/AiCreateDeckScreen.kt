package com.example.flashmind.feature.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AiCreateDeckScreen(
    state: State<AiCreateDeckUiState>,
    onTopicChanged: (String) -> Unit,
    onBack: () -> Unit,
    onCreate: () -> Unit,
) {
    val uiState by state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckScreenBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeaderBackground)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextAction(label = "Quay lại", onClick = onBack, color = Color(0xFFFFFCF6))
                    Text(
                        "AI tạo deck",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFFFFFCF6),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Nhập một chủ đề như du lịch, công sở, giao tiếp hoặc chủ đề bất kỳ. App sẽ tự tạo sẵn deck và card để bạn học ngay.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFE7F0F2),
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.topic,
                        onValueChange = onTopicChanged,
                        label = { Text("Chủ đề deck") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                    )
                    Text(
                        "Ví dụ: giao tiếp hằng ngày, travel, công sở, food, IELTS speaking",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeckTextColor,
                    )
                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    TextAction(label = "Tạo deck bằng AI", onClick = onCreate)
                }
            }
        }
    }
}
