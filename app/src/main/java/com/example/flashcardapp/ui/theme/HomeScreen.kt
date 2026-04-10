package com.example.flashcardapp.ui.theme

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardapp.data.Deck
import com.example.flashcardapp.viewmodel.CardViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CardViewModel,
    onDeckClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    val decks by viewModel.allDecks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    
    // Lấy thông tin người dùng từ Firebase
    val user = FirebaseAuth.getInstance().currentUser
    val userName = remember(user) {
        if (!user?.displayName.isNullOrBlank()) {
            user?.displayName
        } else if (!user?.email.isNullOrBlank()) {
            user?.email?.substringBefore("@")
        } else {
            "Người dùng"
        } ?: "Người dùng"
    }

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
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(headerGradient)
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "FLASHCARD MASTER",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp,
                                fontSize = 18.sp
                            )
                        },
                        actions = {
                            IconButton(onClick = onSettingsClick) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Chào ngày mới,",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "$userName! ✨",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Surface(
                            onClick = onLogout,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Logout,
                                null,
                                tint = Color.White,
                                modifier = Modifier.padding(14.dp).size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(72.dp)
                    .shadow(12.dp, CircleShape, spotColor = primaryColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(38.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp, 24.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(primaryColor)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Bộ sưu tập của bạn",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C1E)
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        color = primaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "${decks.size} bộ",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (decks.isEmpty()) {
                item { EnhancedHomeEmptyState() }
            } else {
                items(decks) { deck ->
                    EnhancedDeckItem(
                        deck = deck,
                        onClick = { onDeckClick(deck.id) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    if (showDialog) {
        ModernAddDeckDialog(
            onConfirm = { name, desc ->
                viewModel.addDeck(name, desc)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun EnhancedDeckItem(deck: Deck, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFF6C63FF).copy(alpha = 0.1f)),
        shape = RoundedCornerShape(32.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBackground = Color(0xFF6C63FF).copy(alpha = 0.08f)
            val iconColor = Color(0xFF6C63FF)
            
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        deck.name.contains("Anh", true) -> Icons.Rounded.Translate
                        deck.name.contains("Code", true) -> Icons.Rounded.Code
                        else -> Icons.Rounded.AutoStories
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1C1E),
                    fontSize = 18.sp
                )
                Text(
                    text = deck.description.ifEmpty { "Nhấn để bắt đầu ôn tập" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
            
            Surface(
                shape = CircleShape,
                color = Color(0xFFF5F7FA),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedHomeEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(180.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.HistoryEdu,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = Color(0xFF6C63FF).copy(alpha = 0.2f)
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Chưa có bộ thẻ nào!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1A1C1E)
        )
        Text(
            "Hãy bắt đầu tạo những thẻ ghi nhớ đầu tiên\nđể rèn luyện trí nhớ của bạn mỗi ngày.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 40.dp, top = 12.dp, end = 40.dp),
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ModernAddDeckDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(40.dp),
        containerColor = Color.White,
        modifier = Modifier.padding(8.dp),
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, desc) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("Tạo ngay", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        },
        title = { 
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFF6C63FF).copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.CreateNewFolder, null, tint = Color(0xFF6C63FF), modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Thêm bộ thẻ mới", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 22.sp,
                    color = Color(0xFF1A1C1E)
                ) 
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên bộ thẻ (ví dụ: Tiếng Anh)") },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E)
                    )
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Mô tả ngắn gọn") },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E)
                    )
                )
            }
        }
    )
}
