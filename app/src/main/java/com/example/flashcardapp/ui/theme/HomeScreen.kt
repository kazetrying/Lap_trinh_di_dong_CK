package com.example.flashcardapp.ui.theme

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardapp.data.Deck
import com.example.flashcardapp.viewmodel.CardViewModel
import com.example.flashcardapp.viewmodel.SyncStatus
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CardViewModel,
    onDeckClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    val decks      by viewModel.allDecks.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tạo bộ thẻ") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                HomeHeader(
                    deckCount       = decks.size,
                    syncStatus      = syncStatus,
                    onSettingsClick = onSettingsClick,
                    onLogout        = onLogout,
                    onSync          = { viewModel.pullFromCloud() },
                    onPush          = { viewModel.pushAllToCloud() }
                )
            }

            // ✅ Hiển thị trạng thái đồng bộ
            item {
                AnimatedVisibility(
                    visible = syncStatus is SyncStatus.Syncing || syncStatus is SyncStatus.Error,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    when (syncStatus) {
                        is SyncStatus.Syncing -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Đang đồng bộ...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        is SyncStatus.Error -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Đồng bộ thất bại: ${(syncStatus as SyncStatus.Error).message}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }

            if (decks.isEmpty()) {
                item { EmptyState() }
            }

            items(decks, key = { it.id }) { deck ->
                DeckItem(
                    deck      = deck,
                    viewModel = viewModel,
                    onClick   = { onDeckClick(deck.id) },
                    onDelete  = { viewModel.deleteDeck(deck) }
                )
            }
        }
    }

    if (showDialog) {
        AddDeckDialog(
            onConfirm = { name, desc ->
                viewModel.addDeck(name, desc)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun HomeHeader(
    deckCount: Int,
    syncStatus: SyncStatus,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    onSync: () -> Unit,
    onPush: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val email = currentUser?.email ?: ""
    val isSyncing = syncStatus is SyncStatus.Syncing

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
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        // Nút góc trên phải
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ✅ Nút đẩy dữ liệu local lên cloud
            IconButton(
                onClick = onPush,
                enabled = !isSyncing,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = "Đẩy lên cloud",
                    tint = Color.White
                )
            }
            // ✅ Nút kéo dữ liệu từ cloud về
            IconButton(
                onClick = onSync,
                enabled = !isSyncing,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Default.CloudDownload,
                    contentDescription = "Kéo từ cloud",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = "Đăng xuất",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Cài đặt",
                    tint = Color.White
                )
            }
        }

        Column {
            Text(
                text = "Flashcard",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Học thông minh hơn mỗi ngày",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            if (email.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(
                    label = "$deckCount bộ thẻ",
                    icon  = Icons.Outlined.LibraryBooks
                )
                // ✅ Hiển thị trạng thái sync trong pill
                when (syncStatus) {
                    is SyncStatus.Success -> StatPill(
                        label = "Đã đồng bộ",
                        icon  = Icons.Default.CloudDone
                    )
                    is SyncStatus.Syncing -> StatPill(
                        label = "Đang sync...",
                        icon  = Icons.Default.Sync
                    )
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun StatPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.LibraryAdd,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            "Chưa có bộ thẻ nào",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Nhấn nút bên dưới để tạo\nbộ thẻ đầu tiên của bạn",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DeckItem(
    deck: Deck,
    viewModel: CardViewModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val totalCount by viewModel.getCardCount(deck.id).collectAsState(initial = 0)
    val dueCount   by viewModel.getDueCardCount(deck.id).collectAsState(initial = 0)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = deck.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (deck.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        deck.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniChip(
                        text  = "$totalCount thẻ",
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    if (dueCount > 0) {
                        MiniChip(
                            text      = "$dueCount cần ôn",
                            color     = ColorForgot.copy(alpha = 0.15f),
                            textColor = ColorForgot
                        )
                    } else {
                        MiniChip(
                            text      = "Đã ôn xong",
                            color     = ColorGood.copy(alpha = 0.15f),
                            textColor = ColorGood
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MiniChip(
    text: String,
    color: Color,
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AddDeckDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Tạo bộ thẻ mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên bộ thẻ *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Mô tả (tùy chọn)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), desc.trim()) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Tạo") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}