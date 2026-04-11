package com.example.flashmind.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private val AuthTextColor = Color(0xFF111111)

private val AuthBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF4F1EA),
        Color(0xFFE8E2D7),
        Color(0xFFDCE8E3),
    ),
)

private val AuthHeader = Brush.linearGradient(
    colors = listOf(
        Color(0xFF102A43),
        Color(0xFF183C5A),
        Color(0xFF1B666F),
    ),
)

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    state: State<AuthUiState>,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSwitchMode: (Boolean) -> Unit,
    onSubmit: () -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AuthHeader)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Flashcard",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFFFFFCF6),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (uiState.isRegisterMode) {
                            "Tạo tài khoản để đồng bộ bộ thẻ, lịch ôn tập và trợ lý học tập trên nhiều thiết bị."
                        } else {
                            "Đăng nhập để tiếp tục học trong không gian gọn, sáng và tập trung hơn."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFE7F0F2),
                    )
                    Text(
                        if (uiState.isRemoteSession) "Đang kết nối máy chủ" else "Sẵn sàng chế độ ngoại tuyến",
                        color = Color(0xFFF0D9A6),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AuthAction(
                    label = "Đăng nhập",
                    selected = !uiState.isRegisterMode,
                    onClick = { onSwitchMode(false) },
                )
                AuthAction(
                    label = "Đăng ký",
                    selected = uiState.isRegisterMode,
                    onClick = { onSwitchMode(true) },
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChanged,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                    )
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChanged,
                        label = { Text("Mật khẩu") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                    )
                    if (uiState.isRegisterMode) {
                        OutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = onConfirmPasswordChanged,
                            label = { Text("Xác nhận mật khẩu") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                        )
                    }
                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    AuthAction(
                        label = if (uiState.isRegisterMode) "Tạo tài khoản" else "Đăng nhập",
                        selected = true,
                        onClick = onSubmit,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthAction(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val textColor = if (selected) AuthTextColor else Color(0xFF5D5568)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF102A43) else Color.Transparent,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = if (selected) Color(0xFFFFFCF6) else textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

