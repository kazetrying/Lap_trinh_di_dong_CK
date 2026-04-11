package com.example.flashmind.feature.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ImportDeckScreen(
    state: State<ImportDeckUiState>,
    onJsonChanged: (String) -> Unit,
    onBack: () -> Unit,
    onImport: () -> Unit,
) {
    val uiState by state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckScreenBackground),
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
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeaderBackground)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextAction(label = "Quay lại", onClick = onBack)
                    Text(
                        "Nhập bộ thẻ",
                        style = MaterialTheme.typography.headlineLarge,
                        color = DeckTextColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Dán JSON bộ thẻ đã xuất để nhập thẻ vào máy.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DeckTextColor,
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.json,
                        onValueChange = onJsonChanged,
                        label = { Text("JSON bộ thẻ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 12,
                        shape = RoundedCornerShape(18.dp),
                    )
                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    TextAction(label = "Nhập bộ thẻ", onClick = onImport)
                }
            }
        }
    }
}
