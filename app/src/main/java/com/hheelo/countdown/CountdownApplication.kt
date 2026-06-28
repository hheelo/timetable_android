package com.hheelo.countdown

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hheelo.countdown.logging.AppLog
import com.hheelo.countdown.logging.CrashHandler
import java.util.concurrent.TimeUnit

class CountdownApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLog.init(this)
        CrashHandler.install()
        AppLog.i(TAG, "应用启动，versionName=${BuildConfig.VERSION_NAME}, versionCode=${BuildConfig.VERSION_CODE}")
        NotificationHelper.createChannel(this)
        scheduleReminderWorker()
    }

    private fun scheduleReminderWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        AppLog.i(TAG, "已调度每日提醒检查 Worker")
    }

    private companion object {
        const val TAG = "CountdownApplication"
    }
}
