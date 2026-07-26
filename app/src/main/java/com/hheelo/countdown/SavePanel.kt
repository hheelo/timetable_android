package com.hheelo.countdown

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hheelo.countdown.logging.LogExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun SavePanel(
    savedMessage: String?,
    tintHex: String,
    onSave: () -> Unit,
    isSaving: Boolean = false,
    messageIsError: Boolean = false,
    enabled: Boolean = true
) {
    val extraColors = LocalCountdownColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onSave,
            enabled = enabled && !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = countdownColor(tintHex)),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Filled.Save, contentDescription = null)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(
                    if (isSaving) R.string.saving_and_refreshing_widget else R.string.save_and_refresh_widget
                )
            )
        }
        savedMessage?.let {
            Text(
                it,
                color = if (messageIsError) countdownColor(CountdownColorHex.Danger) else extraColors.success,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
internal fun Footer() {
    val context = LocalContext.current
    val extraColors = LocalCountdownColors.current
    val scope = rememberCoroutineScope()
    // 在 composable 作用域内取字符串：Context.getString 不会随 Configuration 变化而重组，
    // 手势回调里直接用 context.getString 会拿到过期的本地化文案。
    val shareLogTitle = stringResource(R.string.share_log)
    val noLogMessage = stringResource(R.string.no_log_to_export)
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull()
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
        Text(stringResource(R.string.holiday_info_title), fontWeight = FontWeight.SemiBold, color = extraColors.textPrimary)
        Text(
            stringResource(R.string.holiday_info_body),
            color = extraColors.textSecondary,
            fontSize = 13.sp
        )
        versionName?.let {
            Text(
                stringResource(R.string.version_with_export_hint, it),
                color = extraColors.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        // 导出会读取整个日志文件并落盘，放到 IO 线程避免卡住主线程。
                        scope.launch {
                            val intent = withContext(Dispatchers.IO) { LogExporter.shareIntent(context) }
                            if (intent != null) {
                                context.startActivity(Intent.createChooser(intent, shareLogTitle))
                            } else {
                                Toast.makeText(context, noLogMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
            )
        }
    }
}
