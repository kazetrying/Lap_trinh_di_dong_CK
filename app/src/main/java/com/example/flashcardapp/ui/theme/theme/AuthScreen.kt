package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

    // Nếu đã đăng nhập rồi thì vào thẳng HomeScreen
    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            viewModel.startRealtimeSync()   // ✅ Tự động đồng bộ realtime thay vì pull 1 lần
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Style,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = Color.White
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Flashcard",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (isLoginMode) "Chào mừng trở lại!" else "Tạo tài khoản mới",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        // ── Form ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Tab Đăng nhập / Đăng ký
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                listOf("Đăng nhập" to true, "Đăng ký" to false).forEach { (label, isLogin) ->
                    Button(
                        onClick = {
                            isLoginMode  = isLogin
                            errorMessage = ""
                            confirmPass  = ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoginMode == isLogin)
                                MaterialTheme.colorScheme.primary
                            else Color.Transparent,
                            contentColor = if (isLoginMode == isLogin)
                                Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(label, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            // Mật khẩu
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showPassword)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next
                )
            )

            // Xác nhận mật khẩu
            if (!isLoginMode) {
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it; errorMessage = "" },
                    label = { Text("Xác nhận mật khẩu") },
                    leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )
            }

            // Thông báo lỗi
            if (errorMessage.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Nút chính
            Button(
                onClick = {
                    when {
                        email.isBlank() -> {
                            errorMessage = "Vui lòng nhập email"
                            return@Button
                        }
                        password.length < 6 -> {
                            errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                            return@Button
                        }
                        !isLoginMode && password != confirmPass -> {
                            errorMessage = "Mật khẩu xác nhận không khớp"
                            return@Button
                        }
                    }

                    isLoading    = true
                    errorMessage = ""

                    if (isLoginMode) {
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnSuccessListener {
                                isLoading = false
                                viewModel.startRealtimeSync()   // ✅ Realtime thay vì pull 1 lần
                                onAuthSuccess()
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = when {
                                    e.message?.contains("no user record") == true ->
                                        "Email chưa được đăng ký"
                                    e.message?.contains("password is invalid") == true ||
                                            e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                                        "Email hoặc mật khẩu không đúng"
                                    e.message?.contains("badly formatted") == true ->
                                        "Email không hợp lệ"
                                    else -> "Đăng nhập thất bại. Thử lại sau"
                                }
                            }
                    } else {
                        auth.createUserWithEmailAndPassword(email.trim(), password)
                            .addOnSuccessListener {
                                isLoading = false
                                onAuthSuccess()
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = when {
                                    e.message?.contains("already in use") == true ->
                                        "Email này đã được sử dụng"
                                    e.message?.contains("badly formatted") == true ->
                                        "Email không hợp lệ"
                                    else -> "Đăng ký thất bại. Thử lại sau"
                                }
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        if (isLoginMode) Icons.Default.Login else Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isLoginMode) "Đăng nhập" else "Đăng ký",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}