package com.yourname.flashlearn.worker

import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourname.flashlearn.MainActivity
import com.yourname.flashlearn.domain.repository.FlashcardRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class StudyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: FlashcardRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "study_reminder"
        const val WORK_NAME = "daily_study_reminder"
    }

    override suspend fun doWork(): Result {
        Log.d("StudyReminderWorker", "doWork started")
        return try {
            val dueCount = repository.getTotalDueCount().first()
            Log.d("StudyReminderWorker", "dueCount = $dueCount")
            showNotification(dueCount)
            Log.d("StudyReminderWorker", "showNotification called")
            Result.success()
        } catch (e: Exception) {
            Log.e("StudyReminderWorker", "doWork error", e)
            Result.failure()
        }
    }

    private fun showNotification(dueCount: Int) {
        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo channel (bắt buộc Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở học tập",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nhắc bạn ôn flashcard mỗi ngày"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // Nhấn thông báo → mở app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (dueCount > 0)
            "Bạn có $dueCount thẻ cần ôn tập hôm nay! 💪"
        else
            "Hãy dành 5 phút học flashcard mỗi ngày nhé! 📖"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📚 FlashLearn nhắc nhở!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1001, notification)
    }
}