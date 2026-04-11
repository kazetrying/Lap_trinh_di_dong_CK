package com.example.flashcardapp.ui.theme

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.flashcardapp.worker.ReminderPrefs
import com.example.flashcardapp.worker.ReminderScheduler
import com.example.flashcardapp.worker.ReminderWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var reminderEnabled by remember { mutableStateOf(ReminderPrefs.isEnabled(context)) }
    var selectedHour    by remember { mutableStateOf(ReminderPrefs.getHour(context)) }
    var selectedMinute  by remember { mutableStateOf(ReminderPrefs.getMinute(context)) }
    var showSaved       by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF6C63FF)
    val secondaryColor = Color(0xFF4B44CC)
    val backgroundColor = Color(0xFFF0F2F8)
    
    val headerGradient = Brush.verticalGradient(
        colors = listOf(primaryColor, secondaryColor)
    )

    Scaffold(
        containerColor = backgroundColor,
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
                                "Cài đặt",
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    Text(
                        "Tùy chỉnh trải nghiệm học tập của bạn",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section: Reminder
            Text(
                "Thông báo & Nhắc nhở",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1C1E)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Rounded.NotificationsActive,
                        title = "Bật nhắc nhở học tập",
                        subtitle = "Nhận thông báo khi đến giờ ôn bài",
                        trailing = {
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { enabled ->
                                    reminderEnabled = enabled
                                    ReminderPrefs.setEnabled(context, enabled)
                                    if (enabled) {
                                        ReminderScheduler.schedule(context, selectedHour, selectedMinute)
                                    } else {
                                        ReminderScheduler.cancel(context)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                            )
                        }
                    )
                    
                    if (reminderEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = backgroundColor)
                        SettingsItem(
                            icon = Icons.Rounded.Schedule,
                            title = "Thời gian nhắc nhở",
                            subtitle = "Chọn khung giờ phù hợp nhất với bạn",
                            onClick = {
                                showTimePicker(context, selectedHour, selectedMinute) { h, m ->
                                    selectedHour = h
                                    selectedMinute = m
                                    ReminderPrefs.setTime(context, h, m)
                                    ReminderScheduler.schedule(context, h, m)
                                    showSaved = true
                                }
                            },
                            trailing = {
                                Surface(
                                    color = primaryColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "%02d:%02d".format(selectedHour, selectedMinute),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = primaryColor,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Quick Select Time
            if (reminderEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val presets = listOf(
                        Triple("Sáng", 8, 0),
                        Triple("Trưa", 12, 0),
                        Triple("Chiều", 18, 0),
                        Triple("Tối", 21, 0)
                    )
                    presets.forEach { (label, h, m) ->
                        val isSelected = selectedHour == h && selectedMinute == m
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedHour = h
                                    selectedMinute = m
                                    ReminderPrefs.setTime(context, h, m)
                                    ReminderScheduler.schedule(context, h, m)
                                    showSaved = true
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) primaryColor else Color.White,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Gray)
                                Text("%02d:%02d".format(h, m), fontSize = 14.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color.White else Color(0xFF1A1C1E))
                            }
                        }
                    }
                }
            }

            // Section: Algorithm Info
            Text(
                "Thuật toán học tập",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1C1E)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    InfoRow(Icons.Rounded.Psychology, "Thuật toán", "SuperMemo SM-2")
                    InfoRow(Icons.Rounded.Repeat, "Lặp lại tối thiểu", "1 ngày")
                    InfoRow(Icons.Rounded.TrendingUp, "Hệ số khó", "2.5 (Mặc định)")
                }
            }

            // Test Button
            Button(
                onClick = {
                    val request = OneTimeWorkRequestBuilder<ReminderWorker>().build()
                    WorkManager.getInstance(context).enqueue(request)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Rounded.BugReport, null)
                Spacer(Modifier.width(8.dp))
                Text("Gửi thông báo thử nghiệm", fontWeight = FontWeight.Bold)
            }
            
            if (showSaved) {
                LaunchedEffect(showSaved) {
                    kotlinx.coroutines.delay(2000)
                    showSaved = false
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF4ADE80).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF166534))
                        Spacer(Modifier.width(8.dp))
                        Text("Đã cập nhật giờ nhắc nhở!", color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF6C63FF).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF6C63FF), modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
    }
}

fun showTimePicker(
    context: Context,
    currentHour: Int,
    currentMinute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onTimeSelected(hour, minute) },
        currentHour,
        currentMinute,
        true
    ).show()
}
