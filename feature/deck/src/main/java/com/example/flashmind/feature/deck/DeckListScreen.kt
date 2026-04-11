package com.example.flashmind.feature.deck

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val HomeBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF4F1EA),
        Color(0xFFEDE7DA),
        Color(0xFFE4ECE8),
    ),
)

private val HomeHeaderBackground = Brush.linearGradient(
    colors = listOf(
        Color(0xFF102A43),
        Color(0xFF183C5A),
        Color(0xFF1B666F),
    ),
)

private val HomeStatBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFCF6),
        Color(0xFFF2E6CE),
    ),
)

private val HomeHistoryBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFCF6),
        Color(0xFFDDEAE6),
    ),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeckListScreen(
    state: State<DeckListUiState>,
    currentUserEmail: String?,
    useDarkTheme: Boolean,
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onOpenDeck: (String) -> Unit,
    onCreateDeck: () -> Unit,
    onAiCreateDeck: () -> Unit,
    onImportDeck: () -> Unit,
    onDeleteDeck: (String) -> Unit,
    onLogout: () -> Unit,
    onSyncNow: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSortOrderChanged: (DeckSortOrder) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onReminderEnabledChanged: (Boolean) -> Unit,
    onReminderHourShift: (Int) -> Unit,
    onReminderMinuteShift: (Int) -> Unit,
) {
    val uiState by state

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
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(18.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(34.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HomeHeaderBackground)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "Flashcard",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFFFFFCF6),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = currentUserEmail?.let { "Xin chào $it. Đến giờ học rồi." }
                                ?: "Đến giờ học rồi.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFE7F0F2),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HomeActionPill(label = "Tạo bộ thẻ", onClick = onCreateDeck, filled = true)
                            HomeActionPill(label = "AI tạo deck", onClick = onAiCreateDeck)
                            HomeActionPill(label = "Nhập", onClick = onImportDeck)
                            HomeActionPill(
                                label = if (uiState.isSyncing) "Đang đồng bộ" else "Đồng bộ",
                                onClick = onSyncNow,
                            )
                            HomeActionPill(label = "Đăng xuất", onClick = onLogout)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Đến hạn",
                        value = uiState.totalDueCount.toString(),
                        brush = HomeStatBackground,
                    )
                    DashboardStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Chuỗi học",
                        value = "${uiState.analytics.currentStreakDays}d",
                        brush = HomeHistoryBackground,
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Tổng quan",
                            style = MaterialTheme.typography.titleLarge,
                            color = DeckTextColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            HistoryMetric(label = "Hôm nay", value = uiState.analytics.todayReviews.toString())
                            HistoryMetric(label = "Tổng ôn tập", value = uiState.analytics.totalReviews.toString())
                            HistoryMetric(label = "Bộ thẻ", value = uiState.totalDeckCount.toString())
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    label = { Text("Tìm bộ thẻ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HomeActionPill(
                        label = "A-Z",
                        onClick = { onSortOrderChanged(DeckSortOrder.NAME_ASC) },
                        filled = uiState.sortOrder == DeckSortOrder.NAME_ASC,
                    )
                    HomeActionPill(
                        label = "Z-A",
                        onClick = { onSortOrderChanged(DeckSortOrder.NAME_DESC) },
                        filled = uiState.sortOrder == DeckSortOrder.NAME_DESC,
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Cài đặt",
                            style = MaterialTheme.typography.titleLarge,
                            color = DeckTextColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HomeActionPill(
                                label = if (useDarkTheme) "Dark mode: Bật" else "Dark mode: Tắt",
                                onClick = { onDarkThemeChanged(!useDarkTheme) },
                                filled = useDarkTheme,
                            )
                            HomeActionPill(
                                label = if (reminderEnabled) "Nhắc học: Bật" else "Nhắc học: Tắt",
                                onClick = { onReminderEnabledChanged(!reminderEnabled) },
                                filled = reminderEnabled,
                            )
                        }
                        Text(
                            text = "Giờ nhắc: ${reminderHour.toString().padStart(2, '0')}:${reminderMinute.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DeckTextColor,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HomeActionPill(label = "-1h", onClick = { onReminderHourShift(-1) })
                            HomeActionPill(label = "+1h", onClick = { onReminderHourShift(1) })
                            HomeActionPill(label = "-15m", onClick = { onReminderMinuteShift(-15) })
                            HomeActionPill(label = "+15m", onClick = { onReminderMinuteShift(15) })
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Không gian học",
                    style = MaterialTheme.typography.titleLarge,
                    color = DeckTextColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (uiState.pendingSyncCount > 0) {
                item {
                    Text(
                        text = "${uiState.pendingSyncCount} tác vụ đang chờ đồng bộ lên hệ thống.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF775E2A),
                    )
                }
            }

            uiState.error?.let { error ->
                item {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (uiState.decks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isBlank()) {
                                "Chưa có bộ thẻ nào. Tạo bộ thẻ đầu tiên để bắt đầu."
                            } else {
                                "Không tìm thấy bộ thẻ khớp với \"${uiState.searchQuery}\"."
                            },
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = DeckTextColor,
                        )
                    }
                }
            }

            items(uiState.decks, key = { it.id }) { deck ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDeck(deck.id) },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(deckAccentColor(deck.title), CircleShape)
                                    .width(14.dp)
                                    .height(14.dp),
                            )
                            Text(
                                text = deck.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = DeckTextColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        if (deck.description.isNotBlank()) {
                            Text(
                                text = deck.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF5A5248),
                                maxLines = 2,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${deck.cardCount} thẻ • ${deck.dueCount} thẻ đến hạn",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF6B6256),
                            )
                            TextButton(onClick = { onDeleteDeck(deck.id) }, contentPadding = PaddingValues(0.dp)) {
                                Text(
                                    text = "Xóa",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    modifier: Modifier,
    title: String,
    value: String,
    brush: Brush,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush)
                .padding(vertical = 18.dp, horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = Color(0xFF5E564A))
            AnimatedContent(targetState = value, label = title) { animatedValue ->
                Text(
                    text = animatedValue,
                    style = MaterialTheme.typography.headlineMedium,
                    color = DeckTextColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun HistoryMetric(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7B7266),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = DeckTextColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun HomeActionPill(
    label: String,
    onClick: () -> Unit,
    filled: Boolean = false,
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (filled) Color(0xFFF0D9A6) else Color(0x22FFFFFF),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (filled) DeckTextColor else Color(0xFFFFFCF6),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun deckAccentColor(title: String): Color {
    return when (title.lowercase().firstOrNull()) {
        in 'a'..'f' -> Color(0xFF1F6B70)
        in 'g'..'m' -> Color(0xFFDAA94D)
        in 'n'..'s' -> Color(0xFF2B4C7E)
        else -> Color(0xFF8F5E3B)
    }
}

