package com.yourname.flashlearn

import android.util.Log
import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.yourname.flashlearn.worker.StudyReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class FlashLearnApp : Application(), Configuration.Provider {
    private fun testNotificationNow() {
        Log.d("FlashLearnApp", "testNotificationNow called")
        val request = OneTimeWorkRequestBuilder<StudyReminderWorker>()
            .build()
        WorkManager.getInstance(this).enqueue(request)
    }


    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        try {
            scheduleDailyReminder()
        } catch (_: Exception) {
        }

        try {
            // CHỈ ĐỂ TEST - xóa sau khi kiểm tra xong
            testNotificationNow()
        } catch (_: Exception) {
        }
    }

    private fun scheduleDailyReminder() {
        // Tính delay đến 20:00 tối hôm nay
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            // Nếu đã qua 20:00 thì lên lịch ngày mai
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        val delay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<StudyReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            StudyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}