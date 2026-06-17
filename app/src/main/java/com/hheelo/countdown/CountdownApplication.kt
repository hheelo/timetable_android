package com.hheelo.countdown

import android.app.Application
import com.hheelo.countdown.logging.AppLog
import com.hheelo.countdown.logging.CrashHandler

class CountdownApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLog.init(this)
        CrashHandler.install()
        AppLog.i(TAG, "应用启动，versionName=${BuildConfig.VERSION_NAME}, versionCode=${BuildConfig.VERSION_CODE}")
    }

    private companion object {
        const val TAG = "CountdownApplication"
    }
}
