package com.example.flashcardapp.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardapp.util.TtsHelper
import com.example.flashcardapp.viewmodel.CardViewModel

@Composable
fun StudyScreen(
    deckId: Long,
    deckName: String,
    viewModel: CardViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    DisposableEffect(Unit) { onDispose { ttsHelper.shutdown() } }

    LaunchedEffect(deckId) {
        viewModel.resetStudy()
        viewModel.loadDueCards(deckId)
    }

    val currentCard   by viewModel.currentCard.collectAsState()
    val dueCards      by viewModel.dueCards.collectAsState()
    val currentIndex  by viewModel.currentIndex.collectAsState()
    val studyFinished by viewModel.studyFinished.collectAsState()

    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "flip"
    )

    LaunchedEffect(currentCard?.id) {
        isFlipped = false
        currentCard?.let { ttsHelper.speak(it.front) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (studyFinished) {
            FinishedScreen(onBack = onBack)
        } else {
            val card = currentCard
            if (card != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { ttsHelper.stop(); onBack() },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Thoát", tint = Color.White)
                            }
                            Spacer(Modifier.weight(1f))
                            Text(deckName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Text("${currentIndex + 1}/${dueCards.size}", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                        }
                    }

                    LinearProgressIndicator(
                        progress = {
                            if (dueCards.isEmpty()) 0f
                            else (currentIndex + 1).toFloat() / dueCards.size
                        },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )

                    Spacer(Modifier.weight(0.5f))

                    Text(
                        text = if (!isFlipped) "Nhấn thẻ để xem đáp án" else "Chọn mức độ ghi nhớ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(300.dp)
                            .graphicsLayer { rotationY = rotation; cameraDistance = 14f * density }
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                isFlipped = !isFlipped
                                if (isFlipped) ttsHelper.speak(card.back)
                                else ttsHelper.speak(card.front)
                            }
                    ) {
                        if (rotation <= 90f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "CÂU HỎI",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                    }
                                    Spacer(Modifier.height(20.dp))
                                    Text(
                                        text = card.front,
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    IconButton(
                                        onClick = { ttsHelper.speak(card.front) },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = "Đọc lại", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f }
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFFE8F5E9), MaterialTheme.colorScheme.surface)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ColorGood.copy(alpha = 0.15f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "ĐÁP ÁN",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                    }
                                    Spacer(Modifier.height(20.dp))
                                    Text(
                                        text = card.back,
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    IconButton(
                                        onClick = { ttsHelper.speak(card.back) },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ColorGood.copy(alpha = 0.15f))
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = "Đọc lại", tint = Color(0xFF2E7D32))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(0.5f))

                    if (isFlipped) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                RatingBtn(label = "Quên", sublabel = "Không nhớ", color = ColorForgot, modifier = Modifier.weight(1f)) { viewModel.rateCard(0) }
                                RatingBtn(label = "Khó", sublabel = "Nhớ mờ", color = ColorHard, modifier = Modifier.weight(1f)) { viewModel.rateCard(2) }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                RatingBtn(label = "Tốt", sublabel = "Nhớ được", color = ColorGood, modifier = Modifier.weight(1f)) { viewModel.rateCard(4) }
                                RatingBtn(label = "Dễ", sublabel = "Nhớ rõ", color = ColorEasy, modifier = Modifier.weight(1f)) { viewModel.rateCard(5) }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun RatingBtn(label: String, sublabel: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            Text(sublabel, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Composable
fun FinishedScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(52.dp), tint = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Xuất sắc!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Bạn đã ôn tập xong\ntất cả thẻ hôm nay",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Quay lại", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}