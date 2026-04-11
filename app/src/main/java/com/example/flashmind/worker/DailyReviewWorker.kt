package com.example.flashmind.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.flashmind.core.domain.usecase.CountDueCardsUseCase
import com.example.flashmind.core.domain.usecase.SyncPendingTasksUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyReviewWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val countDueCardsUseCase: CountDueCardsUseCase,
    private val syncPendingTasksUseCase: SyncPendingTasksUseCase,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        createChannel()
        runCatching { syncPendingTasksUseCase() }
        val dueCount = countDueCardsUseCase()
        if (dueCount > 0) {
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Flashcard")
                .setContentText("You have $dueCount cards ready for review today.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            NotificationManagerCompat.from(applicationContext).notify(101, notification)
        }
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily review",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val WORK_NAME = "daily_review_reminder"
        private const val CHANNEL_ID = "flashmind_daily_review"
    }
}

