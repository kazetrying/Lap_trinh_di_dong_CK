package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardapp.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    deckId: Long,
    viewModel: CardViewModel,
    onBack: () -> Unit
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var savedCount by remember { mutableStateOf(0) }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
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
                                "THÊM THẺ MỚI",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp,
                                fontSize = 18.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Icon(Icons.Rounded.ArrowBack, "Quay lại", tint = Color.White, modifier = Modifier.padding(10.dp))
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    if (savedCount > 0) {
                        Text(
                            text = "Đã lưu $savedCount thẻ mới",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Front Input
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Mặt trước (Tiếng Anh)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = primaryColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    OutlinedTextField(
                        value = front,
                        onValueChange = { front = it },
                        placeholder = { Text("Nhập câu hỏi hoặc từ cần học...", color = Color.Gray.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = primaryColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrect = true,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFF1A1C1E),
                            unfocusedTextColor = Color(0xFF1A1C1E)
                        )
                    )
                }

                // Back Input
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Mặt sau (Tiếng Việt)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = primaryColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    OutlinedTextField(
                        value = back,
                        onValueChange = { back = it },
                        placeholder = { Text("Nhập đáp án hoặc định nghĩa...", color = Color.Gray.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = primaryColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrect = true,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFF1A1C1E),
                            unfocusedTextColor = Color(0xFF1A1C1E)
                        )
                    )
                }

                Spacer(Modifier.weight(1f))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (front.isNotBlank() && back.isNotBlank()) {
                                viewModel.addCard(deckId, front.trim(), back.trim())
                                savedCount++
                                front = ""
                                back = ""
                            }
                        },
                        enabled = front.isNotBlank() && back.isNotBlank(),
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(2.dp, if (front.isNotBlank() && back.isNotBlank()) primaryColor else Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("LƯU + TIẾP", fontWeight = FontWeight.Black)
                        }
                    }

                    Button(
                        onClick = {
                            if (front.isNotBlank() && back.isNotBlank()) {
                                viewModel.addCard(deckId, front.trim(), back.trim())
                            }
                            onBack()
                        },
                        enabled = front.isNotBlank() && back.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = primaryColor.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("LƯU + XONG", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
