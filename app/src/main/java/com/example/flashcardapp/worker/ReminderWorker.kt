package com.example.flashcardapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.flashcardapp.MainActivity
import com.example.flashcardapp.data.AppDatabase

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao      = AppDatabase.getInstance(context).cardDao()
        val dueCount = dao.getAllDueCardsCount(System.currentTimeMillis())

        // Luôn hiện thông báo khi đến giờ
        showNotification(dueCount)

        return Result.success()
    }

    private fun showNotification(dueCount: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Tạo channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở ôn tập",
                NotificationManager.IMPORTANCE_HIGH  // HIGH để hiện banner
            ).apply {
                description = "Thông báo nhắc nhở ôn tập flashcard hàng ngày"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (title, body) = if (dueCount > 0) {
            "Đến giờ ôn tập rồi! 📚" to "Bạn có $dueCount thẻ cần ôn tập hôm nay"
        } else {
            "Nhắc nhở học tập 📖" to "Hãy vào app ôn tập một chút nhé!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // HIGH để hiện banner
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID      = "flashcard_reminder"
        const val NOTIFICATION_ID = 1001
    }
}