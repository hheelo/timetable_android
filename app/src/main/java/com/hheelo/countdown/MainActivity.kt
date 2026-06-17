package com.hheelo.countdown

import android.content.Intent
import android.os.Bundle
import com.hheelo.countdown.logging.AppLog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private var selectedEventId by mutableStateOf<String?>(null)
    private var selectedEventRequest by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedEventId = AppDeepLink.eventIdFrom(intent?.data)
        AppLog.i(TAG, "onCreate，深链 data=${intent?.data}，解析到 eventId=$selectedEventId")
        setContent {
            CountdownTheme {
                CountdownApp(
                    selectedEventId = selectedEventId,
                    selectedEventRequest = selectedEventRequest
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        selectedEventId = AppDeepLink.eventIdFrom(intent.data)
        selectedEventRequest += 1
        AppLog.i(TAG, "onNewIntent，深链 data=${intent.data}，解析到 eventId=$selectedEventId")
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
