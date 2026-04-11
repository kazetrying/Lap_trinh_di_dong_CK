package com.example.flashmind.feature.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeckChatScreen(
    state: State<DeckChatUiState>,
    onBack: () -> Unit,
    onPromptChanged: (String) -> Unit,
    onSend: () -> Unit,
    onQuickPrompt: (String) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeaderBackground)
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextAction("Quay lại", onBack, color = Color(0xFFFFFCF6))
                    Text(
                        text = "Trợ lý AI",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFFFFFCF6),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (uiState.deckTitle.isBlank()) {
                            "Trợ lý học tập theo bộ thẻ"
                        } else {
                            "Chat với trợ lý học tập cho bộ thẻ ${uiState.deckTitle}"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFE7F0F2),
                    )
                }
            }

            if (uiState.quickPrompts.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    uiState.quickPrompts.forEach { prompt ->
                        ChatPromptChip(prompt = prompt, onClick = { onQuickPrompt(prompt) })
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.prompt,
                        onValueChange = onPromptChanged,
                        label = { Text("Hỏi về bộ thẻ này") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(18.dp),
                    )
                    TextAction(label = "Gửi tin nhắn", onClick = onSend, color = DeckTextColor)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF102A43) else Color(0xFFFFFCF6),
            ),
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = if (isUser) 24.dp else 8.dp,
                bottomEnd = if (isUser) 8.dp else 24.dp,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUser) Color(0xFFFFFCF6) else DeckTextColor,
            )
        }
    }
}

@Composable
private fun ChatPromptChip(
    prompt: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2E6CE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = prompt,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = DeckTextColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
