package com.hheelo.countdown

import android.app.Application
import com.hheelo.countdown.logging.AppLog
import com.hheelo.countdown.logging.CrashHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CountdownApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppLog.init(this)
        CrashHandler.install()
        AppLog.i(TAG, "应用启动，versionName=${BuildConfig.VERSION_NAME}, versionCode=${BuildConfig.VERSION_CODE}")
        NotificationHelper.createChannel(this)
        applicationScope.launch {
            ReminderScheduler.sync(this@CountdownApplication, CountdownStore(this@CountdownApplication).loadCustomEvents())
        }
    }

    private companion object {
        const val TAG = "CountdownApplication"
    }
}
