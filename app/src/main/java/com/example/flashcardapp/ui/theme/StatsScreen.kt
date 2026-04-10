package com.example.flashcardapp.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardapp.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    deckId: Long,
    deckName: String,
    viewModel: CardViewModel,
    onBack: () -> Unit
) {
    val cards by viewModel.getCardsByDeck(deckId).collectAsState(initial = emptyList())
    val dueCount by viewModel.getDueCardCount(deckId).collectAsState(initial = 0)

    val totalCards = cards.size
    val masteredCards = cards.count { it.repetition >= 3 }
    val learningCards = cards.count { it.repetition in 1..2 }
    val newCards = cards.count { it.repetition == 0 }
    val avgEF = if (cards.isEmpty()) 0f
    else cards.map { it.easeFactor }.average().toFloat()

    val masteryProgress = if (totalCards > 0) masteredCards.toFloat() / totalCards else 0f

    val primaryColor = Color(0xFF6C63FF)
    val secondaryColor = Color(0xFF4B44CC)
    val headerGradient = Brush.verticalGradient(
        colors = listOf(primaryColor, secondaryColor)
    )

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(headerGradient)
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                "THỐNG KÊ CHI TIẾT",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp,
                                fontSize = 16.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            deckName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card with Mastery Chart
            item {
                MasteryOverviewCard(masteryProgress, deckName, totalCards)
            }

            // Stat Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModernStatTile(
                        value = dueCount.toString(),
                        label = "Cần ôn",
                        icon = Icons.Rounded.History,
                        color = Color(0xFFFF5252),
                        modifier = Modifier.weight(1f)
                    )
                    ModernStatTile(
                        value = "%.1f".format(avgEF),
                        label = "Độ khó",
                        icon = Icons.Rounded.Psychology,
                        color = Color(0xFF6C63FF),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Status Distribution
            item {
                DistributionCard(newCards, learningCards, masteredCards)
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp, 20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(primaryColor)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Phân tích từng thẻ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C1E)
                    )
                }
            }

            if (cards.isEmpty()) {
                item {
                    Text(
                        "Chưa có dữ liệu thống kê",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                items(cards, key = { it.id }) { card ->
                    EnhancedCardItem(card)
                }
            }
        }
    }
}

@Composable
fun MasteryOverviewCard(progress: Float, deckName: String, total: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000), label = "mastery_anim"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                Canvas(modifier = Modifier.size(90.dp)) {
                    drawArc(
                        color = Color(0xFFF0F2F8),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Color(0xFF6C63FF), Color(0xFF4B44CC))),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(0xFF6C63FF)
                )
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text(
                    "Mức độ thông thạo",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Đạt được ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1C1E)
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF6C63FF).copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        "Tổng cộng $total thẻ",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6C63FF)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernStatTile(value: String, label: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1C1E))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@Composable
fun DistributionCard(new: Int, learning: Int, mastered: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Trạng thái học tập", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF1A1C1E))
            Spacer(Modifier.height(20.dp))
            
            Row(modifier = Modifier.height(12.dp).fillMaxWidth().clip(CircleShape)) {
                val total = (new + learning + mastered).coerceAtLeast(1).toFloat()
                Box(Modifier.weight((new / total).coerceAtLeast(0.01f)).fillMaxHeight().background(Color(0xFF40C4FF)))
                Box(Modifier.weight((learning / total).coerceAtLeast(0.01f)).fillMaxHeight().background(Color(0xFFFFAB40)))
                Box(Modifier.weight((mastered / total).coerceAtLeast(0.01f)).fillMaxHeight().background(Color(0xFF69F0AE)))
            }
            
            Spacer(Modifier.height(20.dp))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                LegendItem("Mới", new, Color(0xFF40C4FF))
                LegendItem("Đang học", learning, Color(0xFFFFAB40))
                LegendItem("Thuộc", mastered, Color(0xFF69F0AE))
            }
        }
    }
}

@Composable
fun LegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(4.dp))
        Text(count.toString(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1C1E))
    }
}

@Composable
fun EnhancedCardItem(card: com.example.flashcardapp.data.Card) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF6C63FF).copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        card.front.take(1).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF6C63FF),
                        fontSize = 20.sp
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        card.front, 
                        fontWeight = FontWeight.Black, 
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        card.back, 
                        color = Color.Gray, 
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniChip(
                    text = "Lặp: ${card.repetition}",
                    color = Color(0xFFF1F2F6),
                    textColor = Color.DarkGray
                )
                MiniChip(
                    text = "EF: ${"%.1f".format(card.easeFactor)}",
                    color = if (card.easeFactor >= 2.5f) Color(0xFF69F0AE).copy(alpha = 0.15f) else Color(0xFFFFAB40).copy(alpha = 0.15f),
                    textColor = if (card.easeFactor >= 2.5f) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
            }
        }
    }
}

@Composable
fun MiniChip(text: String, color: Color, textColor: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = textColor
        )
    }
}
