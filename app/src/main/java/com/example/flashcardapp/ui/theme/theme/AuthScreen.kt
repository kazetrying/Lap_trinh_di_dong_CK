package com.example.flashcardapp.ui.theme

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardapp.viewmodel.CardViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(
    viewModel: CardViewModel,
    onAuthSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    var isLoginMode  by remember { mutableStateOf(true) }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirmPass  by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val primaryColor = Color(0xFF6C63FF)
    val secondaryColor = Color(0xFF4B44CC)
    val backgroundColor = Color(0xFFF0F2F8)
    val textColor = Color(0xFF1A1C1E)
    
    val headerGradient = Brush.verticalGradient(
        colors = listOf(primaryColor, secondaryColor)
    )

    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            viewModel.startRealtimeSync()
            onAuthSuccess()
        }
    }

    Scaffold(
        containerColor = backgroundColor
    ) { paddingValues ->
        // Thêm verticalScroll để tránh mất nội dung trên màn hình nhỏ
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                    .background(headerGradient),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(20.dp, CircleShape),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(45.dp),
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Flashcard Master",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        if (isLoginMode) "Rất vui được gặp lại bạn!" else "Bắt đầu hành trình mới",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // ── Form chính (Dùng Spacer/Padding thay vì offset để tránh lỗi hiển thị) ──────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp)
                        .shadow(16.dp, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Switch Đăng nhập / Đăng ký
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF0F2F8)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Đăng nhập" to true, "Đăng ký" to false).forEach { (label, isLogin) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isLoginMode == isLogin) primaryColor else Color.Transparent)
                                        .clickable { 
                                            isLoginMode = isLogin
                                            errorMessage = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isLoginMode == isLogin) Color.White else textColor
                                    )
                                }
                            }
                        }

                        // Trường Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = "" },
                            label = { Text("Email", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Rounded.Email, null, tint = primaryColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        // Trường Mật khẩu
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = "" },
                            label = { Text("Mật khẩu", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = primaryColor) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        null,
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next
                            )
                        )

                        // Trường Xác nhận mật khẩu
                        if (!isLoginMode) {
                            OutlinedTextField(
                                value = confirmPass,
                                onValueChange = { confirmPass = it; errorMessage = "" },
                                label = { Text("Xác nhận mật khẩu", color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Rounded.VerifiedUser, null, tint = primaryColor) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    focusedLabelColor = primaryColor,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }

                        if (errorMessage.isNotBlank()) {
                            Text(
                                errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Nút Hành động chính
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Vui lòng nhập đầy đủ thông tin"
                                    return@Button
                                }
                                if (!isLoginMode && password != confirmPass) {
                                    errorMessage = "Mật khẩu không khớp"
                                    return@Button
                                }
                                isLoading = true
                                if (isLoginMode) {
                                    auth.signInWithEmailAndPassword(email.trim(), password)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            viewModel.startRealtimeSync()
                                            onAuthSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = "Lỗi: ${e.localizedMessage}"
                                        }
                                } else {
                                    auth.createUserWithEmailAndPassword(email.trim(), password)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            onAuthSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = "Lỗi: ${e.localizedMessage}"
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                            } else {
                                Text(
                                    if (isLoginMode) "Đăng nhập" else "Đăng ký",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                Text(
                    "Bằng việc sử dụng ứng dụng, bạn đồng ý với Điều khoản của chúng tôi",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 16.dp)
                )
            }
        }
    }
}
