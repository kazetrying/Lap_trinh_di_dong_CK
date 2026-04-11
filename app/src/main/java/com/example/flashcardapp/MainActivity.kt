package com.example.flashcardapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.flashcardapp.ui.theme.theme.FlashcardAppTheme
import com.example.flashcardapp.viewmodel.CardViewModel
import com.example.flashcardapp.viewmodel.CardViewModelFactory
import com.example.flashcardapp.worker.ReminderScheduler

class MainActivity : ComponentActivity() {

    private val viewModel: CardViewModel by viewModels {
        CardViewModelFactory(application)
    }

    // Xin quyen notification (Android 13+)
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ReminderScheduler.schedule(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Bat dau lich nhac nho
        setupReminder()

        setContent {
            FlashcardAppTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    private fun setupReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ can xin quyen
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Da co quyen, dat lich luon
                    ReminderScheduler.schedule(this)
                }
                else -> {
                    // Xin quyen
                    requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android < 13 khong can xin quyen
            ReminderScheduler.schedule(this)
        }
    }
}