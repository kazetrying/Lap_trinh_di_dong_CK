package com.example.flashmind

import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.flashmind.settings.AppSettings
import com.example.flashmind.settings.AppSettingsViewModel
import com.example.flashmind.navigation.FlashMindNavHost
import com.example.flashmind.worker.DailyReviewWorker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: AppSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeReminderSettings()
        setContent {
            val settings = settingsViewModel.uiState.collectAsStateWithLifecycle()
            MaterialTheme(
                colorScheme = if (settings.value.useDarkTheme) darkAppColors() else lightAppColors(),
                typography = Typography(
                    headlineLarge = TextStyle(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
                    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
                    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
                    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
                    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
                    titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium),
                    bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 26.sp, fontWeight = FontWeight.Normal),
                    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal),
                    labelLarge = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium),
                    labelMedium = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
                ),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    FlashMindNavHost()
                }
            }
        }
    }

    private fun observeReminderSettings() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.uiState.collect { state ->
                    scheduleReminder(
                        AppSettings(
                            useDarkTheme = state.useDarkTheme,
                            reminderEnabled = state.reminderEnabled,
                            reminderHour = state.reminderHour,
                            reminderMinute = state.reminderMinute,
                        ),
                    )
                }
            }
        }
    }

    private fun scheduleReminder(settings: AppSettings) {
        val workManager = WorkManager.getInstance(applicationContext)
        if (!settings.reminderEnabled) {
            workManager.cancelUniqueWork(DailyReviewWorker.WORK_NAME)
            return
        }
        val initialDelay = computeInitialDelay(settings.reminderHour, settings.reminderMinute)
        val request = PeriodicWorkRequestBuilder<DailyReviewWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(
            DailyReviewWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun computeInitialDelay(hour: Int, minute: Int): Duration {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return Duration.between(now, target)
    }
}

private fun lightAppColors() = lightColorScheme(
    primary = Color(0xFF14304A),
    secondary = Color(0xFF1F6B70),
    tertiary = Color(0xFFDAA94D),
    background = Color(0xFFF4F1EA),
    surface = Color(0xFFFFFCF6),
    surfaceVariant = Color(0xFFE8E2D6),
    primaryContainer = Color(0xFFDCE8F3),
    secondaryContainer = Color(0xFFDCEFEB),
    tertiaryContainer = Color(0xFFF4E4C6),
    error = Color(0xFFB3261E),
)

private fun darkAppColors() = darkColorScheme(
    primary = Color(0xFF89B4D8),
    secondary = Color(0xFF6EC4B1),
    tertiary = Color(0xFFE3BC67),
    background = Color(0xFF10171E),
    surface = Color(0xFF18212B),
    surfaceVariant = Color(0xFF22303C),
    primaryContainer = Color(0xFF1E3445),
    secondaryContainer = Color(0xFF17353A),
    tertiaryContainer = Color(0xFF4B3F22),
    error = Color(0xFFF2B8B5),
)
