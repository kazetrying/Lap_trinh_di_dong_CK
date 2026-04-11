package com.example.flashcardapp.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

@OptIn(ExperimentalMaterial3Api::class)
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

    val primaryColor = Color(0xFF6C63FF)
    val secondaryColor = Color(0xFF4B44CC)
    val headerGradient = Brush.verticalGradient(
        colors = listOf(primaryColor, secondaryColor)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
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
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(headerGradient)
                    ) {
                        Column(modifier = Modifier.padding(bottom = 20.dp)) {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        deckName.uppercase(),
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp,
                                        fontSize = 16.sp
                                    )
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { ttsHelper.stop(); onBack() },
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(40.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White.copy(alpha = 0.2f)
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Thoát", tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                },
                                actions = {
                                    Text(
                                        "${currentIndex + 1}/${dueCards.size}",
                                        modifier = Modifier.padding(end = 20.dp),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black
                                    )
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                            )
                            
                            LinearProgressIndicator(
                                progress = {
                                    if (dueCards.isEmpty()) 0f
                                    else (currentIndex + 1).toFloat() / dueCards.size
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }

                    Spacer(Modifier.weight(0.4f))

                    Text(
                        text = if (!isFlipped) "Nhấn thẻ để xem đáp án" else "Bạn nhớ từ này như thế nào?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(20.dp))

                    // Flashcard
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                            .height(380.dp)
                            .graphicsLayer { rotationY = rotation; cameraDistance = 14f * density }
                            .shadow(20.dp, RoundedCornerShape(32.dp), spotColor = primaryColor.copy(alpha = 0.2f))
                            .clip(RoundedCornerShape(32.dp))
                            .clickable {
                                isFlipped = !isFlipped
                                if (isFlipped) ttsHelper.speak(card.back)
                                else ttsHelper.speak(card.front)
                            }
                    ) {
                        if (rotation <= 90f) {
                            // Front Side
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        color = primaryColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "CÂU HỎI",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = primaryColor,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 2.sp
                                        )
                                    }
                                    Spacer(Modifier.height(32.dp))
                                    Text(
                                        text = card.front,
                                        style = MaterialTheme.typography.headlineMedium,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1A1C1E),
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                    Spacer(Modifier.height(40.dp))
                                    Surface(
                                        onClick = { ttsHelper.speak(card.front) },
                                        shape = CircleShape,
                                        color = primaryColor.copy(alpha = 0.05f),
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = primaryColor, modifier = Modifier.size(32.dp))
                                        }
                                    }
                                }
                            }
                        } else {
                            // Back Side
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f }
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "ĐÁP ÁN",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 2.sp
                                        )
                                    }
                                    Spacer(Modifier.height(32.dp))
                                    Text(
                                        text = card.back,
                                        style = MaterialTheme.typography.headlineMedium,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1A1C1E),
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                    Spacer(Modifier.height(40.dp))
                                    Surface(
                                        onClick = { ttsHelper.speak(card.back) },
                                        shape = CircleShape,
                                        color = Color(0xFF4CAF50).copy(alpha = 0.05f),
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(0.6f))

                    // Action Buttons
                    if (isFlipped) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RatingBtn(label = "Quên", sublabel = "Học lại", color = Color(0xFFFF5252), modifier = Modifier.weight(1f)) { viewModel.rateCard(0) }
                                RatingBtn(label = "Khó", sublabel = "Nhớ mờ", color = Color(0xFFFFAB40), modifier = Modifier.weight(1f)) { viewModel.rateCard(2) }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RatingBtn(label = "Tốt", sublabel = "Ổn định", color = Color(0xFF448AFF), modifier = Modifier.weight(1f)) { viewModel.rateCard(4) }
                                RatingBtn(label = "Dễ", sublabel = "Nhớ rõ", color = Color(0xFF4CAF50), modifier = Modifier.weight(1f)) { viewModel.rateCard(5) }
                            }
                        }
                    } else {
                        Spacer(Modifier.height(140.dp)) // Maintain space for buttons
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBtn(label: String, sublabel: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp).shadow(8.dp, RoundedCornerShape(20.dp), spotColor = color.copy(alpha = 0.4f)),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
            Text(sublabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun FinishedScreen(onBack: () -> Unit) {
    val primaryColor = Color(0xFF6C63FF)
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            color = primaryColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.EmojiEvents, 
                    contentDescription = null, 
                    modifier = Modifier.size(80.dp), 
                    tint = primaryColor
                )
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(
            "XUẤT SẮC!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1A1C1E),
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Bạn đã hoàn thành việc ôn tập\ntất cả thẻ ghi nhớ cho hôm nay.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = primaryColor.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("QUAY LẠI TRANG CHỦ", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}
